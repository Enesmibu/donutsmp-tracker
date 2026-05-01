package com.donuttracker.addon.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;

/**
 * DonutSMP Sub-Chunk Player Tracker
 *
 * DonutSMP sunucusunda y:0'ın altı normalde görünmez. Ancak y:-1'e inildiğinde
 * 1-4 chunk aralığında y:0 altı görünür hale gelir. Bu modül;
 *   - Etrafındaki belirli chunk aralığında y:0 altında hareket eden oyuncuları takip eder
 *   - Tespit edilen chunk'lar için y=-64'ten y=320'ye kadar dikey bir sütun render eder
 *     → Sütunun y=0 üstündeki kısmı zeminin üzerine çıktığı için herhangi bir
 *       yükseklikten (örn. y:68) görünür hale gelir
 *   - Hangi yönde (sağ, sol, ön, arka, köşegen) olduklarını chat'e yazar
 */
public class SubChunkPlayerTracker extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender  = settings.createGroup("Görünüm");

    // ── Genel Ayarlar ─────────────────────────────────────────────────────────

    private final Setting<Integer> chunkRadius = sgGeneral.add(new IntSetting.Builder()
        .name("chunk-yarıçapı")
        .description("Kaç chunk çevresindeki oyuncular takip edilsin (önerilen: 3-4)")
        .defaultValue(4)
        .min(1)
        .max(10)
        .sliderMax(8)
        .build()
    );

    private final Setting<Integer> trackMinutes = sgGeneral.add(new IntSetting.Builder()
        .name("takip-süresi-dk")
        .description("Son kaç dakika içinde görülen oyuncuları göster")
        .defaultValue(5)
        .min(1)
        .max(30)
        .sliderMax(15)
        .build()
    );

    private final Setting<Boolean> chatAlert = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-uyarısı")
        .description("Yeni bir chunk tespit edildiğinde chat'e mesaj yaz")
        .defaultValue(true)
        .build()
    );

    // ── Render Ayarları ───────────────────────────────────────────────────────

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("chunk-dolgu-rengi")
        .description("Highlight kutusunun dolgu rengi")
        .defaultValue(new SettingColor(255, 50, 50, 25))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("chunk-kenar-rengi")
        .description("Highlight kutusunun kenar rengi")
        .defaultValue(new SettingColor(255, 50, 50, 220))
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("şekil-modu")
        .description("Sadece çizgi mi, dolgu mu, ikisi birden mi")
        .defaultValue(ShapeMode.Lines)
        .build()
    );

    private final Setting<Integer> renderMinY = sgRender.add(new IntSetting.Builder()
        .name("render-min-y")
        .description("Sütunun alt sınırı (world bottom: -64)")
        .defaultValue(-64)
        .min(-64)
        .max(0)
        .sliderMin(-64)
        .sliderMax(0)
        .build()
    );

    private final Setting<Integer> renderMaxY = sgRender.add(new IntSetting.Builder()
        .name("render-max-y")
        .description("Sütunun üst sınırı — zemin üstüne çıkan kısım her yükseklikten görünür")
        .defaultValue(320)
        .min(0)
        .max(320)
        .sliderMin(0)
        .sliderMax(320)
        .build()
    );

    // ── İç Durum ──────────────────────────────────────────────────────────────

    // ChunkPos → son görülme zamanı (ms)
    private final Map<ChunkPos, Long> trackedChunks = new HashMap<>();
    // Bildirim gönderildi mi?
    private final Map<ChunkPos, Boolean> notified    = new HashMap<>();

    public SubChunkPlayerTracker() {
        super(Categories.World, "subchunk-player-tracker",
              "DonutSMP'de y:0 altındaki chunk'larda oyuncu varlığını takip eder. " +
              "Sütun zemine çıktığı için herhangi bir y seviyesinden görünür.");
    }

    @Override
    public void onActivate() {
        trackedChunks.clear();
        notified.clear();
    }

    // ── Tick: Oyuncu Takibi ───────────────────────────────────────────────────

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;

        long now   = System.currentTimeMillis();
        long limit = (long) trackMinutes.get() * 60_000L;

        // Süresi dolmuş chunk'ları temizle
        trackedChunks.entrySet().removeIf(e -> now - e.getValue() > limit);
        notified.keySet().removeIf(cp -> !trackedChunks.containsKey(cp));

        ChunkPos myChunk = new ChunkPos(mc.player.getBlockPos());
        int radius = chunkRadius.get();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (player.getY() >= 0) continue; // Sadece y:0 altındaki oyuncular

            ChunkPos playerChunk = new ChunkPos(player.getBlockPos());

            int dx = Math.abs(playerChunk.x - myChunk.x);
            int dz = Math.abs(playerChunk.z - myChunk.z);
            if (dx > radius || dz > radius) continue;

            boolean isNew = !trackedChunks.containsKey(playerChunk);
            trackedChunks.put(playerChunk, now);

            // İlk kez tespit → chat bildirimi
            if (isNew && chatAlert.get() && !notified.containsKey(playerChunk)) {
                notified.put(playerChunk, true);
                String direction = getDirection(myChunk, playerChunk);
                String msg = String.format(
                    "§c[SubChunkTracker] §fOyuncu tespit edildi! §aYön: §e%s §f| Chunk: §b[%d, %d]",
                    direction, playerChunk.x, playerChunk.z
                );
                mc.player.sendMessage(net.minecraft.text.Text.literal(msg), false);
            }
        }
    }

    // ── Render: Dikey Sütun ───────────────────────────────────────────────────

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (trackedChunks.isEmpty()) return;

        long now   = System.currentTimeMillis();
        long limit = (long) trackMinutes.get() * 60_000L;

        double yBottom = renderMinY.get();   // varsayılan: -64
        double yTop    = renderMaxY.get();   // varsayılan: 320  ← zemin üstüne çıkan kısım görünür

        for (Map.Entry<ChunkPos, Long> entry : trackedChunks.entrySet()) {
            if (now - entry.getValue() > limit) continue;

            ChunkPos cp = entry.getKey();

            double x1 = cp.getStartX();
            double z1 = cp.getStartZ();
            double x2 = cp.getEndX() + 1;   // chunk 16 blok geniş, +1 tam kapsar
            double z2 = cp.getEndZ() + 1;

            // Tek sütun: alttan (yBottom) üste (yTop) kadar
            // Sütunun y=0 üstündeki köşe çizgileri zeminin üstünde kalır
            // → sen y:68'de olsan bile o 4 dikey köşe çizgisini görürsün
            event.renderer.box(
                x1, yBottom, z1,
                x2, yTop,    z2,
                sideColor.get(),
                lineColor.get(),
                shapeMode.get(),
                0
            );
        }
    }

    // ── Yardımcı: Yön Hesaplama ───────────────────────────────────────────────

    /**
     * İki chunk arasındaki yönü Türkçe döndürür. Köşegenleri de destekler.
     * Minecraft yön sistemi: Z- = Kuzey (ileri), Z+ = Güney (geri),
     *                        X+ = Doğu (sağ),   X- = Batı (sol)
     */
    private String getDirection(ChunkPos from, ChunkPos to) {
        int dx = to.x - from.x;
        int dz = to.z - from.z;

        boolean kuzey = dz < 0;
        boolean guney = dz > 0;
        boolean dogu  = dx > 0;
        boolean bati  = dx < 0;

        if (kuzey && !dogu && !bati)   return "Kuzey (İleri) ↑";
        if (guney && !dogu && !bati)   return "Güney (Geri) ↓";
        if (dogu  && !kuzey && !guney) return "Doğu (Sağ) →";
        if (bati  && !kuzey && !guney) return "Batı (Sol) ←";
        if (kuzey && dogu)             return "Kuzeydoğu (Sağ-İleri) ↗";
        if (kuzey && bati)             return "Kuzeybatı (Sol-İleri) ↖";
        if (guney && dogu)             return "Güneydoğu (Sağ-Geri) ↘";
        if (guney && bati)             return "Güneybatı (Sol-Geri) ↙";

        return "Aynı Chunk";
    }
}
