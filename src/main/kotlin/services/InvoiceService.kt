package services

import dao.DaoInvoice
import dto.InvoiceDto
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
    }

}