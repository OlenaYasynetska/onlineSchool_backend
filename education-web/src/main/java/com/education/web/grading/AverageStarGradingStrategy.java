package com.education.web.grading;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AverageStarGradingStrategy implements StarGradingStrategy {

    @Override
    public double aggregateValues(List<Integer> values) {
        if (values.isEmpty()) {
            return 0;
        }
        int sum = values.stream().mapToInt(v -> v != null ? v : 0).sum();
        return StarGradingMath.roundOneDecimal((double) sum / values.size());
    }

    @Override
    public double aggregateSumAndCount(int sum, int count) {
        if (count <= 0) {
            return 0;
        }
        return StarGradingMath.roundOneDecimal((double) sum / count);
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
            List<Double> runningAverage = new ArrayList<>();
            int cumulativeSum = 0;
            int cumulativeCount = 0;
            for (int bi = 0; bi < bucketCount; bi++) {
                cumulativeSum += bucketSums[si][bi];
                cumulativeCount += bucketCounts[si][bi];
                if (cumulativeCount <= 0) {
                    runningAverage.add(0.0);
                } else {
                    runningAverage.add(
                            StarGradingMath.roundOneDecimal((double) cumulativeSum / cumulativeCount)
                    );
                }
            }
            chartSeries.put(subjectKeys.get(si), runningAverage);
        }
        return chartSeries;
    }
}
