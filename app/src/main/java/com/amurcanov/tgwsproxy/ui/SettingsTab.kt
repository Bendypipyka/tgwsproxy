package com.amurcanov.tgwsproxy.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amurcanov.tgwsproxy.ProxyService
import com.amurcanov.tgwsproxy.SettingsStore
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// --- Вспомогательные утилиты ---

private fun generateRandomSecret(): String {
    val bytes = ByteArray(16)
    java.security.SecureRandom().nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}

fun openTelegram(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Telegram не найден", Toast.LENGTH_SHORT).show()
    }
}

// Модель для дата-центров, чтобы не плодить переменные
data class DcConfig(
    val key: String,
    val label: String,
    val value: String,
    val onValueChange: (String) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(settingsStore: SettingsStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Системные состояния
    val isRunning by ProxyService.isRunning.collectAsStateWithLifecycle()
    val isReady by settingsStore.isReady.collectAsStateWithLifecycle(initialValue = false)
    val isExperimental by settingsStore.isExperimentalMode.collectAsStateWithLifecycle(initialValue = false)
    val isDcAuto by settingsStore.isDcAuto.collectAsStateWithLifecycle(initialValue = true)

    // Избавляемся от ада с 12+ подписками: собираем все DC в один реактивный объект
    val dcState by remember(settingsStore) {
        combine(
            settingsStore.dc1, settingsStore.dc2, settingsStore.dc3, 
            settingsStore.dc4, settingsStore.dc5, settingsStore.dc203,
            settingsStore.dc1m, settingsStore.dc2m, settingsStore.dc3m, 
            settingsStore.dc4m, settingsStore.dc5m, settingsStore.dc203m
        ) { array ->
            mapOf(
                "dc1" to array[0], "dc2" to (array[1].ifEmpty { SettingsStore.DEFAULT_DIRECT_DC2_IP }), 
                "dc3" to array[2], "dc4" to (array[3].ifEmpty { SettingsStore.DEFAULT_DIRECT_DC4_IP }), 
                "dc5" to array[4], "dc203" to array[5],
                "dc1m" to array[6], "dc2m" to array[7], "dc3m" to array[8], 
                "dc4m" to array[9], "dc5m" to array[10], "dc203m" to array[11]
            )
        }
    }.collectAsStateWithLifecycle(initialValue = emptyMap())

    // Список конфигураций для отрисовки в цикле
    val dcList = remember(dcState) {
        listOf(
            DcConfig("dc1", "DC 1 IP", dcState["dc1"].orEmpty()) { scope.launch { settingsStore.setDc1(it) } },
            DcConfig("dc2", "DC 2 IP", dcState["dc2"].orEmpty()) { scope.launch { settingsStore.setDc2(it) } },
            DcConfig("dc3", "DC 3 IP", dcState["dc3"].orEmpty()) { scope.launch { settingsStore.setDc3(it) } },
            DcConfig("dc4", "DC 4 IP", dcState["dc4"].orEmpty()) { scope.launch { settingsStore.setDc4(it) } },
            DcConfig("dc5", "DC 5 IP", dcState["dc5"].orEmpty()) { scope.launch { settingsStore.setDc5(it) } },
            DcConfig("dc203", "DC 203 IP", dcState["dc203"].orEmpty()) { scope.launch { settingsStore.setDc203(it) } },
            // Мастер-ноды (m)
            DcConfig("dc1m", "DC 1 Master IP", dcState["dc1m"].orEmpty()) { scope.launch { settingsStore.setDc1m(it) } },
            DcConfig("dc2m", "DC 2 Master IP", dcState["dc2m"].orEmpty()) { scope.launch { settingsStore.setDc2m(it) } },
            DcConfig("dc3m", "DC 3 Master IP", dcState["dc3m"].orEmpty()) { scope.launch { settingsStore.setDc3m(it) } },
            DcConfig("dc4m", "DC 4 Master IP", dcState["dc4m"].orEmpty()) { scope.launch { settingsStore.setDc4m(it) } },
            DcConfig("dc5m", "DC 5 Master IP", dcState["dc5m"].orEmpty()) { scope.launch { settingsStore.setDc5m(it) } },
            DcConfig("dc203m", "DC 203 Master IP", dcState["dc203m"].orEmpty()) { scope.launch { settingsStore.setDc203m(it) } },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки Proxy", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* Действие рефреша если нужно */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Переключатель автоматического режима
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Автонастройка DC", style = MaterialTheme.typography.titleMedium)
                        Text("Использовать встроенные IP адреса", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = isDcAuto,
                        onCheckedChange = { scope.launch { settingsStore.setIsDcAuto(it) } }
                    )
                }
            }

            // Кастомные настройки IP (показываем, только если автовыбор выключен)
            if (!isDcAuto) {
                Text(
                    text = "Конфигурация Data Centers",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Рендерим ВСЕ текстовые поля одной строчкой кода вместо гигантской простыни
                dcList.forEach { dc ->
                    OutlinedTextField(
                        value = dc.value,
                        onValueChange = dc.onValueChange,
                        label = { Text(dc.label) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
            }
        }
    }
}
