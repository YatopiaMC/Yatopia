package net.yatopia.hwaccel.configuration;

import net.yatopia.hwaccel.utils.ConfigSuppliers;

public class OpenCLConfiguration {

    public static boolean useOpenCL = true;
    public static int openCLDeviceOverride = -1;
    public static int openCLTestSize = 8 * 1024 * 1024;
    public static int openCLTestPasses = 4;

    public static void reload() {
        useOpenCL = ConfigSuppliers.booleanConfigSupplier.getBoolean("settings.opencl.enabled", useOpenCL);
        openCLDeviceOverride = ConfigSuppliers.integerConfigSupplier.getInteger("settings.opencl.device-override", openCLDeviceOverride);
        openCLTestSize = ConfigSuppliers.integerConfigSupplier.getInteger("settings.opencl.test-size", openCLTestSize);
        openCLTestPasses = ConfigSuppliers.integerConfigSupplier.getInteger("settings.opencl.test-passes", openCLTestPasses);
    }

}
