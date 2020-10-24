package net.yatopia.hwaccel.lighting.storage;

public interface ChunkLightStorage {

    byte[] getArrayCopy();

    byte[] getSectionCopy(int index);

    void setByte(int index, byte value);

    byte getByte(int index);

    default byte getLight(int x, int y, int z) {
        int index = getIndex(x, y, z);
        int shift = (index & 1) << 2;
        return (byte) (this.getByte(index >> 1) >> shift & 15);
    }

    default void setLight(int x, int y, int z, byte level) {
        int index = getIndex(x, y, z);
        int shift = (index & 1) << 2;
        this.setByte(index >> 1, (byte) (this.getByte(index >> 1) & ~(15 << shift) | (level & 15) << shift));
    }

    default int getIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

}
