package com.amurcanov.tgwsproxy.ui
// build.gradle зависимость:
// implementation("androidx.compose.material:material-icons-extended")

import com.amurcanov.tgwsproxy.SettingsStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    var port by remember { mutableStateOf("1443") }
    var wsPool by remember { mutableStateOf(6) }
    var secretKey by remember { mutableStateOf("6b14cb003a34964c80c1af1f1157616") }
    var routingMode by remember { mutableStateOf(RoutingMode.CLOUDFLARE_CDN) }
    var workerDomain by remember { mutableStateOf("tg-ws-proxy-bhz.pages.dev") }
    var isAutoStart by remember { mutableStateOf(false) }

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
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Порт") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
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
                        label = { Text("Секретный ключ") }, // Fix: label добавлен
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
