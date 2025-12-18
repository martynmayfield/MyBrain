package com.mhss.app.mybrain.di

import android.util.Log
import com.mhss.app.mybrain.dataStore
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val platformModule = module {
    single { androidContext().dataStore }
    single { OkHttp.create() }
    single<Logger> { AndroidHttpLogger() }
    single(named("applicationScope")) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}

class AndroidHttpLogger: Logger {
    override fun log(message: String) {
        Log.i("Ktor", message)
    }
}