package org.delcom.ifs25044pbo2026springbootcashflow.domain.exception;

/**
 * Checked exception dasar untuk seluruh hierarki exception domain Cashflow.
 */
public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
