package services

import dao.DaoInvoice
import dto.InvoiceDto
import dto.InvoiceStatus
import exposed.dsl.DslInvoices
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant

class InvoiceService {

    fun demonstrateInserts() {
        // Create an invoice dto.  Let's keep it simple.
        // No invoices.

        val newInvoice = dto.InvoiceDto(
            customerName = "John Doe",
            amount = BigDecimal("100.00"),
            status = dto.InvoiceStatus.Pending,
            createdAt = Instant.now(),
            items = emptyList() // No items for now
        )

        transaction {
            addLogger(StdOutSqlLogger)
            // insert the invoice into the database

            DaoInvoice.new {
                customerName = newInvoice.customerName
                amount = newInvoice.amount
                status = newInvoice.status
                createdAt = newInvoice.createdAt
            }
        }

        // query for objects where customerName is "John Doe"
        transaction {
            addLogger(StdOutSqlLogger)
            val invoices = DaoInvoice.find { DslInvoices.customerName eq "John Doe" }
            invoices.forEach { invoice ->
                println("Found invoice: ${invoice.id}, Customer: ${invoice.customerName}, Amount: ${invoice.amount}, Status: ${invoice.status}, Created At: ${invoice.createdAt}")
            }
        }
    }

    fun demonstrateUpdates() {
        // Update an invoice
        transaction {
            addLogger(StdOutSqlLogger)
            val invoice = DaoInvoice.findById(1)

            invoice?.let {
                it.status = dto.InvoiceStatus.Paid
                println("Updated invoice: ${it.id}, Customer: ${it.customerName}, Amount: ${it.amount}, Status: ${it.status}, Created At: ${it.createdAt}")
            }
        }
    }

}