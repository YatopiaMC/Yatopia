package net.yatopia.hwaccel.lighting.structures;

import java.util.Objects;

public abstract class Chunk { // TODO add api

    public final ChunkPosition chunkPosition;

    public Chunk(ChunkPosition chunkPosition) {
        this.chunkPosition = chunkPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return chunkPosition.equals(chunk.chunkPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunkPosition);
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "chunkPosition=" + chunkPosition +
                '}';
    }
}
