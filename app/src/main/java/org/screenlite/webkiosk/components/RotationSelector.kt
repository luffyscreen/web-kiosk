package org.screenlite.webkiosk.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.screenlite.webkiosk.ui.theme.isTvDevice

@Composable
fun RotationSelector(rotation: Int, onRotationChange: (Int) -> Unit) {
    Text(
        text = "Rotation",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
    )

    if (isTvDevice()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(0, 90, 180, 270).forEach { angle ->
                OutlinedButton(
                    onClick = { onRotationChange(angle) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (rotation == angle)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else Color.Transparent
                    )
                ) {
                    Text("$angle°")
                }
            }
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(listOf(0, 90), listOf(180, 270)).forEach { rowAngles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowAngles.forEach { angle ->
                        OutlinedButton(
                            onClick = { onRotationChange(angle) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (rotation == angle)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else Color.Transparent
                            )
                        ) {
                            Text("$angle°")
                        }
                    }
                }
            }
        }
    }
}
