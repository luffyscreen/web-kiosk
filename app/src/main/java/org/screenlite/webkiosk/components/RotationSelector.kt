package org.screenlite.webkiosk.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.screenlite.webkiosk.data.Rotation
import org.screenlite.webkiosk.ui.theme.isTvDevice

@Composable
fun RotationSelector(rotation: Rotation, onRotationChange: (Rotation) -> Unit) {
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
            Rotation.entries.forEach { rotationOption ->
                OutlinedButton(
                    onClick = { onRotationChange(rotationOption) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (rotation == rotationOption)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else Color.Transparent
                    )
                ) {
                    Text("${rotationOption.degrees}°")
                }
            }
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val rotationOptions = listOf(
                listOf(Rotation.ROTATION_0, Rotation.ROTATION_90),
                listOf(Rotation.ROTATION_180, Rotation.ROTATION_270)
            )
            rotationOptions.forEach { rowAngles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowAngles.forEach { rotationOption ->
                        OutlinedButton(
                            onClick = { onRotationChange(rotationOption) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (rotation == rotationOption)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else Color.Transparent
                            )
                        ) {
                            Text("${rotationOption.degrees}°")
                        }
                    }
                }
            }
        }
    }
}