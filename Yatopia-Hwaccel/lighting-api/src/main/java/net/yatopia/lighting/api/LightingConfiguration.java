package net.yatopia.lighting.api;

import net.yatopia.hwaccel.utils.ConfigSuppliers;
import net.yatopia.hwaccel.utils.registry.Identifier;

public class LightingConfiguration {

    public static Identifier lightEngineImplRegistryKey = new Identifier("minecraft", "vanilla");

    public static void reload() {
        lightEngineImplRegistryKey = Identifier.tryParse(ConfigSuppliers.stringConfigSupplier.getString("settings.light-engine.type", lightEngineImplRegistryKey.toString()));
    }

}
