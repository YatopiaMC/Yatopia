package net.yatopia.hwaccel.lighting;

import net.yatopia.hwaccel.configuration.LightingConfiguration;
import net.yatopia.hwaccel.lighting.structures.ChunkProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;

public class LightEngineManager {

    private static final Logger LOGGER = LogManager.getLogger();

    public static LightEngineImpl create(ChunkProvider chunkProvider,
                                         boolean hasSkylight,
                                         ExecutorService lightThread) {
        LightEngineType lightEngineType = LightingConfiguration.lightEngineType;
        LOGGER.info("Initializing " + lightEngineType.name() + " light engine for world " + chunkProvider.getWorldName() + "...");
        LightEngineImpl lightEngine = lightEngineType.create(chunkProvider, hasSkylight, lightThread);
        LOGGER.info("Light engine initialized. ");
        return lightEngine;
    }

}
