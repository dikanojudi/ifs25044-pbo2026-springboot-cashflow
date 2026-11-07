package org.delcom.ifs25044pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    @Test
    void incomeTransactionHoldsDataAndPositiveSignedAmount() {
        Transaction tx = new Transaction(1, "Gaji", new BigDecimal("1000.00"), TransactionType.INCOME);

        assertEquals(1, tx.getId());
        assertEquals("Gaji", tx.getDescription());
        assertEquals(new BigDecimal("1000.00"), tx.getAmount());
        assertEquals(TransactionType.INCOME, tx.getType());
        assertNotNull(tx.getCreatedAt());
        assertTrue(tx.isIncome());
        assertEquals(new BigDecimal("1000.00"), tx.signedAmount());
    }

    @Test
    void expenseTransactionHasNegativeSignedAmount() {
        LocalDateTime time = LocalDateTime.of(2026, 1, 2, 3, 4);
        Transaction tx = new Transaction(2, "Makan", new BigDecimal("50.00"), TransactionType.EXPENSE, time);

        assertEquals(time, tx.getCreatedAt());
        assertFalse(tx.isIncome());
        assertEquals(new BigDecimal("-50.00"), tx.signedAmount());
    }

    @Test
    void changeMethodsUpdateDescriptionAndAmount() {
        Transaction tx = new Transaction(3, "Lama", new BigDecimal("10.00"), TransactionType.INCOME);

        tx.changeDescription("Baru");
        tx.changeAmount(new BigDecimal("20.00"));

        assertEquals("Baru", tx.getDescription());
        assertEquals(new BigDecimal("20.00"), tx.getAmount());
    }

    @Test
    void transactionTypeHasIndonesianLabels() {
        assertEquals("Pemasukan", TransactionType.INCOME.label());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.label());
    }
}
