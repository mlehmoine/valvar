package dto

import exposed.dsl.DslUsersTable
import ksp.GenerateRepository
import org.jetbrains.exposed.sql.ResultRow
import org.mapstruct.Mapper

@GenerateRepository(DslUsersTable::class)
data class UserDto(
    val id: Long? = null,
    val name: String,
    val email: String
)
