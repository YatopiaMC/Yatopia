package net.yatopia.lighting.api.registry;

import net.yatopia.lighting.api.LightEngineImpl;
import net.yatopia.lighting.api.structures.ChunkProvider;

import java.util.concurrent.ExecutorService;

public interface LightEngineImplProvider {

    LightEngineImpl create(ChunkProvider chunkProvider,
                           boolean hasSkylight,
                           ExecutorService lightThread);

}
