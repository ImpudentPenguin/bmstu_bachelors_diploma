package me.elenamakeeva.routing

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*
import me.elenamakeeva.routing.modules.MainModule
import me.elenamakeeva.routing.views.main

fun main() = application {
    val trayState = rememberTrayState()
    val module by remember {
        mutableStateOf(MainModule(trayState))
    }

    Tray(
        state = trayState,
        icon = painterResource("car.png")
    )

    Window(onCloseRequest = ::exitApplication, title = "Логистическая система") {
        MaterialTheme {
            main(module)
        }
    }
}
