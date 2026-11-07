package org.delcom.ifs25044pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();

    private Transaction income() {
        return new Transaction(1, "Gaji", new BigDecimal("1000000.00"), TransactionType.INCOME,
                LocalDateTime.of(2026, 11, 7, 9, 5));
    }

    private Transaction expense() {
        return new Transaction(2, "Makan", new BigDecimal("25000.50"), TransactionType.EXPENSE,
                LocalDateTime.of(2026, 11, 7, 12, 30));
    }

    @Test
    void formatMoneyUsesThousandsSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(new BigDecimal("1000000")));
        assertEquals("Rp 0.00", presenter.formatMoney(BigDecimal.ZERO));
        assertEquals("-Rp 1,234.50", presenter.formatMoney(new BigDecimal("-1234.5")));
    }

    @Test
    void formatTransactionFollowsReferenceFormat() {
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income()));
        assertEquals("[2] Makan | Rp 25,000.50 | [Pengeluaran]", presenter.format(expense()));
    }

    @Test
    void formatBalanceAndCreatedAt() {
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(new BigDecimal("4975000")));
        assertEquals("07/11/2026 09:05", presenter.formatCreatedAt(income()));
    }

    @Test
    void formatBalanceStatusDescribesPositiveNegativeAndZero() {
        assertEquals("Pemasukan lebih besar dari pengeluaran", presenter.formatBalanceStatus(new BigDecimal("1")));
        assertEquals("Pengeluaran lebih besar dari pemasukan", presenter.formatBalanceStatus(new BigDecimal("-1")));
        assertEquals("Pemasukan dan pengeluaran seimbang", presenter.formatBalanceStatus(BigDecimal.ZERO));
    }

    @Test
    void formatSortOptionCoversEveryOption() {
        assertEquals("Jumlah terkecil", presenter.formatSortOption(SortOption.AMOUNT_ASC));
        assertEquals("Jumlah terbesar", presenter.formatSortOption(SortOption.AMOUNT_DESC));
        assertEquals("Pemasukan dulu", presenter.formatSortOption(SortOption.INCOME_FIRST));
        assertEquals("Pengeluaran dulu", presenter.formatSortOption(SortOption.EXPENSE_FIRST));
    }

    @Test
    void listFormattersHandleEmptyAndFilledLists() {
        assertEquals("Daftar Transaksi:\n- Belum ada transaksi!", presenter.showTransactions(List.of()));
        assertEquals("Hasil Pencarian: \"x\"\n- Transaksi tidak ditemukan!",
                presenter.showSearchResults(List.of(), "x"));

        String text = presenter.showTransactions(List.of(income(), expense()));
        assertTrue(text.startsWith("Daftar Transaksi:\n[1] Gaji"));
        assertTrue(text.contains("\n[2] Makan"));
    }

    @Test
    void messagesAreFormattedConsistently() {
        assertEquals("Berhasil mencatat pemasukan: [1] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.showAddSuccess(income()));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 5.", presenter.showRemoveFailed(5));
        assertEquals("[!] Keterangan tidak boleh kosong",
                presenter.showValidationError("Keterangan tidak boleh kosong"));
    }
}
