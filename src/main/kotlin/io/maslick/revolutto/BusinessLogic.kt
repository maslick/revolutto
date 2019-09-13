package io.maslick.revolutto

import org.slf4j.LoggerFactory
import java.math.BigDecimal

interface ITransaction {
    suspend fun transfer(from: String, to: String, amount: BigDecimal): Boolean
}

interface IBalance {
    suspend fun getBalance(userId: String): BigDecimal
}

class Transaction(private val repo: IRepo) : ITransaction {
    @Synchronized
    override suspend fun transfer(from: String, to: String, amount: BigDecimal): Boolean {
        if (from == to) return false
        if (!repo.withdraw(from, amount)) return false
        return repo.deposit(to, amount).also {
            log.debug("Sent $amount EUR from $from to $to")
        }
    }
}

class Balance(private val repo: IRepo) : IBalance {
    override suspend fun getBalance(userId: String) = repo.getBalance(userId)
}

val log = LoggerFactory.getLogger("revolutto")!!