package dto

import java.math.BigDecimal
import java.time.Instant

data class InvoiceDto(
    val id: Int? = null,
    val customerName: String,
    val amount: BigDecimal,
    val status: InvoiceStatus,
    val createdAt: Instant,
    val items: List<InvoiceItemDto> // A list of its immutable items
)