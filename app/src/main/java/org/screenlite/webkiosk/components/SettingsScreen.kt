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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.screenlite.webkiosk.R
import org.screenlite.webkiosk.data.KioskSettingsFactory
import org.screenlite.webkiosk.data.Rotation
import org.screenlite.webkiosk.service.StayOnTopService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val kioskSettings = remember { KioskSettingsFactory.get(context) }

    var kioskUrl by remember { mutableStateOf("") }
    var checkIntervalSeconds by remember { mutableStateOf("") }
    var rotation by remember { mutableStateOf(Rotation.ROTATION_0) }
    var idleTimeout by remember { mutableStateOf("") }
    var idleBrightness by remember { mutableStateOf("") }
    var activeBrightness by remember { mutableStateOf("") }

    var checkIntervalError by remember { mutableStateOf<String?>(null) }
    var idleTimeoutError by remember { mutableStateOf<String?>(null) }
    var idleBrightnessError by remember { mutableStateOf<String?>(null) }
    var activeBrightnessError by remember { mutableStateOf<String?>(null) }

    val tabs = listOf("General", "Display", "Brightness")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        kioskUrl = kioskSettings.getStartUrl().first()
        checkIntervalSeconds = (kioskSettings.getCheckInterval().first() / 1000).toString()
        rotation = kioskSettings.getRotation().first()
        idleTimeout = kioskSettings.getIdleTimeout().first().toString()
        idleBrightness = kioskSettings.getIdleBrightness().first().toString()
        activeBrightness = kioskSettings.getActiveBrightness().first().toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTabIndex, edgePadding = 16.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> GeneralSettingsTab(
                    kioskUrl = kioskUrl,
                    onKioskUrlChange = { kioskUrl = it },
                    checkIntervalSeconds = checkIntervalSeconds,
                    onCheckIntervalChange = { checkIntervalSeconds = it },
                    checkIntervalError = checkIntervalError,
                    onCheckIntervalErrorChange = { checkIntervalError = it }
                )
                1 -> DisplaySettingsTab(
                    rotation = rotation,
                    onRotationChange = { rotation = it },
                    idleTimeout = idleTimeout,
                    onIdleTimeoutChange = { idleTimeout = it },
                    idleTimeoutError = idleTimeoutError,
                    onIdleTimeoutErrorChange = { idleTimeoutError = it }
                )
                2 -> BrightnessSettingsTab(
                    idleBrightness = idleBrightness,
                    activeBrightness = activeBrightness,
                    onIdleBrightnessChange = { idleBrightness = it },
                    onActiveBrightnessChange = { activeBrightness = it },
                    idleBrightnessError = idleBrightnessError,
                    activeBrightnessError = activeBrightnessError,
                    onIdleBrightnessErrorChange = { idleBrightnessError = it },
                    onActiveBrightnessErrorChange = { activeBrightnessError = it }
                )
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
            ) {
                FocusableButton(
                    text = stringResource(R.string.button_cancel),
                    onClick = { (context as? ComponentActivity)?.finish() },
                    background = MaterialTheme.colorScheme.surface
                )

                FocusableButton(
                    text = stringResource(R.string.button_save),
                    onClick = {
                        var hasError = false

                        val checkIntervalValue = checkIntervalSeconds.toLongOrNull()
                        if (checkIntervalSeconds.isBlank()) { checkIntervalError = "Required"; hasError = true }
                        else if (checkIntervalValue == null || checkIntervalValue !in 1..99999) { checkIntervalError = "Invalid"; hasError = true }

                        val idleTimeoutValue = idleTimeout.toLongOrNull()
                        if (idleTimeoutValue == null || idleTimeoutValue < 0) { idleTimeoutError = "Must be ≥ 0"; hasError = true }

                        val idleBrightnessValue = idleBrightness.toIntOrNull()
                        if (idleBrightnessValue == null || idleBrightnessValue !in 0..100) { idleBrightnessError = "0–100"; hasError = true }

                        val activeBrightnessValue = activeBrightness.toIntOrNull()
                        if (activeBrightnessValue == null || activeBrightnessValue !in 0..100) { activeBrightnessError = "0–100"; hasError = true }

                        if (hasError) return@FocusableButton

                        (context as? ComponentActivity)?.lifecycleScope?.launch {
                            kioskSettings.setCheckInterval(checkIntervalValue!! * 1000L)
                            kioskSettings.setStartUrl(kioskUrl)
                            kioskSettings.setRotation(rotation)
                            kioskSettings.setIdleTimeout(idleTimeoutValue!!)
                            kioskSettings.setIdleBrightness(idleBrightnessValue!!)
                            kioskSettings.setActiveBrightness(activeBrightnessValue!!)

                            StayOnTopService.restart(context)
                        }
                        (context as? ComponentActivity)?.finish()
                    },
                    background = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GeneralSettingsTab(
    kioskUrl: String,
    onKioskUrlChange: (String) -> Unit,
    checkIntervalSeconds: String,
    onCheckIntervalChange: (String) -> Unit,
    checkIntervalError: String?,
    onCheckIntervalErrorChange: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))

        SettingsField(
            label = stringResource(R.string.settings_kiosk_url_label),
            description = stringResource(R.string.settings_kiosk_url_desc),
            value = kioskUrl,
            onValueChange = onKioskUrlChange,
            placeholder = stringResource(R.string.settings_kiosk_url_placeholder),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )

        Spacer(Modifier.height(32.dp))

        SettingsField(
            label = stringResource(R.string.settings_check_interval_label),
            description = stringResource(R.string.settings_check_interval_desc),
            value = checkIntervalSeconds,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("\\d*"))) {
                    onCheckIntervalChange(newValue)
                    onCheckIntervalErrorChange(null)
                }
            },
            placeholder = stringResource(R.string.settings_check_interval_placeholder),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = checkIntervalError != null,
            supportingText = checkIntervalError ?: stringResource(R.string.settings_check_interval_supporting)
        )
    }
}

