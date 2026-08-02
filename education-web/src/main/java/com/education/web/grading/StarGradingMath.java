package com.education.web.grading;

final class StarGradingMath {

    private StarGradingMath() {
    }

    static double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
