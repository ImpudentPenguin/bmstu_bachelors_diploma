package me.elenamakeeva.routing.utils

import kotlinx.coroutines.*
import java.util.logging.Logger

suspend fun launch(onStart: suspend CoroutineScope.() -> Unit, onComplete: (Long) -> Unit = {}) {
    var start = 0L
    withContext(Dispatchers.IO) {
        launch(CoroutineExceptionHandler { _, throwable ->
            Logger.getGlobal().warning("ERROR in Coroutine")
            throwable.printStackTrace()
        }
        ) {
            start = System.currentTimeMillis()
            onStart()
        }.invokeOnCompletion() { onComplete(System.currentTimeMillis() - start) }
    }
}