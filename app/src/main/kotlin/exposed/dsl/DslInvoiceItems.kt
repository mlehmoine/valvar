package exposed.dsl

import org.jetbrains.exposed.sql.Table

object DslInvoiceItems : Table("invoice_items") {
    val id = integer("id").autoIncrement()
    val invoiceId = reference("invoice_id", DslInvoices.id)
    val description = varchar("description", 255)
    val quantity = integer("quantity")
    val unitPrice = decimal("unit_price", 10, 2)
    override val primaryKey = PrimaryKey(id)
}
