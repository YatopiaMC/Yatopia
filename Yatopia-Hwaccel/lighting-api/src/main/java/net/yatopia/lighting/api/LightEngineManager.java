package net.yatopia.lighting.api;

import net.yatopia.lighting.api.registry.LightEngineImplProvider;
import net.yatopia.lighting.api.registry.LightEngineRegistry;
import net.yatopia.lighting.api.structures.ChunkProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;

public class LightEngineManager {

    public static final Logger LOGGER = LogManager.getLogger();

    public static LightEngineImpl create(ChunkProvider chunkProvider,
                                         boolean hasSkylight,
                                         ExecutorService lightThread) {
        LOGGER.info("Initializing " + LightingConfiguration.lightEngineImplRegistryKey + " light engine for world " + chunkProvider.getWorldName() + "...");
        LightEngineImplProvider lightEngineImplProvider = LightEngineRegistry.LIGHT_ENGINE_IMPL_PROVIDERS.getOrThrow(LightingConfiguration.lightEngineImplRegistryKey);
        LightEngineImpl lightEngine = lightEngineImplProvider.create(chunkProvider, hasSkylight, lightThread);
        LOGGER.info("Light engine initialized. ");
        return lightEngine;
    }

}
