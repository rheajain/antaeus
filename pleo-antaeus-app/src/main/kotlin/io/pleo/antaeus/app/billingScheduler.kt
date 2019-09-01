package io.pleo.antaeus.app

import java.util.Date
import java.util.Calendar
import java.util.Timer
import kotlin.concurrent.*
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.exceptions.NetworkException
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

// Start scheduler
internal fun startScheduler(billingService: BillingService): Unit {
    val currentDate = Date()
    var currentMonth = currentDate.month
    var nextMonth: Int
    var currentYear = currentDate.year
    var nextYear = currentDate.year
    val currentYearOffset = 1900
    if(currentMonth == 11){
        nextMonth = 0
        nextYear = currentYear + 1
    } else {
        nextMonth = currentMonth + 1
    }
    setScheduler(nextYear + currentYearOffset, nextMonth, billingService)
}

fun setScheduler(year: Int, month: Int, billingService: BillingService): Unit{
    var calendar = Calendar.getInstance()
    calendar.set(year, month, 1, 0, 0) //using UTC time zone by default
    val nextDate = calendar.time
    
    val t = Timer()
    t.schedule( time=nextDate){
        logger.info { "Billing for invoices starting" }
        startBilling(billingService)
    }
}

fun startBilling (billingService: BillingService): Unit {

    //call doBilling function
    try{
        do {
            var status = billingService.doBilling()
        } while (status!=3)
        logger.info { "All pending invoices handled" }
    } catch (ne: NetworkException){
        logger.error(ne) { "Network exception occurred. Start billing manually again once connection is fixed." }
    } catch (e: Exception) {
        logger.error(e) { "Exception occurred while billing." }
        startBilling(billingService)
    }
        
    startScheduler(billingService)
}