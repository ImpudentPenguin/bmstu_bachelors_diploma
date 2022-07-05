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

@Composable
fun status(module: MainModule) {
    LazyColumn(modifier = Modifier.padding(vertical = 10.dp)) {
        item {
            header(
                "Наименование" to 1f,
                "Статус" to 1f
            )
        }

        items(module.currentRoutes.value) { route ->
            statusItem(route)
            horizontalSeparator()
        }

        if (module.currentRoutes.value.isEmpty()) {
            item {
                emptyState("Новые маршруты отсутствуют")
            }
        }
    }
}

@Composable
private fun statusItem(route: Route) {
    Row(modifier = Modifier.padding(vertical = 10.dp)) {
        Text("Машина ${route.carId}", modifier = Modifier.weight(1f).padding(start = 4.dp))
        Text(
            checkState(route.path.all { node -> node.isVisited }),
            modifier = Modifier.weight(1f).padding(start = 4.dp)
        )
    }
}

private fun checkState(isAllVisited: Boolean): String {
    return  if (!isAllVisited) "В пути"
    else "Завершил"
}