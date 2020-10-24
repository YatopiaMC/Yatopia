package net.yatopia.hwaccel.utils;

public class ConfigSuppliers {

    public static BooleanConfigSupplier booleanConfigSupplier;
    public static DoubleConfigSupplier doubleConfigSupplier;
    public static IntegerConfigSupplier integerConfigSupplier;
    public static StringConfigSupplier stringConfigSupplier;

    static {
        DefaultConfigSupplier supplier = new DefaultConfigSupplier();
        booleanConfigSupplier = supplier;
        doubleConfigSupplier = supplier;
        integerConfigSupplier = supplier;
        stringConfigSupplier = supplier;
    }

    public static class DefaultConfigSupplier implements BooleanConfigSupplier, DoubleConfigSupplier, IntegerConfigSupplier, StringConfigSupplier {

        @Override
        public boolean getBoolean(String path, boolean def) {
            throw new UnsupportedOperationException("Config suppliers not initialized yet");
        }

        @Override
        public double getDouble(String path, double def) {
            throw new UnsupportedOperationException("Config suppliers not initialized yet");
        }

        @Override
        public int getInteger(String path, int def) {
            throw new UnsupportedOperationException("Config suppliers not initialized yet");
        }

        @Override
        public String getString(String path, String def) {
            throw new UnsupportedOperationException("Config suppliers not initialized yet");
        }
    }

    public interface BooleanConfigSupplier {
        boolean getBoolean(String path, boolean def);
    }

    public interface DoubleConfigSupplier {
        double getDouble(String path, double def);
    }

    public interface IntegerConfigSupplier {
        int getInteger(String path, int def);
    }

    public interface StringConfigSupplier {
        String getString(String path, String def);
    }

}
