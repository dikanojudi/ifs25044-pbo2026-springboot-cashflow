package org.delcom.ifs25044pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private final TransactionRepository repository = new TransactionRepository();

    @Test
    void saveIncomeAndExpenseAssignIncrementingIds() {
        Transaction income = repository.saveIncome("Gaji", new BigDecimal("100.00"));
        Transaction expense = repository.saveExpense("Makan", new BigDecimal("20.00"));

        assertEquals(1, income.getId());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(2, expense.getId());
        assertEquals(TransactionType.EXPENSE, expense.getType());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        repository.saveIncome("Gaji", new BigDecimal("100.00"));

        List<Transaction> copy = repository.findAll();
        copy.clear();

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findByIdAndDeleteByIdWork() {
        Transaction saved = repository.saveIncome("Gaji", new BigDecimal("100.00"));

        assertTrue(repository.findById(saved.getId()).isPresent());
        assertFalse(repository.findById(99).isPresent());

        assertTrue(repository.deleteById(saved.getId()));
        assertFalse(repository.deleteById(saved.getId()));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void findByFiltersWithPredicate() {
        repository.saveIncome("Gaji", new BigDecimal("100.00"));
        repository.saveExpense("Makan", new BigDecimal("20.00"));

        assertEquals(1, repository.findBy(Transaction::isIncome).size());
    }

    @Test
    void updateKeepsMutatedObjectByReference() {
        Transaction saved = repository.saveIncome("Gaji", new BigDecimal("100.00"));
        saved.changeDescription("Bonus");

        repository.update(saved);

        assertEquals("Bonus", repository.findById(saved.getId()).orElseThrow().getDescription());
    }
}
