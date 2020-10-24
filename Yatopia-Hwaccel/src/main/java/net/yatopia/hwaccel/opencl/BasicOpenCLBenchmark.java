package net.yatopia.hwaccel.opencl;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLEventList;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import net.yatopia.hwaccel.configuration.OpenCLConfiguration;
import net.yatopia.hwaccel.utils.GlueList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.LongBuffer;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static com.jogamp.opencl.CLEvent.ProfilingCommand.END;
import static com.jogamp.opencl.CLEvent.ProfilingCommand.START;

public class BasicOpenCLBenchmark {

    private static final Logger LOGGER = LogManager.getLogger();

    private static int WORK_SIZE = OpenCLConfiguration.openCLTestSize;


    public static CompletableFuture<OpenCLBenchmarkResult> benchmark(OpenCompute openCompute) {
        CompletableFuture<OpenCLBenchmarkResult> future = new CompletableFuture<>();
        GlueList<Long> gpuTimingList = new GlueList<>();
        GlueList<Long> cpuTimingList = new GlueList<>();
        GlueList<Long> latencyTimingList = new GlueList<>();
        final CLBuffer<LongBuffer>[] longBuffer1 = new CLBuffer[]{null};
        final CLBuffer<LongBuffer>[] longBuffer2 = new CLBuffer[]{null};
        final CLBuffer<LongBuffer>[] longBuffer3 = new CLBuffer[]{null};
        try {
            OpenCompute.OPENCL_EXECUTOR.execute(() -> {
                final long[] testData1 = new long[WORK_SIZE];
                final long[] testData2 = new long[WORK_SIZE];
                CLProgram program = openCompute.getProgram("SpeedTest.cl");
                for (int i = 0; i < OpenCLConfiguration.openCLTestPasses; i++) {
                    Random random = new Random();
                    for (int h = 0; h < WORK_SIZE; h++) {
                        testData1[h] = random.nextLong();
                        testData2[h] = random.nextLong();
                    }
                    long startTime = System.nanoTime();
                    CLKernel kernel = null;
                    CLEventList events = null;
                    try {
                        if (longBuffer1[0] == null) {
                            longBuffer1[0] = openCompute.getContext().createLongBuffer(WORK_SIZE);
                        } else {
                            longBuffer1[0].getBuffer().clear();
                        }
                        if (longBuffer2[0] == null) {
                            longBuffer2[0] = openCompute.getContext().createLongBuffer(WORK_SIZE);
                        } else {
                            longBuffer2[0].getBuffer().clear();
                        }
                        if (longBuffer3[0] == null) {
                            longBuffer3[0] = openCompute.getContext().createLongBuffer(WORK_SIZE);
                        } else {
                            longBuffer3[0].getBuffer().clear();
                        }
                        longBuffer1[0].getBuffer().put(testData1);
                        longBuffer2[0].getBuffer().put(testData2);
                        kernel = openCompute.getKernel(program, "test");
                        kernel.putArg(longBuffer1[0])
                                .putArg(longBuffer2[0])
                                .putArg(longBuffer3[0]);
                        CLCommandQueue clCommandQueue = openCompute.getQueue();
                        events = new CLEventList(null, 1);
                        clCommandQueue.put1DRangeKernel(kernel, 0,
                                openCompute.getGlobalSize(WORK_SIZE), openCompute.getLocalSize(), events)
                                .putReadBuffer(longBuffer3[0], true)
                                .putWaitForEvents(events, true);

                        long gpuTiming = events.getEvent(0).getProfilingInfo(END) - events.getEvent(0).getProfilingInfo(START);
                        long cpuTiming = System.nanoTime() - startTime;
                        long latencyTiming = cpuTiming - gpuTiming;
                        gpuTimingList.add(gpuTiming);
                        cpuTimingList.add(cpuTiming);
                        latencyTimingList.add(latencyTiming);
                    } finally {
                        if (kernel != null && !kernel.isReleased()) kernel.release();
                        if (events != null && !events.isReleased()) events.release();
                    }
                }

                future.complete(new OpenCLBenchmarkResult(gpuTimingList, cpuTimingList, latencyTimingList, openCompute));
            });
        } catch (Throwable t) {
            future.completeExceptionally(t);
        } finally {
            if (longBuffer1[0] != null && !longBuffer1[0].isReleased()) longBuffer1[0].release();
            if (longBuffer2[0] != null && !longBuffer2[0].isReleased()) longBuffer2[0].release();
            if (longBuffer3[0] != null && !longBuffer3[0].isReleased()) longBuffer3[0].release();
        }

        return future;
    }

}
