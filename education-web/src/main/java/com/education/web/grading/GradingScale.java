package com.education.web.grading;

import java.util.Locale;

/** Шкала виставлення оцінок за домашні роботи. */
public enum GradingScale {
    STARS_1_3("stars_1_3", 1, 3),
    AUSTRIAN_1_5("austrian_1_5", 1, 5);

    private final String wireValue;
    private final int minGrade;
    private final int maxGrade;

    GradingScale(String wireValue, int minGrade, int maxGrade) {
        this.wireValue = wireValue;
        this.minGrade = minGrade;
        this.maxGrade = maxGrade;
    }

    public String wireValue() {
        return wireValue;
    }

    public int minGrade() {
        return minGrade;
    }

    public int maxGrade() {
        return maxGrade;
    }

    public static GradingScale fromWire(String value) {
        if (value == null || value.isBlank()) {
            return STARS_1_3;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "austrian_1_5", "austrian" -> AUSTRIAN_1_5;
            default -> STARS_1_3;
        };
    }

    public void validateGrade(int grade) {
        if (grade < minGrade || grade > maxGrade) {
            throw new IllegalArgumentException(
                    "Grade must be between " + minGrade + " and " + maxGrade + " for scale " + wireValue
            );
        }
    }
}
