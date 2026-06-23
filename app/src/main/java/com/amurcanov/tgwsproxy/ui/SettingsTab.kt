package com.amurcanov.tgwsproxy.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amurcanov.tgwsproxy.ProxyService
import com.amurcanov.tgwsproxy.SettingsStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.SecureRandom

// Вспомогательная функция для генерации секретного ключа
private fun generateRandomSecret(): String {
    val bytes = ByteArray(16)
    SecureRandom().nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}

// Открытие Telegram по ссылке
fun openTelegram(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Telegram не найден", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(settingsStore: SettingsStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isRunning by ProxyService.isRunning.collectAsStateWithLifecycle()
    val isReady by settingsStore.isReady.collectAsStateWithLifecycle(initialValue = false)
    val isExperimental by settingsStore.isExperimentalMode.collectAsStateWithLifecycle(initialValue = false)

    // Чтение сохранённых значений
    val savedIsDcAuto by settingsStore.isDcAuto.collectAsStateWithLifecycle(initialValue = true)
    val savedDc1 by settingsStore.dc1.collectAsStateWithLifecycle(initialValue = "")
    val savedDc2 by settingsStore.dc2.collectAsStateWithLifecycle(initialValue = SettingsStore.DEFAULT_DIRECT_DC2_IP)
    val savedDc3 by settingsStore.dc3.collectAsStateWithLifecycle(initialValue = "")
    val savedDc4 by settingsStore.dc4.collectAsStateWithLifecycle(initialValue = SettingsStore.DEFAULT_DIRECT_DC4_IP)
    val savedDc5 by settingsStore.dc5.collectAsStateWithLifecycle(initialValue = "")
    val savedDc203 by settingsStore.dc203.collectAsStateWithLifecycle(initialValue = "")
    val savedDc1m by settingsStore.dc1m.collectAsStateWithLifecycle(initialValue = "")
    val savedDc2m by settingsStore.dc2m.collectAsStateWithLifecycle(initialValue = "")
    val savedDc3m by settingsStore.dc3m.collectAsStateWithLifecycle(initialValue = "")
    val savedDc4m by settingsStore.dc4m.collectAsStateWithLifecycle(initialValue = "")
    val savedDc5m by settingsStore.dc5m.collectAsStateWithLifecycle(initialValue = "")
    val savedDc203m by settingsStore.dc203m.collectAsStateWithLifecycle(initialValue = "")
    val savedPort by settingsStore.port.collectAsStateWithLifecycle(initialValue = "1443")
    val savedBindIp by settingsStore.bindIp.collectAsStateWithLifecycle(initialValue = "127.0.0.1")
    val savedPoolSize by settingsStore.poolSize.collectAsStateWithLifecycle(initialValue = 4)
    val savedCfEnabled by settingsStore.cfproxyEnabled.collectAsStateWithLifecycle(initialValue = true)
    val savedCustomDomainEnabled by settingsStore.customCfDomainEnabled.collectAsStateWithLifecycle(initialValue = false)
    val savedCustomDomain by settingsStore.customCfDomain.collectAsStateWithLifecycle(initialValue = "")
    val autoStartOnBoot by settingsStore.autoStartOnBoot.collectAsStateWithLifecycle(initialValue = false)
    val savedSecretKey by settingsStore.secretKey.collectAsStateWithLifecycle(initialValue = "LOADING")

    // Если настройки ещё не загружены – показываем индикатор
    if (!isReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Локальные состояния с автосохранением
    var isDcAuto by rememberSaveable(savedIsDcAuto) { mutableStateOf(savedIsDcAuto) }
    var experimentalMode by rememberSaveable(isExperimental) { mutableStateOf(isExperimental) }
    var dc1Text by rememberSaveable(savedDc1) { mutableStateOf(savedDc1) }
    var dc2Text by rememberSaveable(savedDc2) { mutableStateOf(savedDc2) }
    var dc3Text by rememberSaveable(savedDc3) { mutableStateOf(savedDc3) }
    var dc4Text by rememberSaveable(savedDc4) { mutableStateOf(savedDc4) }
    var dc5Text by rememberSaveable(savedDc5) { mutableStateOf(savedDc5) }
    var dc203Text by rememberSaveable(savedDc203) { mutableStateOf(savedDc203) }
    var dc1mText by rememberSaveable(savedDc1m) { mutableStateOf(savedDc1m) }
    var dc2mText by rememberSaveable(savedDc2m) { mutableStateOf(savedDc2m) }
    var dc3mText by rememberSaveable(savedDc3m) { mutableStateOf(savedDc3m) }
    var dc4mText by rememberSaveable(savedDc4m) { mutableStateOf(savedDc4m) }
    var dc5mText by rememberSaveable(savedDc5m) { mutableStateOf(savedDc5m) }
    var dc203mText by rememberSaveable(savedDc203m) { mutableStateOf(savedDc203m) }
    var portText by rememberSaveable(savedPort) { mutableStateOf(savedPort) }
    var bindIpText by rememberSaveable(savedBindIp) { mutableStateOf(savedBindIp) }
    var selectedPoolSize by rememberSaveable(savedPoolSize) { mutableIntStateOf(savedPoolSize) }
    var cfEnabled by rememberSaveable(savedCfEnabled) { mutableStateOf(savedCfEnabled) }
    var customCfDomainEnabled by rememberSaveable(savedCustomDomainEnabled) { mutableStateOf(savedCustomDomainEnabled) }
    var customCfDomain by rememberSaveable(savedCustomDomain) { mutableStateOf(savedCustomDomain) }
    var secretKeyText by remember(savedSecretKey) { mutableStateOf(if (savedSecretKey == "LOADING") "" else savedSecretKey) }

    // Если секретный ключ пустой – генерируем новый
    LaunchedEffect(savedSecretKey) {
        if (savedSecretKey == "") {
            val generated = generateRandomSecret()
            secretKeyText = generated
            settingsStore.saveSecretKey(generated)
        } else if (savedSecretKey != "LOADING") {
            secretKeyText = savedSecretKey
        }
    }

    // Отложенное сохранение всех настроек
    var saveJob by remember { mutableStateOf<Job?>(null) }
    fun scheduleSave() {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(300)
            settingsStore.saveAll(
                isDcAuto, dc1Text, dc2Text, dc3Text, dc4Text, dc5Text, dc203Text,
                dc1mText, dc2mText, dc3mText, dc4mText, dc5mText, dc203mText,
                experimentalMode, bindIpText, portText, selectedPoolSize,
                cfEnabled, customCfDomainEnabled, customCfDomain, secretKeyText
            )
        }
    }

    // Диалог для ручной настройки IP-адресов DC (показывается по кнопке)
    var showIpSetupDialog by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showIpSetupDialog) {
        IpSetupDialog(
            isExperimental = experimentalMode,
            onExperimentalChange = { experimentalMode = it; scheduleSave() },
            dc1Text = dc1Text, onDc1Change = { dc1Text = it; scheduleSave() },
            dc2Text = dc2Text, onDc2Change = { dc2Text = it; scheduleSave() },
            dc3Text = dc3Text, onDc3Change = { dc3Text = it; scheduleSave() },
            dc4Text = dc4Text, onDc4Change = { dc4Text = it; scheduleSave() },
            dc5Text = dc5Text, onDc5Change = { dc5Text = it; scheduleSave() },
            dc203Text = dc203Text, onDc203Change = { dc203Text = it; scheduleSave() },
            dc1mText = dc1mText, onDc1mChange = { dc1mText = it; scheduleSave() },
            dc2mText = dc2mText, onDc2mChange = { dc2mText = it; scheduleSave() },
            dc3mText = dc3mText, onDc3mChange = { dc3mText = it; scheduleSave() },
            dc4mText = dc4mText, onDc4mChange = { dc4mText = it; scheduleSave() },
            dc5mText = dc5mText, onDc5mChange = { dc5mText = it; scheduleSave() },
            dc203mText = dc203mText, onDc203mChange = { dc203mText = it; scheduleSave() },
            onDismiss = { showIpSetupDialog = false }
        )
    }

    // Основной UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // === Блок: IP, Порт и Пул ===
        Text("IP и Порт", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        
        OutlinedTextField(
            value = bindIpText,
            onValueChange = { bindIpText = it; scheduleSave() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("IP для привязки") },
            placeholder = { Text("127.0.0.1") },
            enabled = !isRunning,
            singleLine = true
        )
        
        OutlinedTextField(
            value = portText,
            onValueChange = { portText = it; scheduleSave() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Порт") },
            placeholder = { Text("1443") },
            enabled = !isRunning,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        // Размер пула соединений
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Размер пула потоков", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = selectedPoolSize.toFloat(),
                onValueChange = { selectedPoolSize = it.toInt(); scheduleSave() },
                valueRange = 1f..8f,
                steps = 7,
                enabled = !isRunning,
                modifier = Modifier.width(120.dp)
            )
            Text(selectedPoolSize.toString(), style = MaterialTheme.typography.bodyMedium)
        }

        HorizontalDivider()

        // === Блок: Автоопределение DC ===
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Автоопределение DC", style = MaterialTheme.typography.titleSmall)
            Switch(
                checked = isDcAuto,
                onCheckedChange = { isDcAuto = it; scheduleSave() },
                enabled = !isRunning
            )
        }

        HorizontalDivider()

        // === Блок: CloudFlare CDN ===
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CloudFlare CDN", style = MaterialTheme.typography.titleSmall)
            Switch(
                checked = cfEnabled,
                onCheckedChange = { cfEnabled = it; scheduleSave() },
                enabled = !isRunning
            )
        }

        HorizontalDivider()

        // === Блок: Свой домен (Worker) ===
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Свой домен (Worker)", style = MaterialTheme.typography.titleSmall)
                Switch(
                    checked = customCfDomainEnabled,
                    onCheckedChange = { customCfDomainEnabled = it; scheduleSave() },
                    enabled = !isRunning
                )
            }
            OutlinedTextField(
                value = customCfDomain,
                onValueChange = { customCfDomain = it.trim(); scheduleSave() },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("my-worker.workers.dev") },
                enabled = customCfDomainEnabled && !isRunning,
                singleLine = true
            )
        }

        HorizontalDivider()

        // === Блок: Секретный ключ (просмотр) ===
        OutlinedTextField(
            value = secretKeyText,
            onValueChange = { /* только для чтения */ },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Секретный ключ (только чтение)") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    val newKey = generateRandomSecret()
                    secretKeyText = newKey
                    settingsStore.saveSecretKey(newKey)
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Сгенерировать новый ключ")
                }
            }
        )

        HorizontalDivider()

        // === Блок: Автозапуск при загрузке ===
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Автозапуск при загрузке", style = MaterialTheme.typography.titleSmall)
            Switch(
                checked = autoStartOnBoot,
                onCheckedChange = { settingsStore.saveAutoStartOnBoot(it) },
                enabled = !isRunning
            )
        }

        HorizontalDivider()

        // === Кнопки управления ===
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showIpSetupDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Настройка DC")
            }
            
            Button(
                onClick = {
                    // Остановка сервиса (реализуется отдельно)
                    ProxyService.stop(context)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(if (isRunning) "Остановить" else "Запустить")
            }
        }

        // Кнопка открытия Telegram (опционально)
        Button(
            onClick = { openTelegram(context, "tg://resolve?domain=telegram") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Icon(Icons.Default.Public, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Открыть Telegram")
        }
    }
}

