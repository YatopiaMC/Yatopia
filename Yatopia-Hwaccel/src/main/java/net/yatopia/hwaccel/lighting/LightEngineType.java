package net.yatopia.hwaccel.lighting;

import net.yatopia.hwaccel.lighting.structures.ChunkProvider;

import java.util.concurrent.ExecutorService;

public enum LightEngineType {

    VANILLA() {

        @Override
        public LightEngineImpl create(ChunkProvider chunkProvider, boolean hasSkylight, ExecutorService lightThread) {
            return VanillaLightingHook.constructor.create(chunkProvider, hasSkylight, lightThread);
        }
    };

    public abstract LightEngineImpl create(ChunkProvider chunkProvider,
                                           boolean hasSkylight,
                                           ExecutorService lightThread);

}
