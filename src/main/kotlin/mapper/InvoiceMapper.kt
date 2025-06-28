package mapper

import dao.DaoInvoice
import dao.DaoInvoiceItem
import dao.DaoInvoicesTable
import dto.InvoiceDto
import dto.InvoiceItemDto
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import java.lang.Integer

@Mapper(componentModel = "default", uses = [InvoiceItemMapper::class])
interface InvoiceMapper {
    @Mapping(source = "id", target = "id", qualifiedByName = ["toEntityId"])
    fun toEntity(dto: InvoiceDto): DaoInvoice

    @Mapping(source = "id.value", target = "id")
    // 1. Explicitly delegate the 'items' mapping to our new helper method
    @Mapping(source = "items", target = "items", qualifiedByName = ["mapSizedIterableToList"])
    fun toDto(entity: DaoInvoice): InvoiceDto

    @Named("toEntityId")
    fun toEntityId(id: Integer): EntityID<Int> = EntityID(id.toInt(), DaoInvoicesTable)

    // 2. Declare an abstract method to handle the collection mapping.
    //    MapStruct will generate the implementation for this method,
    //    using the InvoiceItemMapper provided in the `uses` clause.
    @Named("mapSizedIterableToList")
    fun mapSizedIterableToList(items: SizedIterable<DaoInvoiceItem>): List<InvoiceItemDto>
}