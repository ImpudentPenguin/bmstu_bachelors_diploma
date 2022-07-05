package me.elenamakeeva.routing.views

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.elenamakeeva.routing.modules.MainModule
import me.elenamakeeva.routing.uicomponents.*

@Composable
fun main(module: MainModule) {
    val tabState = remember { mutableStateOf(Tabs.Plan) }
    val scroll = rememberScrollState()

    Column {
        TabRow(
            selectedTabIndex = Tabs.values().toList().indexOf(tabState.value),
            backgroundColor = colorSecondaryDark,
            contentColor = Color.White
        ) {
            Tabs.values().forEach { tab ->
                Tab(
                    text = { Text(tab.title) },
                    selected = tabState.value == tab,
                    onClick = {
                        tabState.value = tab
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .scrollable(scroll, orientation = Orientation.Vertical)
        ) {
            when (tabState.value) {
                Tabs.Plan -> plan(module)
                Tabs.Schedule -> schedule(module)
                Tabs.Status -> status(module)
                Tabs.Reports -> reports(module)
            }
        }
    }
}