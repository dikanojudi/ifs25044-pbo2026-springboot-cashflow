package org.delcom.ifs25044pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.Transaction;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter: mengubah data domain menjadi teks siap tampil. Format uang,
 * baris transaksi, saldo, dan pesan notifikasi dipusatkan di sini.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Memformat uang, contoh: 1000000 menjadi "Rp 1,000,000.00". */
    public String formatMoney(BigDecimal amount) {
        String text = String.format(Locale.US, "Rp %,.2f", amount.abs());
        return amount.signum() < 0 ? "-" + text : text;
    }

    /** Memformat satu transaksi: "[id] keterangan | Rp 0.00 | [Jenis]". */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                transaction.getType().label()
        );
    }

    /** Memformat waktu pencatatan transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat saldo, contoh: "Saldo: Rp 4,975,000.00". */
    public String formatBalance(BigDecimal balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Keterangan singkat kondisi saldo, agar tidak hanya mengandalkan warna. */
    public String formatBalanceStatus(BigDecimal balance) {
        if (balance.signum() > 0) {
            return "Pemasukan lebih besar dari pengeluaran";
        }
        if (balance.signum() < 0) {
            return "Pengeluaran lebih besar dari pemasukan";
        }
        return "Pemasukan dan pengeluaran seimbang";
    }

    /** Label opsi pengurutan untuk ditampilkan di UI. */
    public String formatSortOption(SortOption option) {
        return switch (option) {
            case AMOUNT_ASC -> "Jumlah terkecil";
            case AMOUNT_DESC -> "Jumlah terbesar";
            case INCOME_FIRST -> "Pemasukan dulu";
            case EXPENSE_FIRST -> "Pengeluaran dulu";
        };
    }

    /** Memformat daftar transaksi menjadi teks multi-baris. */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        if (transactions.isEmpty()) {
            return header + "\n" + emptyMessage;
        }
        return header + "\n" + transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
    }

    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", "- Belum ada transaksi!");
    }

    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"",
                "- Transaksi tidak ditemukan!");
    }

    public String showAddSuccess(Transaction transaction) {
        return "Berhasil mencatat " + transaction.getType().label().toLowerCase() + ": "
                + format(transaction);
    }

    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
