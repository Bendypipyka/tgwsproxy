package com.amurcanov.tgwsproxy.ui
// build.gradle зависимость:
// implementation("androidx.compose.material:material-icons-extended")

import android.widget.Toast
import com.amurcanov.tgwsproxy.SettingsStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

// Цвета
val BgColor = Color(0xFFF4F0FB)
val CardColor = Color(0xFFFFFFFF)
val AccentPurple = Color(0xFF5C5488)
val LightPurple = Color(0xFFE4DDF3)

// Режим маршрутизации — вместо двух булевых флагов
enum class RoutingMode { CLOUDFLARE_CDN, WORKER, NONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(settingsStore: SettingsStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- Значения из хранилища (источник правды) ---
    val isReady by settingsStore.isReady.collectAsStateWithLifecycle(initialValue = false)
    val savedBindIp by settingsStore.bindIp.collectAsStateWithLifecycle(initialValue = "127.0.0.1")
    val savedPort by settingsStore.port.collectAsStateWithLifecycle(initialValue = "1443")
    val savedPoolSize by settingsStore.poolSize.collectAsStateWithLifecycle(initialValue = 6)
    val savedSecretKey by settingsStore.secretKey.collectAsStateWithLifecycle(initialValue = "")
    val savedCfEnabled by settingsStore.cfproxyEnabled.collectAsStateWithLifecycle(initialValue = true)
    val savedCustomDomainEnabled by settingsStore.customCfDomainEnabled.collectAsStateWithLifecycle(initialValue = false)
    val savedCustomDomain by settingsStore.customCfDomain.collectAsStateWithLifecycle(initialValue = "")
    val savedAutoStart by settingsStore.autoStartOnBoot.collectAsStateWithLifecycle(initialValue = false)

    // Поля, которые этот экран не показывает, но обязан сохранить как есть (saveAll сохраняет всё сразу)
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
    val savedIsExperimental by settingsStore.isExperimentalMode.collectAsStateWithLifecycle(initialValue = false)

    // --- Локальные редактируемые поля экрана ---
    var bindIp by remember { mutableStateOf("127.0.0.1") }
    var port by remember { mutableStateOf("1443") }
    var wsPool by remember { mutableStateOf(6) }
    var secretKey by remember { mutableStateOf("") }
    var routingMode by remember { mutableStateOf(RoutingMode.CLOUDFLARE_CDN) }
    var workerDomain by remember { mutableStateOf("") }
    var isAutoStart by remember { mutableStateOf(false) }

    var ipError by remember { mutableStateOf(false) }
    var portError by remember { mutableStateOf(false) }

    // Подтягиваем сохранённые значения один раз, как только DataStore готов.
    // Дальше работаем только с локальными полями, чтобы не перетирать ввод пользователя.
    var hasLoadedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(isReady) {
        if (isReady && !hasLoadedOnce) {
            bindIp = savedBindIp
            port = savedPort
            wsPool = savedPoolSize
            secretKey = savedSecretKey
            routingMode = when {
                !savedCfEnabled -> RoutingMode.NONE
                savedCustomDomainEnabled -> RoutingMode.WORKER
                else -> RoutingMode.CLOUDFLARE_CDN
            }
            workerDomain = savedCustomDomain
            isAutoStart = savedAutoStart
            hasLoadedOnce = true
        }
    }

    fun onSaveClick() {
        val ipValid = isValidIpv4(bindIp)
        val portValid = isValidPort(port)
        ipError = !ipValid
        portError = !portValid
        if (!ipValid || !portValid) {
            Toast.makeText(context, "Проверьте IP и порт", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            settingsStore.saveAll(
                isDcAuto = savedIsDcAuto,
                dc1 = savedDc1, dc2 = savedDc2, dc3 = savedDc3, dc4 = savedDc4,
                dc5 = savedDc5, dc203 = savedDc203,
                dc1m = savedDc1m, dc2m = savedDc2m, dc3m = savedDc3m,
                dc4m = savedDc4m, dc5m = savedDc5m, dc203m = savedDc203m,
                isExperimental = savedIsExperimental,
                bindIp = bindIp.trim(),
                port = port.trim(),
                poolSize = wsPool,
                cfproxyEnabled = routingMode != RoutingMode.NONE,
                customCfDomainEnabled = routingMode == RoutingMode.WORKER,
                customCfDomain = workerDomain.trim(),
                secretKey = secretKey
            )
            settingsStore.saveAutoStartOnBoot(isAutoStart)
            Toast.makeText(context, "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(horizontal = 16.dp)
            .padding(top = 48.dp, bottom = 80.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Настройки",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // --- СЕКЦИЯ: Подключение ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(icon = Icons.Default.Public, title = "Подключение")

                    OutlinedTextField(
                        value = bindIp,
                        onValueChange = {
                            bindIp = it
                            ipError = false
                        },
                        label = { Text("IP адрес") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = ipError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = {
                            Text(
                                if (ipError) "Введите корректный IPv4-адрес"
                                else "127.0.0.1 — только это устройство, 0.0.0.0 — принимать из локальной сети"
                            )
                        }
                    )

                    OutlinedTextField(
                        value = port,
                        onValueChange = {
                            port = it
                            portError = false
                        },
                        label = { Text("Порт") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = portError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            if (portError) Text("Порт должен быть числом от 1 до 65535")
                        }
                    )

                    // Кнопка динамически отражает текущий режим
                    val autoButtonLabel = when (routingMode) {
                        RoutingMode.CLOUDFLARE_CDN -> "Авто (CF CDN включён)"
                        RoutingMode.WORKER -> "Авто (Worker включён)"
                        RoutingMode.NONE -> "Авто (без маршрутизации)"
                    }

                    Button(
                        onClick = { /* Автонастройка */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightPurple,
                            contentColor = AccentPurple
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(autoButtonLabel, fontWeight = FontWeight.SemiBold)
                    }
                }

                HorizontalDivider(color = BgColor, thickness = 2.dp)

                // --- СЕКЦИЯ: Пул WS ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(icon = Icons.Default.Layers, title = "Пул WS")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(2, 4, 6).forEach { poolValue ->
                            val isSelected = wsPool == poolValue
                            Button(
                                onClick = { wsPool = poolValue },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) AccentPurple else LightPurple,
                                    contentColor = if (isSelected) Color.White else AccentPurple
                                ),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text(poolValue.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                HorizontalDivider(color = BgColor, thickness = 2.dp)

                // --- СЕКЦИЯ: Секретный ключ ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(icon = Icons.Default.VpnKey, title = "Секретный ключ")

                    OutlinedTextField(
                        value = secretKey,
                        onValueChange = { secretKey = it },
                        label = { Text("Секретный ключ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { /* Сгенерировать новый ключ */ }) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Обновить",
                                    tint = AccentPurple
                                )
                            }
                        }
                    )
                }

                HorizontalDivider(color = BgColor, thickness = 2.dp)

                // --- СЕКЦИЯ: Маршрутизация и Переключатели ---
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader(icon = Icons.Default.Route, title = "Маршрутизация")

                    // CloudFlare CDN — при включении снимает Worker и наоборот
                    SettingSwitchRow(
                        icon = Icons.Default.Cloud,
                        title = "CloudFlare CDN",
                        isChecked = routingMode == RoutingMode.CLOUDFLARE_CDN,
                        onCheckedChange = { if (it) routingMode = RoutingMode.CLOUDFLARE_CDN else routingMode = RoutingMode.NONE }
                    )

                    SettingSwitchRow(
                        icon = Icons.Default.Language,
                        title = "Свой домен (Worker)",
                        isChecked = routingMode == RoutingMode.WORKER,
                        onCheckedChange = { if (it) routingMode = RoutingMode.WORKER else routingMode = RoutingMode.NONE }
                    )

                    // Поле домена появляется только при активном Worker
                    AnimatedVisibility(
                        visible = routingMode == RoutingMode.WORKER,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        OutlinedTextField(
                            value = workerDomain,
                            onValueChange = { workerDomain = it },
                            label = { Text("Домен Worker'а") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                    }

                    HorizontalDivider(color = BgColor, thickness = 2.dp)

                    SettingSwitchRow(
                        icon = Icons.Default.PowerSettingsNew,
                        title = "Автозапуск",
                        isChecked = isAutoStart,
                        onCheckedChange = { isAutoStart = it }
                    )
                }

                HorizontalDivider(color = BgColor, thickness = 2.dp)

                Button(
                    onClick = { onSaveClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPurple,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сохранить", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, color = AccentPurple, fontSize = 16.sp)
    }
}

@Composable
fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Medium, color = AccentPurple, fontSize = 16.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentPurple,
                uncheckedThumbColor = AccentPurple,
                uncheckedTrackColor = LightPurple
            )
        )
    }
}

/** Простая проверка формата IPv4 (без ведущих нулей, октеты 0-255). */
private fun isValidIpv4(ip: String): Boolean {
    val parts = ip.trim().split(".")
    if (parts.size != 4) return false
    return parts.all { part ->
        val n = part.toIntOrNull()
        n != null && n in 0..255 && part == n.toString()
    }
}

/** Проверка, что порт — целое число в допустимом диапазоне TCP-портов. */
private fun isValidPort(port: String): Boolean {
    val p = port.trim().toIntOrNull() ?: return false
    return p in 1..65535
}
