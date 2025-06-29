package dto

import java.math.BigDecimal

data class InvoiceItemDto(
    val id: Int? = null,
    val invoiceId: Int,
    val description: String,
    val quantity: Int,
    val unitPrice: BigDecimal
)
