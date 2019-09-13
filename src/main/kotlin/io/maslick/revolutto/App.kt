package io.maslick.revolutto

import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.config.HoconApplicationConfig
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import org.koin.Logger.SLF4JLogger
import org.koin.ktor.ext.Koin


@KtorExperimentalAPI fun main() {
    embeddedServer(Netty, applicationEngineEnvironment {
        connector { port = conf.property("server.port").getString().toInt() }
        module(Application::main)
    }).start(wait = true)
}

@KtorExperimentalAPI fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) { gson() }
    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "internal server error")
        }
    }
    install(Koin) {
        SLF4JLogger()
        loadDIConfiguration()
    }
    routing { businessLogicRoutes() }
}

@KtorExperimentalAPI val conf = HoconApplicationConfig(ConfigFactory.load())
