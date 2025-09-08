package org.screenlite.webkiosk.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FocusableButton(
    text: String,
    onClick: () -> Unit,
    background: Color,
) {
    var isFocused by remember { mutableStateOf(false) }

    Button(
        onClick = onClick,
        modifier = Modifier
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (isFocused) Modifier.border(
                    width = 3.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) else Modifier
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp))
    }
}
