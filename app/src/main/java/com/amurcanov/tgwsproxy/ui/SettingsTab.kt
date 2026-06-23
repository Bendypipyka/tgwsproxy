package com.amurcanov.tgwsproxy.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amurcanov.tgwsproxy.ProxyService
import com.amurcanov.tgwsproxy.SettingsStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsTab(settingsStore: SettingsStore) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val isRunning by ProxyService.isRunning.collectAsStateWithLifecycle()
    val isReady by settingsStore.isReady.collectAsStateWithLifecycle(initialValue = false)
    
    // Состояния из хранилища (source of truth)
    val savedCfEnabled by settingsStore.cfproxyEnabled.collectAsStateWithLifecycle(initialValue = true)
    val savedCustomDomainEnabled by settingsStore.customCfDomainEnabled.collectAsStateWithLifecycle(initialValue = false)
    val savedCustomDomain by settingsStore.customCfDomain.collectAsStateWithLifecycle(initialValue = "")
    
    // Локальное UI-состояние (без ключей, чтобы не пересоздавалось)
    var cfEnabled by remember { mutableStateOf(savedCfEnabled) }
    var customCfDomainEnabled by remember { mutableStateOf(savedCustomDomainEnabled) }
    var customCfDomain by remember { mutableStateOf(savedCustomDomain) }
    
    // Синхронизация UI-состояния с сохранённым при внешнем обновлении
    LaunchedEffect(savedCfEnabled) { cfEnabled = savedCfEnabled }
    LaunchedEffect(savedCustomDomainEnabled) { customCfDomainEnabled = savedCustomDomainEnabled }
    LaunchedEffect(savedCustomDomain) { customCfDomain = savedCustomDomain }
    
    // Состояние процесса сохранения
    var isSaving by remember { mutableStateOf(false) }
    var saveJob by remember { mutableStateOf<Job?>(null) }

    fun scheduleSave() {
        saveJob?.cancel()
        isSaving = true
        saveJob = scope.launch {
            delay(300) // debounce
            try {
                // Обрезаем пробелы только при сохранении, а не при вводе
                val trimmedDomain = customCfDomain.trim()
                settingsStore.saveCfSettings(cfEnabled, customCfDomainEnabled, trimmedDomain)
                
                // Обновляем локальное состояние после успешного сохранения
                customCfDomain = trimmedDomain
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Ошибка сохранения: ${e.message ?: "неизвестная ошибка"}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isSaving = false
            }
        }
    }

    if (!isReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Заголовок с индикатором сохранения ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Настройки прокси", style = MaterialTheme.typography.titleMedium)
            if (isSaving) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Сохранение...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // --- CloudFlare и Custom Domain ---
        
        // CloudFlare Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CloudFlare CDN", style = MaterialTheme.typography.titleSmall)
            Switch(
                checked = cfEnabled,
                onCheckedChange = { 
                    cfEnabled = it
                    scheduleSave() 
                },
                enabled = !isRunning
            )
        }

        HorizontalDivider()

        // Custom Domain Column
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
                    onCheckedChange = { 
                        customCfDomainEnabled = it
                        scheduleSave() 
                    },
                    enabled = !isRunning
                )
            }
            
            OutlinedTextField(
                value = customCfDomain,
                onValueChange = { 
                    customCfDomain = it
                    scheduleSave() 
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("my-worker.workers.dev") },
                enabled = !isRunning,
                singleLine = true,
                isError = customCfDomain.isNotBlank() && 
                    customCfDomain.trim().isEmpty() // только пробелы
            )
        }
        
        HorizontalDivider()
    }
}
