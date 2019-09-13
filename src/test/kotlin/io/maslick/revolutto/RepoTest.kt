package io.maslick.revolutto

import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RepoTest: AutoCloseKoinTest() {
    private val user1 = "scrooge"
    private val user2 = "donald"
    private val repo: IRepo by inject()

    @Before
    fun before() {
        startKoin { modules(diModules) }
    }

    @Test
    fun `deposit 150 eur`() = runBlocking {
        val initialBalance = repo.getBalance(user1)
        val amount = BigDecimal(100)
        val result = repo.deposit(user1, amount)
        assertTrue(result)
        assertEquals(amount + initialBalance, repo.getBalance(user1))
    }

    @Test
    fun `withdraw 100 eur`() = runBlocking {
        val initialBalance = repo.getBalance(user1)
        val amount = BigDecimal(100)
        val result = repo.withdraw(user1, amount)
        assertTrue(result)
        assertEquals(initialBalance - amount, repo.getBalance(user1))
    }

    @Test
    fun `withdraw more than account owner has`() = runBlocking {
        val initialBalance = repo.getBalance(user1)
        val amount = initialBalance + BigDecimal(120)
        val result = repo.withdraw(user1, amount)
        assertFalse(result)
        assertEquals(initialBalance, repo.getBalance(user1))
    }

    @Test
    fun `deposit to a broke account`() = runBlocking {
        assertEquals(BigDecimal(0), repo.getBalance(user2))
        val result = repo.deposit(user2, BigDecimal(200))
        assertTrue(result)
        assertEquals(BigDecimal(200), repo.getBalance(user2))
    }
}