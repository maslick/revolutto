package io.maslick.revolutto

import io.ktor.application.call
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import org.koin.ktor.ext.inject


data class BalanceReq(val from: String, val to: String, val amount: Double)

fun Route.businessLogicRoutes() {
    val transactionService by inject<ITransaction>()
    val balanceService by inject<IBalance>()

    post("v1/transfer") {
        val req = call.receiveOrNull<BalanceReq>() ?: throw IllegalArgumentException("Not enough input data")
        val result = transactionService.transfer(req.from, req.to, req.amount)
        call.respond(mapOf(
            "success" to result,
            "from" to req.from,
            "to" to req.to,
            "amount" to req.amount
        ))
    }

    get("v1/{username}/balance") {
        val userId = call.parameters["username"] ?: throw IllegalArgumentException("User id not specified")
        val userBalance = balanceService.getBalance(userId)
        call.respond(mapOf(
            "balance" to userBalance,
            "username" to userId
        ))
    }
}

