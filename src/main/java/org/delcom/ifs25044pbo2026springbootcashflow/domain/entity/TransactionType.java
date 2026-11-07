package org.delcom.ifs25044pbo2026springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan: pemasukan menambah saldo, pengeluaran mengurangi saldo.
 */
public enum TransactionType {
    INCOME("Pemasukan"),
    EXPENSE("Pengeluaran");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    /** Nama jenis transaksi dalam bahasa Indonesia. */
    public String label() {
        return label;
    }
}
