-- Номер домашнього завдання / вправи (окремо від довільного повідомлення).
ALTER TABLE homework_portal_submissions
  ADD COLUMN homework_number VARCHAR(128) NULL
    COMMENT 'номер ДЗ або вправи (напр. HW_3)'
    AFTER message_text;
