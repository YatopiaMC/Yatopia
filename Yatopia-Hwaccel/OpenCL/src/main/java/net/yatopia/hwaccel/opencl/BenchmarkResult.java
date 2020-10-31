package net.yatopia.hwaccel.opencl;


import net.yatopia.hwaccel.utils.GlueList;

public class BenchmarkResult {
    long CalculateAverage(GlueList list) {
        long sum = 0;
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                sum += (long) list.get(i);
            }
            return sum / list.size();
        }
        return sum;
    }
}
