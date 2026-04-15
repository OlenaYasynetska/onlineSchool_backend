-- Дозволити довші коди груп (унікальний ідентифікатор у межах школи), напр. описові підписи.
ALTER TABLE school_groups
  MODIFY COLUMN code VARCHAR(255) NOT NULL;
