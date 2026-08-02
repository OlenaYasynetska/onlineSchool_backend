package com.education.web.homework.dto;

/** Загальний показник за предметом (sum або average — залежить від {@code gradingMethod} школи). */
public record SubjectStarTotalRow(String subject, double starsTotal) {
}
