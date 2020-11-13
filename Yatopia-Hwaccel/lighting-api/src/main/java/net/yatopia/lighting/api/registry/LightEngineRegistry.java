package net.yatopia.lighting.api.registry;

import net.yatopia.hwaccel.utils.registry.Registry;

public class LightEngineRegistry {

    public static final Registry<LightEngineImplProvider> LIGHT_ENGINE_IMPL_PROVIDERS = new Registry<>("light_engine_impl_providers");

}
