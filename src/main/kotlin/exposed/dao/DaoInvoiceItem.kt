package dao

import dto.InvoiceItemDto
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class DaoInvoiceItem(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DaoInvoiceItem>(DaoInvoiceItemsTable)

    var invoice by DaoInvoice referencedOn DaoInvoiceItemsTable.invoice
    var description by DaoInvoiceItemsTable.description
    var quantity by DaoInvoiceItemsTable.quantity
    var unitPrice by DaoInvoiceItemsTable.unitPrice

    fun toDTO(): InvoiceItemDto = InvoiceItemDto(
        id = id.value,
        invoiceId = invoice.id.value,
        description = description,
        quantity = quantity,
        unitPrice = unitPrice
    )
}
