package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {
   // TODO - Add code e.g. here
   fun doBilling(): String{
       //fetch invoices that have status pending one by one
        val pendingInvoice = invoiceService.fetchPending()
        
        // do validations of invoice and customer
        invoiceService.updateStatus(pendingInvoice!!.id, InvoiceStatus.INPROGRESS)
        return this.billInvoice(pendingInvoice)
   }

    fun billInvoice(invoice: Invoice): String{
        val invoiceId = invoice.id
        try{
            // call payment provider
            if(paymentProvider.charge(invoice) ){
                invoiceService.updateStatus(invoice.id, InvoiceStatus.PAID)
                return "Billing successful for Invoice Id $invoiceId"
            } else{
                invoiceService.updateStatus(invoice.id, InvoiceStatus.PENDING)
                return "Billing failed for Invoice Id $invoiceId"
            }
        } catch(e: Exception){
            logger.error(e) { "Payment for invoice $invoiceId failed" }
            invoiceService.updateStatus(invoice.id, InvoiceStatus.PENDING)
            return "Billing failed"
        }
        
    }
}