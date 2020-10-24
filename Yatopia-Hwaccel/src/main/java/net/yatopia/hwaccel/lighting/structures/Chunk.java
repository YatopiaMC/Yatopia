package net.yatopia.hwaccel.lighting.structures;

public abstract class Chunk { // TODO add api

    final ChunkPosition chunkPosition;

    public Chunk(ChunkPosition chunkPosition) {
        this.chunkPosition = chunkPosition;
    }
}