@Composable
fun DisplaySettingsTab(
    rotation: Rotation,
    onRotationChange: (Rotation) -> Unit,
    idleTimeout: String,
    onIdleTimeoutChange: (String) -> Unit,
    idleTimeoutError: String?,
    onIdleTimeoutErrorChange: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))

        RotationSelector(rotation = rotation, onRotationChange = onRotationChange)

        Spacer(Modifier.height(32.dp))

        SettingsField(
            label = stringResource(R.string.settings_idle_timeout_label),
            description = stringResource(R.string.settings_idle_timeout_desc),
            value = idleTimeout,
            onValueChange = {
                if (it.matches(Regex("\\d*"))) {
                    onIdleTimeoutChange(it)
                    onIdleTimeoutErrorChange(null)
                }
            },
            placeholder = "Seconds before dimming",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = idleTimeoutError != null,
            supportingText = idleTimeoutError
        )
    }
}

@Composable
fun BrightnessSettingsTab(
    idleBrightness: String,
    activeBrightness: String,
    onIdleBrightnessChange: (String) -> Unit,
    onActiveBrightnessChange: (String) -> Unit,
    idleBrightnessError: String?,
    activeBrightnessError: String?,
    onIdleBrightnessErrorChange: (String?) -> Unit,
    onActiveBrightnessErrorChange: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))

        SettingsField(
            label = stringResource(R.string.settings_idle_brightness_label),
            description = stringResource(R.string.settings_idle_brightness_desc),
            value = idleBrightness,
            onValueChange = {
                if (it.matches(Regex("\\d*"))) {
                    onIdleBrightnessChange(it)
                    onIdleBrightnessErrorChange(null)
                }
            },
            placeholder = "0–100",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = idleBrightnessError != null,
            supportingText = idleBrightnessError
        )

        Spacer(Modifier.height(32.dp))

        SettingsField(
            label = stringResource(R.string.settings_active_brightness_label),
            description = stringResource(R.string.settings_active_brightness_desc),
            value = activeBrightness,
            onValueChange = {
                if (it.matches(Regex("\\d*"))) {
                    onActiveBrightnessChange(it)
                    onActiveBrightnessErrorChange(null)
                }
            },
            placeholder = "0–100",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = activeBrightnessError != null,
            supportingText = activeBrightnessError
        )
    }
}
