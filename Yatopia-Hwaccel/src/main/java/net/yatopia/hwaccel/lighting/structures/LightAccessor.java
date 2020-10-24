package net.yatopia.hwaccel.lighting.structures;

import net.yatopia.hwaccel.lighting.storage.ChunkLightStorage;

public interface LightAccessor {

    ChunkLightStorage getLightStorage(ChunkPosition chunkPosition);

    byte getLightLevel(BlockPosition blockPosition);

}
