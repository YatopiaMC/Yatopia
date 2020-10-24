package net.yatopia.hwaccel.opencl;

import net.yatopia.hwaccel.utils.GlueList;

public class OpenCLBenchmarkResult extends BenchmarkResult{
    private final long gpuTiming;
    private final long cpuTiming;
    private final long latencyTiming;
    public final OpenCompute openCompute;

    OpenCLBenchmarkResult(GlueList gpuTiming, GlueList cpuTiming, GlueList latencyTiming, OpenCompute openCompute) {
        this.gpuTiming = CalculateAverage(gpuTiming);
        this.cpuTiming = CalculateAverage(cpuTiming);
        this.latencyTiming = CalculateAverage(latencyTiming);
        this.openCompute = openCompute;
    }

    public long getGpuTiming() {
            return gpuTiming;
        }

    public long getCpuTiming() {
            return cpuTiming;
        }

    public long getLatencyTiming() {
            return latencyTiming;
        }

}
