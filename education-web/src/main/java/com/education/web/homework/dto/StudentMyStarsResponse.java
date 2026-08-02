package com.education.web.homework.dto;

import java.util.List;
import java.util.Map;

/** Зірки учня з оцінених домашніх робіт (БД). */
public record StudentMyStarsResponse(
        /** {@code sum} або {@code average} — з налаштувань школи. */
        String gradingMethod,
        double totalStars,
        double weekGain,
        double monthGain,
        List<SubjectStarTotalRow> subjectTotals,
        List<String> chartMonthLabels,
        Map<String, List<Double>> starsBySubjectChartSeries,
        List<StarRewardLogRow> rewardLog,
        List<SubjectHomeworkProgressRow> subjectHomeworkProgress,
        /** {@code DAY} or {@code MONTH} — як зібрані {@link #chartMonthLabels()}. */
        String starsChartGranularity
) {
}
