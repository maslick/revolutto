package io.maslick.revolutto

import io.ktor.util.KtorExperimentalAPI
import org.koin.core.KoinApplication
import org.koin.dsl.bind
import org.koin.dsl.module


val businessLogicModule = module {
    single<ITransaction> { Transaction(get()) }
    single<IBalance> { Balance(get()) }
}

val dbModule = module {
    single<IRepo> { DummyRepo() } bind IRepo::class
}

val diModules = listOf(businessLogicModule, dbModule)
@KtorExperimentalAPI fun KoinApplication.loadDIConfiguration() = modules(diModules)