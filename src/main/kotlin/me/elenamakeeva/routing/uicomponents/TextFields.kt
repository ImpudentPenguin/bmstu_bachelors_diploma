package me.elenamakeeva.routing.uicomponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun field(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    isError: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
            textAlign = TextAlign.Start,
            style = Typography.body1.copy(color = Color.LightGray)
        )

        TextField(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            isError = isError,
            textStyle = Typography.body1,
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = colorPrimaryLight,
                cursorColor = Color.Black,
                disabledLabelColor = colorPrimaryLight,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun emptyState(title: String) {
    Text(title, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center)
}