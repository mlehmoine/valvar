# Valvar

### This is some cool shit. 

Valvar is a repository layer on top of Exposed that provides the user with database objects with immutable attributes.

## Why did I write this?

When you work with libraries like Exposed or Ktorm, they more or less force you to deal with data objects that have
mutable attributes.  That makes sense if you think about it.  Databases, by their very nature, are mutable.  So,
naturally, the mutability is reflected in the software written to work with databases.  However, mutability can
complicate code.  It's hard to know when an attribute might have changed earlier or might change later.  If it's 
immutable, you know it's not going to change when you least expect it.

I wanted an easy-to-use interface to a database that gave me objects with immutable attributes.  And, I wanted to 
minimize the amount of repetitive boilerplate code.  Yeah, Mapstruct, I'm talking about you. 

## Why is this cool?

 * For very little code, you get a basic CRUD repository for a database table that deals in DTOs with immutable attributes.
 * There is almost no boilerplate code for you to write.
 * It doesn't use generics
 * It doesn't use reflection
 * It doesn't use Mapstruct

## How do you use it?

1. Create a table.

```sql
-- Create the Users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);
```

2. Define an Exposed dsl table object that matches your table.

```kotlin
object DslUsersTable : Table("users") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100)
    val email = varchar("email", 100)
    override val primaryKey = PrimaryKey(id)
}
```
3. Define a DTO

```kotlin
@GenerateRepository(DslUsersTable::class)
data class UserDto(
    val id: Long? = null,
    val name: String,
    val email: String
)
```
Did you notice that annotation, `@@GenerateRepository`?  When the project builds, it will generate a CRUD repository
for the `UserDto` class.  The repository will be named `UserDtoRepository`.  It will have methods
like
 - `findById`
 - `findAll`
 - `create`
 - `update`
 - `delete`

The repository returns `UserDto` objects, which are immutable data transfer objects.  The nasty mutable attributes that
normally come with Exposed are not present in the DTO and are hidden safely behind the repository.

A Ksp annotation processor generates the boilerplate for you behind the scenes.

4. If you need extra methods, you can extend the repository with your own class.  The repository provides a reference to the table
   as `table`, so you can use it to query the table using Exposed DSL.

```kotlin
class UserDtoRepositoryExt : UserDtoRepository() {
    fun findByEmail(email: String): UserDto? {
        return table.selectAll()
            .where { DslUsersTable.email eq email }
            .singleOrNull()?.let { toDto(it) }
    }
}
```
