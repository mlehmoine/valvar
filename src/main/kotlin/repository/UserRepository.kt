package repository

import dto.UserDto
import exposed.dao.DaoUserEntity
import exposed.dao.DaoUsersTable
import mapper.UserMapper
import org.jetbrains.exposed.sql.SizedIterable

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

    /**
     * Updates an existing user with values from a DTO.
     * Must be called within a transaction.
     * @return Updated UserDto or null if the user was not found.
     */
    fun update(dto: UserDto): UserDto? {

        /* TODO:
         *  There is a downside to this approach:
         *  It takes one query to pull the entity by ID,
         *  and then another query to update it.
         */

        val entity = DaoUserEntity.findById(dto.id) ?: return null
        userMapper.updateEntity(dto, entity)
        return userMapper.toDto(entity)
    }

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