package io.maslick.revolutto

data class Account(val username: String, val balance: Double, val fullname: String)

interface IRepo {
    suspend fun withdraw(username: String, amount: Double): Boolean
    suspend fun deposit(username: String, amount: Double): Boolean
    suspend fun getBalance(username: String): Double
}

class DummyRepo : IRepo {
    private val users = HashMap<String, Account>().apply {
        set("donald", Account("donald", 0.0, "Donald Duck"))
        set("daisy", Account("daisy", 100.0, "Daisy Duck"))
        set("scrooge", Account("scrooge", 10000.0, "Scrooge McDuck"))
    }

    override suspend fun withdraw(username: String, amount: Double): Boolean {
        val user = findUser(username)
        if (user.balance < amount) return false
        users[username] = user.copy(balance = user.balance - amount)
        return true
    }

    override suspend fun getBalance(username: String) = findUser(username).balance
    override suspend fun deposit(username: String, amount: Double) = withdraw(username, -amount)

    private fun findUser(userId: String) =
        users[userId] ?: throw IllegalArgumentException("account with username=$userId not found")
}