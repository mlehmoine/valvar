package mapper

import dto.UserDto
import exposed.dao.DaoUserEntity
import exposed.dao.DaoUsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named

@Mapper(componentModel = "default") // Use "spring" if using Spring
interface UserMapper {
    @Mapping(source = "id.value", target = "id") // Map EntityID<Long> to Long
    fun toDto(entity: DaoUserEntity): UserDto

    @Mapping(source = "id", target = "id", qualifiedByName = ["toEntityId"]) // Map Long to EntityID<Long>
    fun toEntity(dto: UserDto): DaoUserEntity

    @Named("toEntityId")
    fun toEntityId(id: Long?): EntityID<Long> = EntityID(id!!, DaoUsersTable)
}