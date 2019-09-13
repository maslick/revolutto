package io.maslick.revolutto

import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Executors.callable
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KtorExperimentalAPI
class IntegrationTest {

    private val gson = Gson()

    @Test
    fun `checking users balance`() = withTestApplication(Application::test) {
        val initialBalanceDaisy = getBalance("daisy")
        val initialBalanceScrooge = getBalance("scrooge")
        assertTrue(initialBalanceDaisy > 0)
        assertTrue(initialBalanceScrooge > 0)
    }

    @Test
    fun `sending X eur from Scrooge to Donald == OK`() = withTestApplication(Application::test) {
        val initialBalanceScrooge = getBalance("scrooge")
        val initialBalanceDonald = getBalance("donald")
        val amount = 25.0

        val result = transferMoney(from = "scrooge", to = "donald", amount = amount)
        assertEquals(true, result)

        assertEquals(initialBalanceScrooge - amount, getBalance("scrooge"))
        assertEquals(initialBalanceDonald + amount, getBalance("donald"))
    }

    @Test
    fun `sending X eur from Donald to Daisy == not enough funds`() = withTestApplication(Application::test) {
        val initialBalanceDonald = getBalance("donald")
        val initialBalanceDaisy = getBalance("daisy")
        val amount = initialBalanceDonald + 0.01

        val result = transferMoney(from = "donald", to = "daisy", amount = amount)
        assertEquals(false, result)

        assertEquals(initialBalanceDonald, getBalance("donald"))
        assertEquals(initialBalanceDaisy, getBalance("daisy"))
    }

    @Test
    fun `transfer money concurrently in 1 eur transactions`() = withTestApplication(Application::test) {
        val initialBalanceScrooge = getBalance("scrooge")
        val initialBalanceDonald = getBalance("donald")
        val amount = 1.0

        val pool = Executors.newFixedThreadPool(4)
        val tasks = 1.rangeTo(10_000).map {
            callable { transferMoney(from = "scrooge", to = "donald", amount = amount) }
        }
        pool.invokeAll(tasks)

        assertEquals(initialBalanceScrooge + initialBalanceDonald, getBalance("donald"))
        assertEquals(0.0, getBalance("scrooge"))
    }

    @Test
    fun `transfer money concurrently to each other in small amounts`() = withTestApplication(Application::test) {
        val initialBalanceScrooge = getBalance("scrooge")
        val initialBalanceGyro = getBalance("gyro")

        val numberOfTransactions = 10_000
        val divider = 2
        val amount1 = initialBalanceScrooge / numberOfTransactions
        val amount2 = initialBalanceGyro / numberOfTransactions / divider

        val pool = Executors.newFixedThreadPool(4)
        val tasks = arrayListOf<Callable<Any>>()

        for (i in 1..numberOfTransactions) {
            tasks.add(callable { transferMoney(from = "scrooge", to = "gyro", amount = amount1) })
            tasks.add(callable { transferMoney(from = "gyro", to = "scrooge", amount = amount2) })
        }
        pool.invokeAll(tasks)

        assertEquals(initialBalanceGyro - amount2*numberOfTransactions + amount1*numberOfTransactions, getBalance("gyro"))
        assertEquals(initialBalanceScrooge + amount2*numberOfTransactions - amount1*numberOfTransactions, getBalance("scrooge"))
        assertEquals(initialBalanceGyro + initialBalanceScrooge, getBalance("gyro") + getBalance("scrooge"))
    }

    @Test
    fun `test health endpoint`() = withTestApplication(Application::test) {
        assertEquals(HttpStatusCode.OK, handleRequest(HttpMethod.Get, "v1/health").response.status())
    }

    private fun TestApplicationEngine.getBalance(userId: String): Double {
        return handleRequest(HttpMethod.Get, "v1/$userId/balance").response.let {
            assertEquals(HttpStatusCode.OK, it.status())
            gson.fromJson<Map<String, Double>>(it.content, mapType)["balance"]
        } ?: throw Exception("error :(")
    }

    private fun TestApplicationEngine.transferMoney(from: String, to: String, amount: Double): Boolean {
        return handleRequest(HttpMethod.Post, "v1/transfer") {
            addHeader("Content-Type", "application/json")
            setBody(generateJson(from, to, amount))
        }.response.let {
            val resp = gson.fromJson<Map<String, Boolean>>(it.content, mapType)
            assertEquals(HttpStatusCode.OK, it.status())
            resp["success"]
        } ?: throw Exception("error :(")
    }

    private fun generateJson(from: String, to: String, amount: Double) =
        """{"from": "$from", "to": "$to", "amount": $amount}"""
}