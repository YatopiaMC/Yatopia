package net.yatopia.hwaccel.opencl;


import net.yatopia.hwaccel.utils.GlueList;

public class BenchmarkResult {
    long CalculateAverage(GlueList<Long> list) {
        long sum = 0;
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                sum += list.get(i);
            }
            return sum / list.size();
        }
        return sum;
    }
}
