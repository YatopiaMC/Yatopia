package net.yatopia.lighting.api.structures;

import net.yatopia.lighting.api.storage.ChunkLightStorage;

public interface LightAccessor {

    ChunkLightStorage getLightStorage(ChunkPosition chunkPosition);

    byte getLightLevel(BlockPosition blockPosition);

}
