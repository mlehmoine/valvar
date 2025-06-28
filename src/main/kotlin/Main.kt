package com.segtax

import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    DatabaseFactory.init()
    //val invoiceService = InvoiceService()
    //invoiceService.demonstrateInserts()
    //invoiceService.demonstrateUpdates()
    transaction {
        addLogger(StdOutSqlLogger)
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

