package dto

import exposed.dsl.DslUsersTable
import ksp.GenerateRepository

@GenerateRepository(DslUsersTable::class)
data class UserDto(
    val id: Long? = null,
    val name: String,
    val email: String
)
