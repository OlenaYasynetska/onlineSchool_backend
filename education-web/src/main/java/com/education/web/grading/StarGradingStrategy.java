package com.education.web.grading;

import java.util.List;
import java.util.Map;

/**
 * Strategy: агрегація зірок (sum / average) без дублювання логіки в сервісах.
 */
public interface StarGradingStrategy {

    double aggregateValues(List<Integer> values);

    double aggregateSumAndCount(int sum, int count);

    Map<String, List<Double>> buildChartSeries(List<String> subjectKeys, int[][] bucketSums, int[][] bucketCounts);
}
