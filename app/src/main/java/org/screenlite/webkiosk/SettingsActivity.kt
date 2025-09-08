package org.screenlite.webkiosk

import android.content.Intent
import android.os.Bundle
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.screenlite.webkiosk.app.DataStoreHelper
import org.screenlite.webkiosk.service.StayOnTopService

private val DarkBackgroundColor = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val PrimaryColor = Color(0xFF6200EE)
private val TextColorPrimary = Color.White
private val TextColorSecondary = Color.Gray
private val ErrorColor = Color(0xFFCF6679)

private val tvDarkColorScheme = darkColorScheme(
    background = DarkBackgroundColor,
    surface = SurfaceColor,
    primary = PrimaryColor,
    onPrimary = Color.White,
    onBackground = TextColorPrimary,
    onSurface = TextColorPrimary,
    error = ErrorColor
)

private val tvTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        letterSpacing = 0.25.sp,
        color = TextColorPrimary
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = TextColorPrimary
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = TextColorSecondary
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextColorSecondary
    )
)

@Composable
fun TvSettingsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = tvDarkColorScheme,
        typography = tvTypography,
        content = content
    )
}

fun isTvDevice(): Boolean {
    val uiMode = android.content.res.Resources.getSystem().configuration.uiMode
    return (uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION
}

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawable(DarkBackgroundColor.toArgb().toDrawable())

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        setContent {
            TvSettingsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen()
                }
            }
        }
    }
}

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
                    color = Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(8.dp)
                ) else Modifier
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var checkIntervalSeconds by remember { mutableStateOf("") }
    var kioskUrl by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var rotation by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val currentInterval = DataStoreHelper.getCheckInterval(context).first()
        checkIntervalSeconds = (currentInterval / 1000).toString()
        kioskUrl = DataStoreHelper.getStartUrl(context).first()
        rotation = DataStoreHelper.getRotation(context).first()
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
                .padding(start = 48.dp, end = 48.dp, bottom = 32.dp, top = 8.dp)
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = errorMessage != null,
                supportingText = errorMessage ?: stringResource(R.string.settings_check_interval_supporting)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_rotation_label),
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
                            onClick = { rotation = angle },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (rotation == angle) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
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
                    listOf(
                        listOf(0, 90),
                        listOf(180, 270)
                    ).forEach { rowAngles ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowAngles.forEach { angle ->
                                OutlinedButton(
                                    onClick = { rotation = angle },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (rotation == angle) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
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
                            DataStoreHelper.setCheckInterval(context, seconds * 1000L)
                            DataStoreHelper.setStartUrl(context, kioskUrl)
                            DataStoreHelper.setRotation(context, rotation)

                            if (StayOnTopService.isRunning) {
                                context.stopService(Intent(context, StayOnTopService::class.java))
                                context.startService(Intent(context, StayOnTopService::class.java))
                            }
                        }
                        (context as? ComponentActivity)?.finish()
                    },
                    background = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

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
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
        )
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
                    cursorBrush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color.White)
                    ),
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

@Preview(
    name = "UHD TV Preview",
    device = "spec:width=3840px,height=2160px,dpi=320",
    uiMode = Configuration.UI_MODE_TYPE_TELEVISION
)
@Composable
fun SettingsScreenUhdTvPreview() {
    TvSettingsTheme {
        SettingsScreen()
    }
}

@Preview(name = "Settings Screen TV Preview", device = "id:tv_1080p")
@Composable
fun SettingsScreenTvPreview() {
    TvSettingsTheme {
        SettingsScreen()
    }
}

@Preview(name = "Settings Screen Phone Preview", device = "id:pixel_4")
@Composable
fun SettingsScreenPhonePreview() {
    TvSettingsTheme {
        SettingsScreen()
    }
}