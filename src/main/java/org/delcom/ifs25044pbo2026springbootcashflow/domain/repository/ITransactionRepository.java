package org.delcom.ifs25044pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.Transaction;

import java.math.BigDecimal;

/**
 * Port spesifik transaksi: menambah method simpan pemasukan dan pengeluaran
 * dengan penomoran ID otomatis.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /** Menyimpan transaksi pemasukan baru. */
    Transaction saveIncome(String description, BigDecimal amount);

    /** Menyimpan transaksi pengeluaran baru. */
    Transaction saveExpense(String description, BigDecimal amount);
}
