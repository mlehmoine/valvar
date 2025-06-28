package repository

import dto.UserDto
import exposed.dao.DaoUserEntity
import exposed.dao.DaoUsersTable
import exposed.dsl.DslUsersTable
import mapper.UserMapper
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.updateReturning

class UserRepository(private val userMapper: UserMapper) {
    /**
     * Retrieves a user by ID and returns it as a DTO.
     * Must be called within a transaction.
     * @return UserDto or null if not found.
     */
    fun findById(id: Long): UserDto? {
        val entity = DaoUserEntity.findById(id)
        return entity?.let { userMapper.toDto(it) }
    }

    /**
     * Retrieves all users as DTOs.
     * Must be called within a transaction.
     * @return List of UserDto.
     */
    fun findAll(): List<UserDto> {
        return DaoUserEntity.all().map { userMapper.toDto(it) }
    }

    /**
     * Creates a new user from a DTO and returns the created DTO.
     * Must be called within a transaction.
     * @return Created UserDto.
     */
    fun create(dto: UserDto): UserDto {
        val entity = DaoUserEntity.new {
            name = dto.name
            email = dto.email
        }
        return userMapper.toDto(entity)
    }

    fun update(dto: UserDto): UserDto? {
        // This performs the update and asks the database to return the specified columns for the updated row.
        // .singleOrNull() ensures we get a result only if exactly one row was updated.
        val updatedRow = DslUsersTable
            .updateReturning(where = { DaoUsersTable.id eq dto.id }) {
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
        val entity = DaoUserEntity.findById(id) ?: return false
        entity.delete()
        return true
    }
}