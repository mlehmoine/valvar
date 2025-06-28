package exposed.dsl

import org.jetbrains.exposed.sql.Table

object DslUsersTable : Table("users") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100)
    val email = varchar("email", 100)
    override val primaryKey = PrimaryKey(id)
}