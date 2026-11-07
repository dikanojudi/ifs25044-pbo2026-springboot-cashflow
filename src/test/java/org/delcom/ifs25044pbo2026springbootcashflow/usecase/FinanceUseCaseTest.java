package org.delcom.ifs25044pbo2026springbootcashflow.usecase;

import org.delcom.ifs25044pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void addIncomeAndExpenseUpdateBalance() throws Exception {
        useCase.addIncome("Gaji", "5000000");
        useCase.addExpense("Makan", "25000.50");

        assertEquals(2, useCase.getAllTransactions().size());
        assertEquals(new BigDecimal("5000000.00"), useCase.getTotalIncome());
        assertEquals(new BigDecimal("25000.50"), useCase.getTotalExpense());
        assertEquals(new BigDecimal("4974999.50"), useCase.getBalance());
    }

    @Test
    void emptyUseCaseHasZeroBalance() {
        assertEquals(new BigDecimal("0.00"), useCase.getBalance());
    }

    @Test
    void descriptionIsTrimmedAndAmountRoundedToTwoDecimals() throws Exception {
        Transaction tx = useCase.addIncome("  Gaji  ", " 10.005 ");

        assertEquals("Gaji", tx.getDescription());
        assertEquals(new BigDecimal("10.01"), tx.getAmount());
    }

    @Test
    void blankDescriptionIsRejectedForIncomeAndExpense() {
        assertThrows(ValidationException.class, () -> useCase.addIncome("  ", "10"));
        assertThrows(ValidationException.class, () -> useCase.addExpense(null, "10"));
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void invalidAmountIsRejected() {
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", null));
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", "   "));
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", "abc"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x", "0"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x", "-5"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x", "0.001"));
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void validationMessagesAreClear() {
        ValidationException notNumber =
                assertThrows(ValidationException.class, () -> useCase.addIncome("x", "abc"));
        ValidationException notPositive =
                assertThrows(ValidationException.class, () -> useCase.addIncome("x", "-1"));

        assertEquals("Jumlah harus berupa angka", notNumber.getMessage());
        assertEquals("Jumlah harus lebih besar dari 0", notPositive.getMessage());
    }

    @Test
    void removeTransactionDeletesAndKeepsBalanceConsistent() throws Exception {
        Transaction income = useCase.addIncome("Gaji", "100");
        useCase.addExpense("Makan", "30");

        useCase.removeTransaction(income.getId());

        assertEquals(1, useCase.getAllTransactions().size());
        assertEquals(new BigDecimal("-30.00"), useCase.getBalance());
    }

    @Test
    void removeWithInvalidIdThrowsValidationException() throws Exception {
        useCase.addIncome("Gaji", "100");

        ValidationException zero = assertThrows(ValidationException.class, () -> useCase.removeTransaction(0));
        assertThrows(ValidationException.class, () -> useCase.removeTransaction(-3));

        assertEquals("ID tidak valid", zero.getMessage());
        assertEquals(1, useCase.getAllTransactions().size());
    }

    @Test
    void removeUnknownIdThrowsEntityNotFound() {
        EntityNotFoundException ex =
                assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(99));

        assertEquals(99, ex.getId());
    }

    @Test
    void searchIsCaseInsensitiveOnDescriptionAndDoesNotChangeData() throws Exception {
        useCase.addIncome("Gaji Bulanan", "100");
        useCase.addExpense("Makan Siang", "10");

        assertEquals(1, useCase.searchTransactions("GAJI").size());
        assertEquals(0, useCase.searchTransactions("zzz").size());
        assertEquals(2, useCase.searchTransactions(null).size());
        assertEquals(2, useCase.getAllTransactions().size());
    }

    @Test
    void sortUsesDomainSortOption() throws Exception {
        useCase.addExpense("Kecil", "10");
        useCase.addIncome("Besar", "90");

        List<Transaction> asc = useCase.sortTransactions(SortOption.AMOUNT_ASC);
        List<Transaction> incomeFirst = useCase.sortTransactions(SortOption.INCOME_FIRST);

        assertEquals("Kecil", asc.get(0).getDescription());
        assertEquals("Besar", incomeFirst.get(0).getDescription());
    }
}
