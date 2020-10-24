package net.yatopia.hwaccel.opencl;

import net.yatopia.hwaccel.utils.GlueList;

public class CPUBenchmarkResult extends BenchmarkResult{
    private final long timing;

    CPUBenchmarkResult(GlueList timing) {
        this.timing = CalculateAverage(timing);
    }

    public long getTiming() {
        return timing;
    }

}
