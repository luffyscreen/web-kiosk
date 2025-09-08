package org.screenlite.webkiosk.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.screenlite.webkiosk.ui.theme.*

@Composable
fun SettingsField(
    label: String,
    description: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    isError: Boolean = false,
    supportingText: String? = null
) {
    Column(Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isTvDevice()) {
            var isFocused by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused }
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = if (isFocused) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(SurfaceColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, color = Color.Gray)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = TextColorPrimary, fontSize = 16.sp),
                    singleLine = true,
                    cursorBrush = Brush.verticalGradient(colors = listOf(Color.White, Color.White)),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                keyboardOptions = keyboardOptions,
                singleLine = true,
                isError = isError,
                supportingText = {
                    Text(
                        text = supportingText ?: "",
                        color = if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceColor.copy(alpha = 0.5f),
                    unfocusedContainerColor = SurfaceColor.copy(alpha = 0.5f),
                    focusedIndicatorColor = PrimaryColor,
                    unfocusedIndicatorColor = Color.Gray,
                    cursorColor = PrimaryColor,
                    errorCursorColor = ErrorColor,
                    errorIndicatorColor = ErrorColor,
                )
            )
        }
    }
}
