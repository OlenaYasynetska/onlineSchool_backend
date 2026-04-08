package com.education.web.homework.dto;

import java.util.List;
import java.util.Map;

/** Зірки учня з оцінених домашніх робіт (БД). */
public record StudentMyStarsResponse(
        int totalStars,
        int weekGain,
        int monthGain,
        List<SubjectStarTotalRow> subjectTotals,
        List<String> chartMonthLabels,
        Map<String, List<Integer>> starsBySubjectChartSeries,
        List<StarRewardLogRow> rewardLog
) {
}
