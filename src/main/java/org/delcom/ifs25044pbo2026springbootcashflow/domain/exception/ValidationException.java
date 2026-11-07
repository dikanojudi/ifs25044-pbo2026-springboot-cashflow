package org.delcom.ifs25044pbo2026springbootcashflow.domain.exception;

/**
 * Dilempar ketika input gagal memenuhi aturan validasi bisnis.
 */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}
