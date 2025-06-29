package exposed.dsl

import dto.InvoiceStatus
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object DslInvoices : Table("invoices") {
    val id = integer("id").autoIncrement()
    val customerName = varchar("customer_name", 100)
    val amount = decimal("amount", 10, 2)
    val status = enumerationByName("status", 20, InvoiceStatus::class)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}
