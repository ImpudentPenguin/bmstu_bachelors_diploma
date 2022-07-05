package me.elenamakeeva.routing.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.elenamakeeva.routing.modules.MainModule
import me.elenamakeeva.routing.uicomponents.*

@Composable
fun plan(module: MainModule) {
    val scope = rememberCoroutineScope()
    var loaderState by remember { mutableStateOf(false) }
    var requestMaxState by remember { mutableStateOf("500") }
    var expressMaxState by remember { mutableStateOf("50") }

    Box(modifier = Modifier.padding(vertical = 10.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopStart),
        ) {
            field(
                modifier = Modifier.padding(bottom = 14.dp),
                label = "Количество заявок",
                value = requestMaxState,
                isError = (requestMaxState.toIntOrNull() ?: 0) <= 0,
                onValueChange = { value ->
                    requestMaxState = value
                }
            )

            field(
                modifier = Modifier.padding(bottom = 14.dp),
                label = "Количество получаемых экспресс заявок",
                value = expressMaxState,
                isError = (expressMaxState.toIntOrNull() ?: 0) <= 0,
                onValueChange = { value ->
                    expressMaxState = value
                }
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            button(title = "Получить заявки") {
                scope.launch {
                    loaderState = true
                    module.getRequests(
                        requestsCount = requestMaxState.toIntOrNull() ?: 500,
                        expressRequestsCount = expressMaxState.toIntOrNull() ?: 50
                    )

                    delay(1000)
                    loaderState = false
                    module.isGeneratedRequests.value = true
                }
            }

            button(
                title = "Сформировать маршрут",
                enabled = module.isGeneratedRequests.value
            ) {
                scope.launch {
                    loaderState = true
                    module.generateRoutes {
                        loaderState = false
                    }
                }
            }
        }

        if (module.finishedRoutes.value.isNotEmpty() || module.currentRoutes.value.isNotEmpty()
            || module.futureRoutes.value.isNotEmpty()
        )
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(colorPrimaryLight)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Маршруты для ТС успешно сформированы")
            }

        circularIndeterminateProgressBar(isDisplayed = loaderState)
    }
}