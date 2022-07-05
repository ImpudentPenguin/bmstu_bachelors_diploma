package me.elenamakeeva.routing.uicomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun verticalSeparator() {
    Divider(modifier = Modifier.width(1.dp).fillMaxHeight().background(colorPrimaryLight))
}

@Composable
fun horizontalSeparator() {
    Divider(Modifier.height(1.dp), colorPrimaryLight)
}