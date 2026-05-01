package com.donuttracker.addon;

import com.donuttracker.addon.modules.SubChunkPlayerTracker;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class DonutTrackerAddon extends MeteorAddon {

    public static final String NAME = "DonutSMP Tracker";

    @Override
    public void onInitialize() {
        Modules.get().add(new SubChunkPlayerTracker());
    }

    @Override
    public String getPackage() {
        return "com.donuttracker.addon";
    }
}
