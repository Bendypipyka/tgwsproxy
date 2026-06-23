import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {
    private val _proxyUrl = MutableStateFlow("")
    val proxyUrl: StateFlow<String> = _proxyUrl

    fun updateProxyUrl(newUrl: String) {
        _proxyUrl.value = newUrl
    }

    fun generateRandomSecret(): String {
        // Логика генерации случайного секрета
        return "randomSecret123"
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    var proxyUrl by remember { mutableStateOf("") }
    val currentProxyUrl by viewModel.proxyUrl.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = currentProxyUrl,
            onValueChange = { proxyUrl = it },
            label = { Text("Proxy URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.updateProxyUrl(proxyUrl)
        }) {
            Text("Сохранить")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            openTelegram(context = LocalContext.current)
        }) {
            Text("Открыть Telegram")
        }
    }
}

fun openTelegram(context: Context) {
    val url = "https://t.me/your_channel"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
