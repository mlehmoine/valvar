import dto.UserDto
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import repository.UserDtoRepositoryExt

fun main() {
    DatabaseFactory.init()

    transaction {
        addLogger(StdOutSqlLogger)

        val newUser = UserDto(
            name = "John wick",
            email = "john@wick.com"
        )

        val userRepository = UserDtoRepositoryExt()
        userRepository.create(newUser)

        println("All users:")
        userRepository.findAll().forEach { user ->
            println("User: ${user.name}, Email: ${user.email}")
        }
    }
}

