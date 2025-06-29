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

        println("user with id 1:")
        userRepository.findById(1)?.let { user ->
            println("User: ${user.name}, Email: ${user.email}")
        } ?: println("User not found")

        println("All users:")
        userRepository.findAll().forEach { user ->
            println("User: ${user.name}, Email: ${user.email}")
        }

        val user2 = userRepository.findById(2)!!
        val modifiedUser = user2.copy(name = "Jane")
        userRepository.update(modifiedUser)

        userRepository.delete(2)
    }
}

