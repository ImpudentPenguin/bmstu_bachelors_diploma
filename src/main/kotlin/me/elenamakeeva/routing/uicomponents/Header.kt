package me.elenamakeeva.routing.uicomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun header(vararg values: Pair<String, Float>) {
    Row(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
            .height(30.dp)
            .background(color = colorPrimaryLight)
            .padding(4.dp)
    ) {
        values.forEachIndexed { index, (value, weight) ->
            title(value, weight)

            if (index != values.size - 1)
                verticalSeparator()
        }
    }
}

@Composable
fun RowScope.title(value: String, weight: Float = 1f) {
    Text(value, modifier = Modifier.weight(weight), textAlign = TextAlign.Center)
}