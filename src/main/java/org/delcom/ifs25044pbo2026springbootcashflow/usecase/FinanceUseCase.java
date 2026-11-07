package org.delcom.ifs25044pbo2026springbootcashflow.usecase;

import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Use case pencatatan keuangan. Hanya berisi logika bisnis: tidak ada I/O,
 * tidak ada komponen UI. Kegagalan dilaporkan lewat checked domain exception.
 */
public class FinanceUseCase {
    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    /** Mengambil semua transaksi. */
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    /** Mencatat pemasukan baru setelah input divalidasi. */
    public Transaction addIncome(String description, String amountInput) throws ValidationException {
        String cleanDescription = validateDescription(description);
        BigDecimal amount = parseAmount(amountInput);
        return repository.saveIncome(cleanDescription, amount);
    }

    /** Mencatat pengeluaran baru setelah input divalidasi. */
    public Transaction addExpense(String description, String amountInput) throws ValidationException {
        String cleanDescription = validateDescription(description);
        BigDecimal amount = parseAmount(amountInput);
        return repository.saveExpense(cleanDescription, amount);
    }

    /**
     * Menghapus transaksi berdasarkan ID.
     *
     * @throws ValidationException     jika ID tidak valid (bukan bilangan positif)
     * @throws EntityNotFoundException jika tidak ada transaksi dengan ID tersebut
     */
    public void removeTransaction(int id) throws ValidationException, EntityNotFoundException {
        if (id <= 0) {
            throw new ValidationException("ID tidak valid");
        }
        if (!repository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari transaksi berdasarkan kata kunci pada keterangan (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) {
        String lowerKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        return repository.findBy(tx -> tx.getDescription().toLowerCase().contains(lowerKeyword));
    }

    /** Mengurutkan transaksi sesuai {@link SortOption}. */
    public List<Transaction> sortTransactions(SortOption option) {
        return repository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total seluruh pemasukan. */
    public BigDecimal getTotalIncome() {
        return sumOf(true);
    }

    /** Total seluruh pengeluaran. */
    public BigDecimal getTotalExpense() {
        return sumOf(false);
    }

    /** Saldo saat ini = total pemasukan - total pengeluaran. */
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    private BigDecimal sumOf(boolean income) {
        return repository.findBy(tx -> tx.isIncome() == income).stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    private String validateDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    private BigDecimal parseAmount(String amountInput) throws ValidationException {
        if (amountInput == null || amountInput.trim().isEmpty()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountInput.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        if (amount.signum() <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
        return amount;
    }
}
