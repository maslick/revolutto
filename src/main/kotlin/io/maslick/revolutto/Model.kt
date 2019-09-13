package io.maslick.revolutto

import java.math.BigDecimal

data class Account(val username: String, val balance: BigDecimal, val fullname: String)

interface IRepo {
    suspend fun withdraw(username: String, amount: BigDecimal): Boolean
    suspend fun deposit(username: String, amount: BigDecimal): Boolean
    suspend fun getBalance(username: String): BigDecimal
}

class DummyRepo : IRepo {
    private val users = HashMap<String, Account>().apply {
        set("donald", Account("donald", BigDecimal(0), "Donald Duck"))
        set("daisy", Account("daisy", BigDecimal(100), "Daisy Duck"))
        set("scrooge", Account("scrooge", BigDecimal(10000), "Scrooge McDuck"))
        set("gyro", Account("gyro", BigDecimal(10000), "Gyro Gearloose"))
    }

    override suspend fun withdraw(username: String, amount: BigDecimal): Boolean {
        val user = findUser(username)
        if (user.balance < amount) return false
        users[username] = user.copy(balance = user.balance - amount)
        return true
    }

    override suspend fun getBalance(username: String) = findUser(username).balance
    override suspend fun deposit(username: String, amount: BigDecimal) = withdraw(username, -amount)

    private fun findUser(userId: String) =
        users[userId] ?: throw IllegalArgumentException("account with username=$userId not found")
}