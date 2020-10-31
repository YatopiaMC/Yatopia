package net.yatopia.hwaccel.opencl;

import net.yatopia.hwaccel.utils.GlueList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class BasicCPUBenchmark {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int WORK_SIZE = OpenCLConfiguration.openCLTestSize;

    public static CompletableFuture<CPUBenchmarkResult> benchmark() {
        CompletableFuture<CPUBenchmarkResult> future = new CompletableFuture<>();
        OpenCompute.OPENCL_EXECUTOR.execute(() -> {
            GlueList<Long> timingList = new GlueList<>();
            final long[] testData1 = new long[WORK_SIZE];
            final long[] testData2 = new long[WORK_SIZE];
            long[] result = new long[WORK_SIZE];
            try {
                long lastTime = Long.MAX_VALUE;
                long thisTime = Long.MIN_VALUE;
                int times = 0;
                int totalTimes = 0;
                while ((Math.abs((lastTime - thisTime) / 1_000_000) >= 2 || times ++ < 5) && totalTimes < 96) {
                    if(Math.abs((lastTime - thisTime) / 1_000_000) >= 2)
                        times = 0;
                    lastTime = thisTime;
                    long startTime = System.nanoTime();
                    run0(testData1, testData2, result);
                    thisTime = System.nanoTime() - startTime;
                    totalTimes ++;
                }
                if(totalTimes == 96)
                    LOGGER.warn("Reached warmup timeout");

                for (int i = 0; i < OpenCLConfiguration.openCLTestPasses; i++) {
                    long duration = run0(testData1, testData2, result);
                    timingList.add(duration);
                }
                future.complete(new CPUBenchmarkResult(timingList));
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    private static long run0(long[] testData1, long[] testData2, long[] result) {
        Random random = new Random();
        for (int h = 0; h < WORK_SIZE; h++) {
            testData1[h] = random.nextLong();
            testData2[h] = random.nextLong();
        }
        long startTime = System.nanoTime();
        for (int h = 0; h < WORK_SIZE; h++) {
            result[h] = (((testData1[h] * h) << 2) + (testData2[h] % 2) / (h + 1) + (h >> 2) / ((h + 1) << 4));
        }
        return System.nanoTime() - startTime;
    }
}
