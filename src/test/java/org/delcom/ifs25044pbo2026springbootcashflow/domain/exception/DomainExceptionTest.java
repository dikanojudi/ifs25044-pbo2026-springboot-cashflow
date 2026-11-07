package org.delcom.ifs25044pbo2026springbootcashflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class DomainExceptionTest {

    @Test
    void domainExceptionKeepsMessageAndCause() {
        Throwable cause = new RuntimeException("sebab");
        DomainException withCause = new DomainException("gagal", cause);

        assertEquals("gagal", new DomainException("gagal").getMessage());
        assertEquals("gagal", withCause.getMessage());
        assertSame(cause, withCause.getCause());
    }

    @Test
    void entityNotFoundExceptionCarriesId() {
        EntityNotFoundException ex = new EntityNotFoundException(7);

        assertEquals(7, ex.getId());
        assertEquals("Transaksi dengan ID 7 tidak ditemukan.", ex.getMessage());
        assertInstanceOf(DomainException.class, ex);
    }

    @Test
    void validationExceptionIsADomainException() {
        ValidationException ex = new ValidationException("tidak valid");

        assertEquals("tidak valid", ex.getMessage());
        assertInstanceOf(DomainException.class, ex);
    }
}
