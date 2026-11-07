package org.delcom.ifs25044pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOptionTest {

    private final Transaction small = new Transaction(1, "A", new BigDecimal("10.00"), TransactionType.EXPENSE);
    private final Transaction big = new Transaction(2, "B", new BigDecimal("90.00"), TransactionType.INCOME);
    private final Transaction mid = new Transaction(3, "C", new BigDecimal("50.00"), TransactionType.INCOME);

    private List<Integer> sortedIds(SortOption option) {
        List<Transaction> list = new ArrayList<>(List.of(small, big, mid));
        list.sort(option.comparator());
        return list.stream().map(Transaction::getId).toList();
    }

    @Test
    void amountAscendingStartsFromSmallest() {
        assertEquals(List.of(1, 3, 2), sortedIds(SortOption.AMOUNT_ASC));
    }

    @Test
    void amountDescendingStartsFromLargest() {
        assertEquals(List.of(2, 3, 1), sortedIds(SortOption.AMOUNT_DESC));
    }

    @Test
    void incomeFirstPutsIncomeBeforeExpense() {
        assertEquals(List.of(2, 3, 1), sortedIds(SortOption.INCOME_FIRST));
    }

    @Test
    void expenseFirstPutsExpenseBeforeIncome() {
        assertEquals(List.of(1, 2, 3), sortedIds(SortOption.EXPENSE_FIRST));
    }
}
