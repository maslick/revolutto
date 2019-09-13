package io.maslick.revolutto

import com.google.gson.reflect.TypeToken
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import org.koin.ktor.ext.Koin

@KtorExperimentalAPI
fun Application.test() {
    install(DefaultHeaders)
    install(ContentNegotiation) { gson() }
    install(Koin) {
        loadDIConfiguration()
    }
    routing { businessLogicRoutes() }
}

val mapType = object : TypeToken<HashMap<String, Any>>() {}.type!!