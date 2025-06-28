package dao

import org.jetbrains.exposed.dao.id.IntIdTable

object DaoInvoiceItemsTable : IntIdTable("invoice_items") {
    val invoice = reference("invoice_id", DaoInvoicesTable)
    val description = varchar("description", 255)
    val quantity = integer("quantity")
    val unitPrice = decimal("unit_price", 10, 2)
}