// === Диалог для настройки IP-адресов DC ===
@Composable
private fun IpSetupDialog(
    isExperimental: Boolean,
    onExperimentalChange: (Boolean) -> Unit,
    dc1Text: String, onDc1Change: (String) -> Unit,
    dc2Text: String, onDc2Change: (String) -> Unit,
    dc3Text: String, onDc3Change: (String) -> Unit,
    dc4Text: String, onDc4Change: (String) -> Unit,
    dc5Text: String, onDc5Change: (String) -> Unit,
    dc203Text: String, onDc203Change: (String) -> Unit,
    dc1mText: String, onDc1mChange: (String) -> Unit,
    dc2mText: String, onDc2mChange: (String) -> Unit,
    dc3mText: String, onDc3mChange: (String) -> Unit,
    dc4mText: String, onDc4mChange: (String) -> Unit,
    dc5mText: String, onDc5mChange: (String) -> Unit,
    dc203mText: String, onDc203mChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Ручная настройка DC", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                // Переключатель экспериментального режима (использование альтернативных IP)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Экспериментальный режим", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isExperimental, onCheckedChange = onExperimentalChange)
                }
                
                HorizontalDivider()
                
                // Поля для основных DC (1, 2, 3, 4, 5, 203)
                Text("Основные DC", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                DcInputRow("DC1", dc1Text, onDc1Change)
                DcInputRow("DC2", dc2Text, onDc2Change)
                DcInputRow("DC3", dc3Text, onDc3Change)
                DcInputRow("DC4", dc4Text, onDc4Change)
                DcInputRow("DC5", dc5Text, onDc5Change)
                DcInputRow("DC203", dc203Text, onDc203Change)
                
                HorizontalDivider()
                
                // Поля для Media DC (1m, 2m, 3m, 4m, 5m, 203m)
                Text("Media DC (альтернативные)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                DcInputRow("DC1m", dc1mText, onDc1mChange)
                DcInputRow("DC2m", dc2mText, onDc2mChange)
                DcInputRow("DC3m", dc3mText, onDc3mChange)
                DcInputRow("DC4m", dc4mText, onDc4mChange)
                DcInputRow("DC5m", dc5mText, onDc5mChange)
                DcInputRow("DC203m", dc203mText, onDc203mChange)
                
                Spacer(Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Закрыть")
                }
            }
        }
    }
}

// Вспомогательный компонент для строки ввода IP DC
@Composable
private fun DcInputRow(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text("IP адрес") },
        singleLine = true
    )
}
