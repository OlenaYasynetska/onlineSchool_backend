package com.education.web.homework;

/**
 * Чи є у здачі реальне вкладення (не «без файлу»).
 */
final class HomeworkAttachmentPolicy {

    private HomeworkAttachmentPolicy() {}

    static boolean hasDownloadableAttachment(HomeworkPortalSubmissionEntity s) {
        String path = s.getStoragePath();
        if (path != null && path.endsWith("/.no-file.txt")) {
            return false;
        }
        String fn = s.getFileName();
        if (fn == null || fn.isBlank()) {
            return false;
        }
        String t = fn.trim();
        if ("(no file)".equals(t)) {
            return false;
        }
        if ("__no_hw_attachment__.txt".equalsIgnoreCase(t) || "no-attachment.txt".equalsIgnoreCase(t)) {
            return false;
        }
        Long sz = s.getFileSizeBytes();
        if (sz != null && sz == 0) {
            return false;
        }
        return true;
    }
}
