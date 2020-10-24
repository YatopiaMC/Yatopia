/*
 * Glowstone Copyright (C) 2015-2020 The Glowstone Project.
 * Glowstone Copyright (C) 2011-2014 Tad Hardesty.
 * Lightstone Copyright (C) 2010-2011 Graham Edgecombe.
 */

package net.yatopia.hwaccel.opencl;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.llb.impl.CLAbstractImpl;
import net.yatopia.hwaccel.configuration.OpenCLConfiguration;
import net.yatopia.hwaccel.utils.GlueList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class OpenCompute implements Closeable {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ClassLoader CLASS_LOADER = OpenCompute.class.getClassLoader();
    static final ExecutorService OPENCL_EXECUTOR = Executors.newCachedThreadPool(new ThreadFactory() {
        private AtomicLong serial = new AtomicLong(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("YatopiaCL-" + serial.getAndIncrement());
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            thread.setDaemon(true);
            return thread;
        }
    });

    private final CLPlatform platform;
    private final CLContext context;
    private final CLDevice device;
    private final CLCommandQueue queue;
    private ConcurrentHashMap<String, CLProgram> programs;

    private static OpenCompute activeInstance = null;

    public static OpenCompute getActiveInstance() {
        return activeInstance;
    }

    public static synchronized void release() {
        if(activeInstance == null) return;
        LOGGER.info("Yatopia OpenCL: Shutting down");
        activeInstance.close();
        activeInstance = null;
    }

    public CLPlatform getPlatform() {
        return platform;
    }

    public CLContext getContext() {
        return context;
    }

    public CLDevice getDevice() {
        return device;
    }

    public CLCommandQueue getQueue() {
        return queue;
    }

    /**
     * Initializes the {@link CLContext}, {@link CLDevice} and {@link CLCommandQueue} for the given
     * {@link CLPlatform}.
     *
     * @param device the {@link CLDevice} to use
     */
    private OpenCompute(CLDevice device) {
        this(device, false);
    }

    private OpenCompute(CLDevice device, boolean profilingEnabled) {
        CLCommandQueue queue1;

        programs = new ConcurrentHashMap<>();
        platform = device.getPlatform();
        context = CLContext.create(device);
        CLCommandQueue tmpqueue = null;
        this.device = device;
        try {
            if (profilingEnabled)
                tmpqueue = device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE, CLCommandQueue.Mode.OUT_OF_ORDER_MODE);
            else
                tmpqueue = device.createCommandQueue(CLCommandQueue.Mode.OUT_OF_ORDER_MODE);
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize {}", device.getName());
        }

        queue = tmpqueue;
    }

    /**
     * Returns an OpenCL program, loading it synchronously if it's not in cache.
     *
     * @param name the program filename
     * @return the OpenCL program, or null if there isn't a valid program with that name
     */
    public CLProgram getProgram(String name) {
        if (programs.containsKey(name)) {
            return programs.get(name);
        } else {
            if (programs.containsKey(name))
                return programs.get(name);
            synchronized (this) {
                if (programs.containsKey(name))
                    return programs.get(name);
                long startTime = System.nanoTime();
                try (InputStream input = CLASS_LOADER
                        .getResourceAsStream("net/yatopia/hwaccel/opencl/programs/" + name)) {
                    CLProgram program = context.createProgram(input).build();
                    programs.put(name, program);
                    LOGGER.info("OpenCL program [{}] compiled in {}ms", name, (System.nanoTime() - startTime) / 1_000_000);
                    return program;
                } catch (Throwable ex) {
                    LOGGER.log(Level.WARN,
                            "Could not load builtin OpenCL program after {}ms", ex, (System.nanoTime() - startTime) / 1_000_000);
                }
            }
        }
        return null;
    }

    public CLKernel getKernel(CLProgram program, String name) {
        return program.createCLKernel(name);
    }

    /**
     * Initializes OpenCompute API with settings defined in yatopia.yml
     */
    public static synchronized void init() {
        if (OpenCLConfiguration.useOpenCL) {
            LOGGER.info("Initializing OpenCL...");
            if (!CLAbstractImpl.isAvailable()) {
                LOGGER.warn("Your system does not meet the OpenCL requirements for Yatopia OpenCL Acceleration. " +
                        "See if driver updates are available.");
                return;
            }

            CLPlatform[] platforms = CLPlatform.listCLPlatforms();
            LOGGER.debug(String.format("Found %d platforms", platforms.length));
            List<CLDevice> devices = new GlueList<>();
            List<CompletableFuture<OpenCLBenchmarkResult>> deviceBenchmarks = new GlueList<>();
            for (int i = 0; i < platforms.length; i++) {
                CLPlatform platform = platforms[i];
                LOGGER.debug("Platform {}: Vendor string: {} ", i, platform.getVendor());
                LOGGER.debug("Platform {}: Profile string: {}", i, platform.getProfile());
                LOGGER.debug("Platform {}: Version string: {} ", i, platform.getVersion());
                LOGGER.debug("Platform {}: Name string: {} ", i, platform.getName());
                LOGGER.debug("Platform {}: Available extensions: {} ", i, platform.getExtensions());
                CLDevice[] platformDevices = platform.listCLDevices();
                devices.addAll(Arrays.asList(platformDevices));
            }

            LOGGER.info("Found {} devices", devices.size());
            if (devices.isEmpty()) {
                LOGGER.warn("Your system does not meet the OpenCL requirements for Yatopia OpenCL Acceleration. " +
                        "See if driver updates are available.");
                return;
            }

            for (int i = 0, deviceCount = devices.size(); i < deviceCount; i++) {
                CLDevice device = devices.get(i);
                CLPlatform platform = device.getPlatform();
                int platformID = -1;
                for (int j = 0; j < platforms.length; j++)
                    if (platforms[j] == platform) {
                        platformID = j;
                        break;
                    }
                LOGGER.info("Device {}: {} ", i, device.getName());
                LOGGER.debug("Device {} of Platform {}: Version string: {} ", i, platformID, device.getVersion());
                LOGGER.debug("Device {} of Platform {}: C-Version string: {} ", i, platformID, device.getCVersion());
                LOGGER.debug("Device {} of Platform {}: Driver version string: {} ", i, platformID, device.getDriverVersion());
                LOGGER.debug("Device {} of Platform {}: Profile string: {} ", i, platformID, device.getProfile());
                LOGGER.debug("Device {} of Platform {}: Device type: {} ", i, platformID, device.getType());
                LOGGER.debug("Device {} of Platform {}: Available extensions: {}", i, platformID, device.getExtensions());
                LOGGER.debug("Device {} of Platform {}: Execution capabilities: {}", i, platformID, device.getExecutionCapabilities());
            }

            if (OpenCLConfiguration.openCLDeviceOverride < -1 || OpenCLConfiguration.openCLDeviceOverride >= devices.size()) {
                LOGGER.warn("Invalid override in configuration, ignoring");
                OpenCLConfiguration.openCLDeviceOverride = -1;
            }
            if (OpenCLConfiguration.openCLDeviceOverride != -1) {
                LOGGER.info("Overrides detected, using device {}", OpenCLConfiguration.openCLDeviceOverride);
                activeInstance = new OpenCompute(devices.get(OpenCLConfiguration.openCLDeviceOverride));
            } else {
                LOGGER.info("No overrides defined, sorting devices by benchmark results...");
                for (CLDevice device : devices) {
                    OpenCompute openCompute = new OpenCompute(device, true);
                    deviceBenchmarks.add(BasicOpenCLBenchmark.benchmark(openCompute).handle((openCLBenchmarkResult, throwable) -> {
                        if (throwable != null)
                            LOGGER.info("A device failed to benchmark, device information unavailable", throwable);
                        openCompute.close();
                        return openCLBenchmarkResult;
                    }));
                }

                deviceBenchmarks.sort((o1, o2) -> {
                    OpenCLBenchmarkResult openCLBenchmarkResult1 = o1.join();
                    OpenCLBenchmarkResult openCLBenchmarkResult2 = o2.join();
                    int gpuTimingCompare = Long.compare(openCLBenchmarkResult1.getGpuTiming(), openCLBenchmarkResult2.getGpuTiming());
                    int cpuTimingCompare = Long.compare(openCLBenchmarkResult1.getCpuTiming(), openCLBenchmarkResult2.getCpuTiming());
                    if (gpuTimingCompare != 0) return gpuTimingCompare;
                    else return cpuTimingCompare;
                });
                deviceBenchmarks.removeIf(future -> future.join() == null);

                if (deviceBenchmarks.isEmpty()) {
                    LOGGER.warn("Your system does not meet the OpenCL requirements for Yatopia OpenCL Acceleration. " +
                            "See if driver updates are available.");
                    return;
                }

                LOGGER.info("Benchmark results: ");
                for (int i = 0, size = deviceBenchmarks.size(); i < size; i++) {
                    OpenCLBenchmarkResult openCLBenchmarkResult = deviceBenchmarks.get(i).join();
                    LOGGER.info("{}: {}ms compute time, {}ms round trip, {}ms latency",
                            openCLBenchmarkResult.openCompute.getDevice().getName(), openCLBenchmarkResult.getGpuTiming() / 1_000_000.0, openCLBenchmarkResult.getCpuTiming() / 1_000_000.0,
                            openCLBenchmarkResult.getLatencyTiming() / 1_000_000.0);
                    LOGGER.debug("{}: CLPlatform {} on device CLDevice {}",
                            openCLBenchmarkResult.openCompute.getDevice().getName(), openCLBenchmarkResult.openCompute.getPlatform(), openCLBenchmarkResult.openCompute.getDevice());
                }

                CompletableFuture<CPUBenchmarkResult> cpuBenchmark = BasicCPUBenchmark.benchmark();
                double CPUResult = cpuBenchmark.join().getTiming() / 1_000_000.0;
                OpenCLBenchmarkResult fastestdeivce = deviceBenchmarks.get(0).join();
                LOGGER.info("Native CPU: {}ms round trip", CPUResult);
                if (!(fastestdeivce.getCpuTiming() / 1_000_000.0 < CPUResult)) {
                    LOGGER.warn("OpenCL: OpenCL is not faster");
                }
                CLDevice device = fastestdeivce.openCompute.device;
                LOGGER.info("OpenCL: Using {}", device.getName());
                LOGGER.debug("OpenCL: Using CLPlatform {} on device CLDevice {}", device.getPlatform(), device);
                activeInstance = new OpenCompute(device);

            }
        }
    }

    /**
     * Calculates the number of work groups.
     *
     * @param size the total number of local work units
     * @return the number of work groups
     */
    public int getGlobalSize(int size) {
        return getGlobalSize(size, getLocalSize());
    }

    /**
     * Calculates the number of work groups.
     *
     * @param size          the total number of local work units
     * @param localWorkSize the number of local work units per work group
     * @return the number of work groups
     */
    private static int getGlobalSize(int size, int localWorkSize) {
        int globalSize = size;
        int r = globalSize % localWorkSize;
        if (r != 0) {
            globalSize += localWorkSize - r;
        }
        return globalSize;
    }

    /**
     * Calculates the number of local work units per work group.
     *
     * @return the size of the work groups
     */
    public int getLocalSize() {
        return Math.min(device.getMaxWorkGroupSize(), 256);
    }

    /**
     * Calculates the number of local work units per work group, applying a specified maximum.
     *
     * @param max the maximum size allowed
     * @return the size of the work groups
     */
    public int getLocalSize(int max) {
        return Math.min(device.getMaxWorkGroupSize(), max);
    }

    /**
     * Static de-initializer. Clears all references to {@link CLProgram}, {@link CLKernel} and
     * {@link CLContext} instances.
     */
    @Override
    public synchronized void close() {
        programs.clear();
        programs = null;
        context.release();
    }
}
