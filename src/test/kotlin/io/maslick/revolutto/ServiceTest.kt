package io.maslick.revolutto

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
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

class ServiceTest : AutoCloseKoinTest() {
    private val transaction by inject<ITransaction>()
    private val balance by inject<IBalance>()

    @Before
    fun before() {
        startKoin { modules(diModules) }
    }

    @Test
    fun `Scrooge donates everything to Daisy in 1 transaction`() = runBlocking {
        val initialBalanceDaisy = balance.getBalance("daisy")
        val initialBalanceScrooge = balance.getBalance("scrooge")
        val result = transaction.transfer("scrooge", "daisy", initialBalanceScrooge)

        assertTrue(result)
        assertEquals(initialBalanceDaisy + initialBalanceScrooge, balance.getBalance("daisy"))
        assertEquals(BigDecimal.ZERO, balance.getBalance("scrooge"))
    }

    @Test
    @ObsoleteCoroutinesApi
    fun `Scrooge donates everything to Daisy in N concurrent transactions`() = runBlocking {
        val initialBalanceDaisy = balance.getBalance("daisy")
        val initialBalanceScrooge = balance.getBalance("scrooge")

        val jobs = 1.rangeTo(initialBalanceScrooge.toInt()).map {
            launch {
                assertTrue(transaction.transfer("scrooge", "daisy", BigDecimal.ONE))
            }
        }
        jobs.forEach { job -> job.join() }

        assertEquals(initialBalanceDaisy + initialBalanceScrooge, balance.getBalance("daisy"))
        assertEquals(BigDecimal.ZERO, balance.getBalance("scrooge"))
    }

    @Test
    @ObsoleteCoroutinesApi
    fun `Scrooge and Gyro transfer money to each other concurrently in small transactions`() = runBlocking {
        val initialBalanceScrooge = balance.getBalance("scrooge")
        val initialBalanceGyro = balance.getBalance("gyro")

        val numberOfTransactions = 10_000
        val divider = 2
        val amount1 = (initialBalanceScrooge.toDouble() / numberOfTransactions).toBigDecimal()
        val amount2 = (initialBalanceGyro.toDouble() / numberOfTransactions / divider).toBigDecimal()

        val jobs = 1.rangeTo(numberOfTransactions).map {
            launch {
                assertTrue(transaction.transfer(from = "scrooge", to = "gyro", amount = amount1))
                assertTrue(transaction.transfer(from = "gyro", to = "scrooge", amount = amount2))
            }
        }
        jobs.forEach { job -> job.join() }

        val expectedBalanceGyro = initialBalanceGyro - amount2*numberOfTransactions.toBigDecimal() + amount1*numberOfTransactions.toBigDecimal()
        val expectedBalanceScrooge = initialBalanceScrooge + amount2*numberOfTransactions.toBigDecimal() - amount1*numberOfTransactions.toBigDecimal()
        val expectedSum = expectedBalanceGyro + expectedBalanceScrooge

        assertEquals(0, balance.getBalance("gyro").compareTo(expectedBalanceGyro))
        assertEquals(0, balance.getBalance("scrooge").compareTo(expectedBalanceScrooge))
        assertEquals(0, expectedSum.compareTo(balance.getBalance("gyro") + balance.getBalance("scrooge")))
    }

    @Test
    fun `transfer money to themselves`() = runBlocking {
        assertFalse(transaction.transfer("scrooge", "scrooge", BigDecimal.ONE))
    }
}