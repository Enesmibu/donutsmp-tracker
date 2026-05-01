# DonutSMP SubChunk Player Tracker

## Bu nedir?

Bu, **Meteor Client ana koduna dokunmayan**, bağımsız bir **Fabric addon** modülüdür.  
Derlenince tek bir `.jar` dosyası çıkar. O jar'ı Meteor Client ile birlikte `mods` klasörüne atarsın, hepsi bu.

> Meteor Client'ı kaynak koddan derlemeye gerek yok.  
> Bu addon Meteor Client'ı bir **bağımlılık** olarak kullanır, içine gömülmez.

---

## Bu mod ne yapar?

DonutSMP sunucusunda **y:0 katının altı normalde görünmez**.  
Ancak **y:-1'e inildiğinde** etrafında 1-4 chunk görünür hale gelir.

Bu modül:
- Etrafında belirlediğin chunk yarıçapında (varsayılan: 4 chunk) **y:0 altında** hareket eden oyuncuları takip eder
- Son **5 dakika** içinde orada görülen chunk'ları **kırmızı 3D kutu** ile highlight eder
- Yeni oyuncu tespit edilince **chat'e yön bilgisi** yazar (Kuzey/Güney + köşegenler)
- Highlight'lar sadece sen **y≥0'dayken** görünür

---

## Gereksinimler

| Yazılım | Versiyon |
|---|---|
| Java (JDK) | 21 |
| Minecraft | 1.21.4 |
| Fabric Loader | 0.16.9+ |
| Fabric API | 0.119.2+1.21.4 |
| Meteor Client | 0.5.9-SNAPSHOT |

---

## Proje Yapısı

```
minecraft-addon/
├── build.gradle                          ← Bağımlılıklar ve build ayarları
├── settings.gradle
├── gradlew / gradlew.bat                 ← Gradle indirme + çalıştırma scripti
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── src/main/java/com/donuttracker/addon/
    ├── DonutTrackerAddon.java            ← Addon giriş noktası
    └── modules/
        └── SubChunkPlayerTracker.java    ← Tek modül, asıl mantık burada
```

---

## Derleme (Build)

**Ön koşul:** Java 21 kurulu olmalı. İndirmek için: https://adoptium.net/

```bash
# Bu klasörü herhangi bir yere çıkart, terminal ile içine gir:
cd minecraft-addon

# Windows:
gradlew.bat build

# Linux / Mac:
chmod +x gradlew
./gradlew build
```

İlk çalıştırmada Gradle otomatik indirilir, sonra Minecraft mappings ve Meteor Client indirilir.  
İnternet bağlantısına göre 2-5 dakika sürebilir.

Derleme başarılıysa `.jar` dosyası şu konumda oluşur:

```
build/libs/donutsmp-subchunk-tracker-1.0.0.jar
```

---

## Kurulum

1. `donutsmp-subchunk-tracker-1.0.0.jar` dosyasını Minecraft **mods** klasörüne koy:
   - Windows: `%appdata%\.minecraft\mods\`
   - Linux/Mac: `~/.minecraft/mods/`
2. Minecraft Launcher'da **Fabric 1.21.4** profili seç.
3. Oyunu başlat → oyuna gir → `.` tuşuna bas (Meteor Client menüsü).
4. **Modules** → **World** sekmesine git.
5. **subchunk-player-tracker** modülünü bul ve aktifleştir.

---

## Ayarlar

| Ayar | Açıklama | Varsayılan |
|---|---|---|
| `chunk-yarıçapı` | Kaç chunk çevresinde takip yapılsın | 4 |
| `takip-süresi-dk` | Son kaç dakika içinde görülen oyuncuları göster | 5 |
| `sadece-yüzeyde-göster` | Highlight'ları sadece sen y≥0'dayken göster | Açık |
| `chat-uyarısı` | Yeni tespit edilince chat'e mesaj yaz | Açık |
| `chunk-dolgu-rengi` | Highlight kutusunun içi | Kırmızı %15 opak |
| `chunk-kenar-rengi` | Highlight kutusunun kenarı | Kırmızı parlak |
| `şekil-modu` | Sadece çizgi / sadece dolgu / ikisi | İkisi |
| `render-min-y` | Kutunun alt sınırı | -64 |

---

## Nasıl Kullanılır?

1. DonutSMP sunucusuna gir, modülü aktifleştir.
2. Y:0 katında dolaşırken mod arka planda oyuncuları takip eder.
3. Etrafındaki 4 chunk içinde **y:0 altında** bir oyuncu varsa:
   - Chat mesajı gelir: `[SubChunkTracker] Oyuncu tespit edildi! Yön: Kuzeydoğu (Sağ-İleri) ↗ | Chunk: [102, -45]`
   - O chunk kırmızı kutu ile işaretlenir.
4. Highlight **5 dakika** sonra kaybolur (yeniden görülmezse).

---

## Sorun Giderme

**`Could not resolve meteordevelopment:meteor-client`**  
→ `build.gradle` dosyasındaki `meteor-client` versiyonunu  
[buradan](https://maven.meteordev.org/snapshots/meteordevelopment/meteor-client/) kontrol et ve güncelle.

**`Unsupported class file major version`**  
→ Java 21 kurulu değil. `java -version` komutuyla kontrol et.

**Modül Meteor'da görünmüyor**  
→ Fabric API ve Meteor Client `.jar` dosyalarının da `mods` klasöründe olduğundan emin ol.
