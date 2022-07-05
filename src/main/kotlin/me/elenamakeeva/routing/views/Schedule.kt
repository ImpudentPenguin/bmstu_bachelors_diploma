package me.elenamakeeva.routing.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.elenamakeeva.routing.models.Route
import me.elenamakeeva.routing.modules.MainModule
import me.elenamakeeva.routing.uicomponents.emptyState
import me.elenamakeeva.routing.uicomponents.header
import me.elenamakeeva.routing.uicomponents.horizontalSeparator
import java.time.format.DateTimeFormatter

@Composable
fun schedule(module: MainModule) {
    LazyColumn(modifier = Modifier.padding(vertical = 10.dp)) {
        item {
            header(
                "Наименование" to 1f,
                "Время начала маршрута" to 1f
            )
        }

        items(module.futureRoutes.value) { route ->
            scheduleItem(route)
            horizontalSeparator()
        }

        if (module.futureRoutes.value.isEmpty()) {
            item {
                emptyState("Новые заявки отсутствуют")
            }
        }
    }
}

@Composable
private fun scheduleItem(route: Route) {
    Row(modifier = Modifier.padding(vertical = 10.dp)) {
        Text("Машина ${route.carId}", modifier = Modifier.weight(1f).padding(start = 4.dp))
        Text(
            route.date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
            modifier = Modifier.weight(1f).padding(start = 4.dp)
        )
    }
}