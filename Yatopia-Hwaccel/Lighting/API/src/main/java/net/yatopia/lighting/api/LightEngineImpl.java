package net.yatopia.lighting.api;

import net.yatopia.lighting.api.structures.BlockPosition;
import net.yatopia.lighting.api.structures.Chunk;
import net.yatopia.lighting.api.structures.ChunkPosition;
import net.yatopia.lighting.api.structures.ChunkSectionPosition;
import net.yatopia.lighting.api.structures.LightAccessor;
import net.yatopia.lighting.api.structures.LightType;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface LightEngineImpl extends Closeable {

    int doLightUpdates(int maxUpdateCount, boolean doSkylight, boolean skipEdgeLightPropagation);

    void addLightSource(BlockPosition pos, int level);

    void checkBlock(BlockPosition pos);

    void updateChunkStatus(ChunkPosition pos);

    void setSectionStatus(ChunkSectionPosition pos, boolean notReady);

    void setColumnEnabled(ChunkPosition pos, boolean lightEnabled);

    void enqueueSectionData(LightType lightType, ChunkSectionPosition pos, @Nullable byte[] nibbles, boolean flag);

    void setRetainData(ChunkPosition pos, boolean retainData);

    CompletableFuture<Void> lightChunk(Chunk chunk, boolean excludeBlocks);

    LightAccessor get(LightType lightType);

    int getLight(BlockPosition pos, int ambientDarkness);

}
