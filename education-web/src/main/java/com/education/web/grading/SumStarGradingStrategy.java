package com.education.web.grading;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SumStarGradingStrategy implements StarGradingStrategy {

    @Override
    public double aggregateValues(List<Integer> values) {
        return values.stream().mapToInt(v -> v != null ? v : 0).sum();
    }

    @Override
    public double aggregateSumAndCount(int sum, int count) {
        return sum;
    }

    @Override
    public Map<String, List<Double>> buildChartSeries(
            List<String> subjectKeys,
            int[][] bucketSums,
            int[][] bucketCounts
    ) {
        Map<String, List<Double>> chartSeries = new LinkedHashMap<>();
        int bucketCount = subjectKeys.isEmpty() || bucketSums.length == 0 ? 0 : bucketSums[0].length;
        for (int si = 0; si < subjectKeys.size(); si++) {
            List<Double> cumulative = new ArrayList<>();
            int run = 0;
            for (int bi = 0; bi < bucketCount; bi++) {
                run += bucketSums[si][bi];
                cumulative.add((double) run);
            }
            chartSeries.put(subjectKeys.get(si), cumulative);
        }
        return chartSeries;
    }
}
