package repository

import dto.UserDto
import exposed.dsl.DslUsersTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.updateReturning

class UserRepository() {
    /**
     * Retrieves a user by ID and returns it as a DTO.
     * Must be called within a transaction.
     * @return UserDto or null if not found.
     */
    fun findById(id: Long): UserDto? {
        val resultRow = DslUsersTable.select(DslUsersTable.id eq id).singleOrNull()

        // 4. If a row was found, map it to a DTO.
        return resultRow?.let {
            toUserDto(it)
        }
    }

    /**
     * Retrieves all users as DTOs.
     * Must be called within a transaction.
     * @return List of UserDto.
     */
    fun findAll(): List<UserDto> {
        return DslUsersTable.selectAll().map { toUserDto(it) }
    }

    /**
     * Creates a new user from a DTO and returns the created DTO.
     * Must be called within a transaction.
     * @return Created UserDto.
     */
    fun create(dto: UserDto): UserDto {
        val resultRow = DslUsersTable.insertReturning {
            it[name] = dto.name
            it[email] = dto.email
        }.singleOrNull() ?: error("Failed to create user")

        return toUserDto(resultRow)
    }

    fun update(dto: UserDto): UserDto? {
        // This performs the update and asks the database to return the specified columns for the updated row.
        // .singleOrNull() ensures we get a result only if exactly one row was updated.
        val updatedRow = DslUsersTable
            .updateReturning(where = { DslUsersTable.id eq dto.id }) {
                it[name] = dto.name
                it[email] = dto.email
            }.singleOrNull()

        return updatedRow?.let { toUserDto(it) }
    }

    fun toUserDto(row: ResultRow): UserDto = UserDto(
        id = row[DslUsersTable.id],
        name = row[DslUsersTable.name],
        email = row[DslUsersTable.email]
    )

    /**
     * Deletes a user by ID.
     * Must be called within a transaction.
     * @return true if deleted, false if not found.
     */
    fun delete(id: Long): Boolean {
        val rowDeleted = DslUsersTable.deleteWhere{DslUsersTable.id eq id}
        return rowDeleted > 0
    }
}