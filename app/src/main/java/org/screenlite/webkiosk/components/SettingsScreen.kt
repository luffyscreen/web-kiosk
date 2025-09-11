package org.screenlite.webkiosk.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.screenlite.webkiosk.R
import org.screenlite.webkiosk.data.KioskSettingsFactory
import org.screenlite.webkiosk.data.Rotation
import org.screenlite.webkiosk.service.StayOnTopService
import org.screenlite.webkiosk.ui.theme.isTvDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val kioskSettings = remember { KioskSettingsFactory.get(context) }
    var checkIntervalSeconds by remember { mutableStateOf("") }
    var kioskUrl by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var rotation: Rotation by remember { mutableStateOf(Rotation.ROTATION_0) }

    LaunchedEffect(Unit) {
        checkIntervalSeconds = (kioskSettings.getCheckInterval().first() / 1000).toString()
        kioskUrl = kioskSettings.getStartUrl().first()
        rotation = kioskSettings.getRotation().first()
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 48.dp, vertical = 8.dp)
                .widthIn(max = 550.dp)
                .let { if (!isTvDevice()) it.imePadding() else it }
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(24.dp))

            SettingsField(
                label = stringResource(R.string.settings_kiosk_url_label),
                description = stringResource(R.string.settings_kiosk_url_desc),
                value = kioskUrl,
                onValueChange = { kioskUrl = it },
                placeholder = stringResource(R.string.settings_kiosk_url_placeholder),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri)
            )

            Spacer(modifier = Modifier.height(32.dp))

            SettingsField(
                label = stringResource(R.string.settings_check_interval_label),
                description = stringResource(R.string.settings_check_interval_desc),
                value = checkIntervalSeconds,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("\\d*"))) {
                        checkIntervalSeconds = newValue
                        errorMessage = null
                    }
                },
                placeholder = stringResource(R.string.settings_check_interval_placeholder),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                isError = errorMessage != null,
                supportingText = errorMessage ?: stringResource(R.string.settings_check_interval_supporting)
            )

            Spacer(modifier = Modifier.height(32.dp))

            RotationSelector(
                rotation = rotation,
                onRotationChange = { rotation = it }
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
            ) {
                FocusableButton(
                    text = stringResource(R.string.button_cancel),
                    onClick = { (context as? ComponentActivity)?.finish() },
                    background = MaterialTheme.colorScheme.surface,
                )

                FocusableButton(
                    text = stringResource(R.string.button_save),
                    onClick = {
                        if (checkIntervalSeconds.isBlank()) {
                            errorMessage = context.getString(R.string.settings_check_interval_empty)
                            return@FocusableButton
                        }
                        val seconds = checkIntervalSeconds.toLongOrNull()
                        if (seconds == null || seconds !in 1..99999) {
                            errorMessage = context.getString(R.string.settings_check_interval_invalid)
                            return@FocusableButton
                        }

                        (context as? ComponentActivity)?.lifecycleScope?.launch {
                            kioskSettings.setCheckInterval(seconds * 1000L)
                            kioskSettings.setStartUrl(kioskUrl)
                            kioskSettings.setRotation(rotation)

                            StayOnTopService.restart(context)
                        }
                        (context as? ComponentActivity)?.finish()
                    },
                    background = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
