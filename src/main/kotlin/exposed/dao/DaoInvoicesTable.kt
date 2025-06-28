package dao

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import dto.InvoiceStatus

object DaoInvoicesTable : IntIdTable("invoices") {
    val customerName = varchar("customer_name", 100)
    val amount = decimal("amount", 10, 2)
    val status = enumerationByName("status", 20, InvoiceStatus::class)
    val createdAt = timestamp("created_at")
}
