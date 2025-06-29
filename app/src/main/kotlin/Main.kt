package com.segtax

import dto.UserDto
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import dto.UserDtoRepository
import exposed.dsl.DslUsersTable
import org.jetbrains.exposed.sql.updateReturning

//import dto.UserDtoMapperImpl

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    DatabaseFactory.init()
    //val invoiceService = InvoiceService()
    //invoiceService.demonstrateInserts()
    //invoiceService.demonstrateUpdates()
    transaction {
        addLogger(StdOutSqlLogger)

        val newUser = UserDto(
            name = "John wick",
            email = "john@wick.com"
        )



       // val userMapper = UserDtoMapperImpl()
        //val userRepository = UserDtoRepository()

//        val userEntity = DaoUserEntity.new {
//            name = "John Doe"
//            email = "john@example.com"
//        }

//        val existingUser = DaoUserEntity.findById(1)
//        println("User: ${existingUser?.name}")
//        existingUser!!.email = "foo@foo.com"
    }

    transaction {
        addLogger(StdOutSqlLogger)
//        val updatedUser = DaoUserEntity.findById(1)
//        println("Updated User: ${updatedUser?.name}, Email: ${updatedUser?.email}")
    }
}

