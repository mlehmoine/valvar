package repository

import dto.UserDto
import dto.UserDtoRepository
import exposed.dsl.DslUsersTable
import org.jetbrains.exposed.sql.selectAll

class UserDtoRepositoryExt : UserDtoRepository() {
    fun findByEmail(email: String): UserDto? {
        return table.selectAll()
            .where { DslUsersTable.email eq email }
            .singleOrNull()?.let { toDto(it) }
    }
}