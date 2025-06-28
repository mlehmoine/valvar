package mapper

import dao.DaoInvoiceItem
import dto.InvoiceItemDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "default")
interface InvoiceItemMapper {
    @Mapping(source = "id.value", target = "id")
    fun toDto(entity: DaoInvoiceItem): InvoiceItemDto

    // You would also add a toEntity method here if you need to map in the other direction
}