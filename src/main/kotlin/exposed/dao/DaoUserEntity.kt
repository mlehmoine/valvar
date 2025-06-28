package exposed.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class DaoUserEntity (id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DaoUserEntity>(DaoUsersTable)
    var name by DaoUsersTable.name
    var email by DaoUsersTable.email
}