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
   fun doBilling(): Int{
       
       //fetch invoices that have status pending one by one
        val pendingInvoice = invoiceService.fetchPending()
        
        if(pendingInvoice==null){
            return 3
        }
        // do validations of invoice and customer
        invoiceService.updateStatus(pendingInvoice!!.id, InvoiceStatus.INPROGRESS)
        return this.billInvoice(pendingInvoice)
   }

    fun billInvoice(invoice: Invoice): Int{
        val invoiceId = invoice.id
        try{
            // call payment provider
            if(paymentProvider.charge(invoice) ){
                invoiceService.updateStatus(invoice.id, InvoiceStatus.PAID)
                logger.info { "Billing successful for Invoice Id $invoiceId" }
                println("Billing successful for Invoice Id $invoiceId")
                return 1 
            } else{
                invoiceService.updateStatus(invoice.id, InvoiceStatus.FAILED)
                logger.info { "Billing failed for Invoice Id $invoiceId" } 
                println("Billing failed for Invoice Id $invoiceId")
                return 2
            }
        } catch(e: Exception){
            logger.error(e) { "Payment for invoice $invoiceId failed" }
            invoiceService.updateStatus(invoice.id, InvoiceStatus.FAILED)
            throw e
        }
        
    }
}