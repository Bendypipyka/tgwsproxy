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

// Кастомные цвета из твоего макета
val BgColor = Color(0xFFF4F0FB) // Светло-фиолетовый фон
val CardColor = Color(0xFFFFFFFF) // Белая карточка
val AccentPurple = Color(0xFF5C5488) // Темно-фиолетовый акцент (для активных кнопок)
val LightPurple = Color(0xFFE4DDF3) // Светло-фиолетовый (для неактивных кнопок)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenTemplate() {
    // Временные стейты для демонстрации (потом заменишь на свои из SettingsStore)
    var port by remember { mutableStateOf("1443") }
    var wsPool by remember { mutableStateOf(6) }
    var secretKey by remember { mutableStateOf("6b14cb003a34964c80c1af1f1157616") }
    var isCfEnabled by remember { mutableStateOf(true) }
    var isAutoStart by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor) // Устанавливаем цвет фона приложения
            .padding(horizontal = 16.dp)
            .padding(top = 48.dp, bottom = 80.dp) // Отступы для статус-бара и нижнего меню
            .verticalScroll(rememberScrollState())
    ) {
        // Заголовок экрана
        Text(
            text = "Настройки",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Главная белая карточка с настройками
        Card(
            shape = RoundedCornerShape(24.dp), // Сильное скругление как на макете
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

                    Button(
                        onClick = { /* Автонастройка */ },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightPurple,
                            contentColor = AccentPurple
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Авто (Включён CF)", fontWeight = FontWeight.SemiBold)
                    }
                }

                Divider(color = BgColor, thickness = 2.dp)

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
                                modifier = Modifier.weight(1f).height(48.dp),
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

                Divider(color = BgColor, thickness = 2.dp)

                // --- СЕКЦИЯ: Секретный ключ ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(icon = Icons.Default.VpnKey, title = "Секретный ключ")
                    
                    OutlinedTextField(
                        value = secretKey,
                        onValueChange = { secretKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { /* Сгенерировать новый ключ */ }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Обновить", tint = AccentPurple)
                            }
                        }
                    )
                }

                Divider(color = BgColor, thickness = 2.dp)

                // --- СЕКЦИЯ: Переключатели ---
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingSwitchRow(
                        icon = Icons.Default.Cloud,
                        title = "CloudFlare CDN",
                        isChecked = isCfEnabled,
                        onCheckedChange = { isCfEnabled = it }
                    )
                    
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

// Вспомогательный компонент для заголовков секций
@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, color = AccentPurple, fontSize = 16.sp)
    }
}

// Вспомогательный компонент для строк с переключателями (свитчами)
@Composable
fun SettingSwitchRow(icon: ImageVector, title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
