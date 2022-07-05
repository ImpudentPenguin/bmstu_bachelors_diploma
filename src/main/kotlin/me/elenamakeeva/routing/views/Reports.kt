package me.elenamakeeva.routing.views

import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.*
import androidx.compose.ui.unit.dp
import me.elenamakeeva.routing.modules.MainModule
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import me.elenamakeeva.routing.models.Node
import me.elenamakeeva.routing.models.Request
import me.elenamakeeva.routing.models.Route
import me.elenamakeeva.routing.uicomponents.*
import me.elenamakeeva.routing.utils.Constants.FORMATTER
import java.time.format.DateTimeFormatter

@Composable
fun reports(module: MainModule) {
    LazyColumn(modifier = Modifier.padding(vertical = 10.dp)) {
        item {
            header(
                "Наименование" to 1f,
                "Дата доставки" to 1f,
                "Отчет" to 2f
            )
        }

        items(module.finishedRoutes.value + module.oldRoutes.value) { route ->
            reportItem(route)
            horizontalSeparator()
        }

        if (module.finishedRoutes.value.isEmpty() && module.oldRoutes.value.isEmpty()) {
            item {
                emptyState("Новые отчеты отсутствуют")
            }
        }
    }
}

@Composable
private fun reportItem(route: Route) {
    var visible by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 10.dp)) {
        Row(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
            Text("Машина ${route.carId}")

            if (route.hasExpress)
                Icon(
                    modifier = Modifier.padding(horizontal = 4.dp).size(20.dp).align(Alignment.CenterVertically),
                    painter = painterResource("warning.png"),
                    contentDescription = null
                )
        }

        Text(
            route.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )

        Box(modifier = Modifier.weight(2f), contentAlignment = Alignment.Center) {
            button("Посмотреть отчет") {
                visible = true
            }
        }

        Dialog(
            state = DialogState(width = 800.dp, height = 200.dp),
            visible = visible,
            title = "Пройденный маршрут",
            onCloseRequest = { visible = false },
        ) {
            dialogContent(route)
        }
    }
}

@Composable
private fun dialogContent(route: Route) {
    Column {
        Text(
            "Машина ${route.model} ${route.carId}\n" +
                    "Время старта: ${route.startTime.format(FORMATTER)}\n" +
                    "Дата доставки ${route.date.format(FORMATTER)}",
            modifier = Modifier.padding(top = 24.dp, start = 24.dp),
            color = colorPrimaryDark
        )

        LazyRow(
            modifier = Modifier.padding(top = 10.dp, start = 8.dp, end = 8.dp).height(50.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            items(route.path) { item ->
                TooltipArea(
                    tooltip = {
                        Surface(
                            modifier = Modifier.shadow(4.dp),
                            color = colorLight,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Направление: ${item.direction}\n" +
                                            "Координаты: ${item.placeX}, ${item.placeY}",
                                    modifier = Modifier.padding(10.dp),
                                    style = Typography.subtitle2.copy(color = colorPrimaryDark)
                                )

                                if (item is Request && item.isExpress)
                                    Text(
                                        text = "Экспресс заявка",
                                        modifier = Modifier.padding(10.dp),
                                        style = Typography.caption
                                    )
                            }
                        }
                    }
                ) {
                    node(item)
                }
            }
        }
    }
}

@Composable
private fun node(item: Node) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clip(CircleShape)
            .background(
                if (item.requestId == -1) colorPrimary
                else if (item is Request && item.isExpress) Color.Green
                else if (item.isVisited) colorPrimaryDark
                else colorPrimaryLight
            )
            .padding(4.dp)
    ) {
        Text(
            text = item.requestId.takeIf { it > -1 }?.toString() ?: "Депо",
            fontSize = 8.sp,
            color = if (item.isVisited) Color.White else Color.Unspecified
        )
    }
}