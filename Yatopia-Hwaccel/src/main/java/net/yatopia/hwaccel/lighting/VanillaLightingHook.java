package net.yatopia.hwaccel.lighting;

import net.yatopia.hwaccel.lighting.structures.ChunkProvider;

import java.util.concurrent.ExecutorService;

public class VanillaLightingHook {

    public static LightEngineConstructor constructor = new DefaultConstructor();

    public interface LightEngineConstructor {

        LightEngineImpl create(ChunkProvider chunkProvider,
                               boolean hasSkylight,
                               ExecutorService lightThread);

    }

    public static class DefaultConstructor implements LightEngineConstructor {
        @Override
        public LightEngineImpl create(ChunkProvider chunkProvider, boolean hasSkylight, ExecutorService lightThread) {
            throw new IllegalStateException("No vanilla lighting engine constructor found");
        }
    }

}
