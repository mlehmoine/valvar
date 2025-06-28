package exposed.dao

import org.jetbrains.exposed.dao.id.LongIdTable

object DaoUsersTable : LongIdTable("users") {
    val name = varchar("name", 255)
    val email = varchar("email", 255)
}
