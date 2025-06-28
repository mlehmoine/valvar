package dao

import dto.InvoiceDto
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class DaoInvoice(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DaoInvoice>(DaoInvoicesTable)

    var customerName by DaoInvoicesTable.customerName
    var amount by DaoInvoicesTable.amount
    var status by DaoInvoicesTable.status
    var createdAt by DaoInvoicesTable.createdAt

    // Reference to invoice items
    val items by DaoInvoiceItem referrersOn DaoInvoiceItemsTable.invoice

//    fun toDTO(): InvoiceDto = InvoiceDto(
//        id = id.value,
//        customerName = customerName,
//        amount = amount,
//        status = status,
//        createdAt = createdAt,
//        items = items.map { it.toDTO() }
//    )
//
//    fun updateFromDTO(dto: InvoiceDto) {
//        customerName = dto.customerName
//        amount = dto.amount
//        status = dto.status
//        createdAt = dto.createdAt
//        // Note: Items are not updated here; they should be managed separately.
//    }
}
