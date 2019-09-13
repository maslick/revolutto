package io.maslick.revolutto

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
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
        val daisysInitialBalance = balance.getBalance("daisy")
        val scroogesInitialBalance = balance.getBalance("scrooge")
        val result = transaction.transfer("scrooge", "daisy", scroogesInitialBalance)

        assertTrue(result)
        assertEquals(daisysInitialBalance + scroogesInitialBalance, balance.getBalance("daisy"))
        assertEquals(0.0, balance.getBalance("scrooge"))
    }

    @Test
    @ObsoleteCoroutinesApi
    fun `Scrooge donates everything to Daisy in N concurrent transactions`() = runBlocking {
        val daisysInitialBalance = balance.getBalance("daisy")
        val scroogesInitialBalance = balance.getBalance("scrooge")

        val jobs = 1.rangeTo(scroogesInitialBalance.toInt()).map {
            launch {
                assertTrue(transaction.transfer("scrooge", "daisy", 1.0))
            }
        }
        jobs.forEach { job -> job.join() }

        assertEquals(daisysInitialBalance + scroogesInitialBalance, balance.getBalance("daisy"))
        assertEquals(0.0, balance.getBalance("scrooge"))
    }

    @Test
    fun `transfer money to themselves`() = runBlocking {
        assertFalse(transaction.transfer("scrooge", "scrooge", 1.0))
    }
}