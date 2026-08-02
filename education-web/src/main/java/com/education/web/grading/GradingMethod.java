package com.education.web.grading;

import java.util.Locale;

/** Спосіб агрегації зірок з оцінених ДЗ (налаштування школи). */
public enum GradingMethod {
    SUM("sum"),
    AVERAGE("average");

    private final String wireValue;

    GradingMethod(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }

    public static GradingMethod fromWire(String value) {
        if (value == null || value.isBlank()) {
            return SUM;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "average" -> AVERAGE;
            default -> SUM;
        };
    }
}
