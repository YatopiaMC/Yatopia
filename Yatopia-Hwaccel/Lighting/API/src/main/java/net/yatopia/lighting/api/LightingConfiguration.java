package net.yatopia.lighting.api;

import net.yatopia.hwaccel.utils.ConfigSuppliers;

public class LightingConfiguration {

    public static LightEngineType lightEngineType = LightEngineType.VANILLA;

    public static void reload() {
        lightEngineType = LightEngineType.valueOf(ConfigSuppliers.stringConfigSupplier.getString("settings.light-engine.type", lightEngineType.name()));
    }

}
