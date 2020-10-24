package net.yatopia.hwaccel.lighting.storage;

import com.google.common.base.Preconditions;

import java.util.Arrays;

public class ByteArrayChunkStorage implements ChunkLightStorage {

    final byte[] array;

    public ByteArrayChunkStorage() {
        array = new byte[32768];
        Arrays.fill(array, (byte) 0x00);
    }

    public ByteArrayChunkStorage(byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 32768);
        array = bytes.clone();
    }

    @Override
    public byte[] getArrayCopy() {
        return array.clone();
    }

    @Override
    public byte[] getSectionCopy(int index) {
        return Arrays.copyOfRange(array, index << 11, (index + 1) << 11);
    }

    @Override
    public void setByte(int index, byte value) {
        array[index] = value;
    }

    @Override
    public byte getByte(int index) {
        return array[index];
    }
}
