package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.Runs
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.Money
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.InvoiceStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows;
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException

class BillingServiceTest {

    val pendingInvoice1 = Invoice(11, 2, Money(29.22.toBigDecimal(), Currency.USD), InvoiceStatus.PENDING)
    val pendingInvoice2 = Invoice(12, 2, Money(29.22.toBigDecimal(), Currency.USD), InvoiceStatus.PENDING) 
    val pendingInvoice3 = Invoice(13, 2, Money(29.22.toBigDecimal(), Currency.USD), InvoiceStatus.PENDING) 

    private val paymentProvider = mockk<PaymentProvider> {
        every { charge(pendingInvoice1) } returns true 
        every { charge(pendingInvoice2) } returns false
        every { charge(pendingInvoice3) } throws CurrencyMismatchException(13, 2)
    }

    private val invoiceService = mockk<InvoiceService> {
        every { updateStatus(any(), any()) } returns 1
    }

    private val billingService = BillingService(paymentProvider = paymentProvider, invoiceService= invoiceService)

    @Test
    fun `Should return success message when PaymentProvider returns charge true`() {
        assertEquals(1, billingService.billInvoice(pendingInvoice1))
    }

    @Test
    fun `Should return failure message when PaymentProvider returns charge false`() {
        assertEquals(2, billingService.billInvoice(pendingInvoice2))
    }

    @Test
    fun `Should say billing failed when an exception is thrown` () {
        assertThrows<CurrencyMismatchException>{
            billingService.billInvoice(pendingInvoice3)
        } 
    }
}