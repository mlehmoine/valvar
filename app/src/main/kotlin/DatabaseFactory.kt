import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import exposed.dsl.DslUsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        // Connect to the database and initialize the schema
        val db = connectToPostgres()

        // The transaction block is how you execute queries in Exposed.
        // Here, we use it to create the tables if they don't exist.
        transaction(db) {
            // Print the SQL logs to the console, which is very useful for debugging.
            addLogger(StdOutSqlLogger)

            println("Initializing database schema...")
            //Create the tables based on your Table objects
            SchemaUtils.create(DslUsersTable)
            println("Schema initialized.")
        }
    }

    private fun connectToPostgres(): Database {
        // HikariCP is a high-performance JDBC connection pool.
        // Exposed's Database.connect() can take a DataSource directly.
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = "jdbc:postgresql://localhost:5432/valvardb" // Your DB URL
            username = "valvar" // Your DB user
            password = "valvarpw" // Replace with your actual password
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(config)
        return Database.connect(dataSource)
    }
}