package io.test.customtab

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.test.customtab.ui.theme.CustomTabTheme
import io.test.customtab.ui.theme.MyTypography
import io.test.customtab.ui.theme.UnicoBlue

/**
 * MainActivity - Gerencia Custom Tabs e callbacks de forma consolidada
 */
class MainActivity : ComponentActivity() {

    // Estado da URL de input
    private var urlInput = mutableStateOf("")

    // Estado do callback recebido
    private var callbackScheme = mutableStateOf("")
    private var callbackHost = mutableStateOf("")
    private var callbackParams = mutableStateOf<Map<String, String>>(emptyMap())

    // Custom Tabs
    private var customTabsClient: CustomTabsClient? = null
    private var customTabsSession: CustomTabsSession? = null

    companion object {
        private const val UNICO_PRIMARY_COLOR = 0xFF007BFF.toInt()
        private const val UNICO_SECONDARY_COLOR = 0xFFE3F2FD.toInt()
    }

    private val customTabsServiceConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            customTabsClient = client
            customTabsClient?.warmup(0L)
            customTabsSession = customTabsClient?.newSession(null)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            customTabsClient = null
            customTabsSession = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeCustomTabs()
        setContent {
            MyApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unbindService(customTabsServiceConnection)
        } catch (e: Exception) {
            // Serviço já desconectado
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processCallback(intent)
    }

    // ============= Custom Tabs =============

    private fun initializeCustomTabs() {
        val packageName = CustomTabsClient.getPackageName(this, emptyList())
        if (packageName != null) {
            CustomTabsClient.bindCustomTabsService(this, packageName, customTabsServiceConnection)
        }
    }

    private fun openCustomTab(url: String) {
        if (url.isBlank()) return

        try {
            val uri = Uri.parse(url)

            // Pre-carrega URL para melhor performance
            customTabsSession?.mayLaunchUrl(uri, null, null)

            val customTabsIntent = CustomTabsIntent.Builder(customTabsSession).apply {
                setShowTitle(true)
                setUrlBarHidingEnabled(true)
                setToolbarColor(UNICO_PRIMARY_COLOR)
                setSecondaryToolbarColor(UNICO_SECONDARY_COLOR)
                setStartAnimations(this@MainActivity, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                setExitAnimations(this@MainActivity, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                setInstantAppsEnabled(false)
            }.build().apply {
                intent.apply {
                    putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://$packageName"))
                    putExtra("android.support.customtabs.extra.user_opt_out_from_session", false)
                    putExtra("androidx.browser.customtabs.extra.user_opt_out_from_session", false)
                    putExtra("com.android.browser.application_id", packageName)
                }
            }

            customTabsIntent.launchUrl(this, uri)

        } catch (e: Exception) {
            // Fallback: abre no browser padrão
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    // ============= Callback Handler =============

    private fun processCallback(intent: Intent) {
        val uri = intent.data

        if (uri != null) {
            // Extrai parâmetros
            val params = mutableMapOf<String, String>()
            uri.queryParameterNames?.forEach { param ->
                val value = uri.getQueryParameter(param) ?: ""
                params[param] = value
            }

            // Atualiza estado para exibir na UI
            callbackScheme.value = uri.scheme ?: ""
            callbackHost.value = uri.host ?: ""
            callbackParams.value = params
        }
    }

    // ============= UI =============

    @Composable
    fun MyApp() {
        CustomTabTheme {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        30.dp,
                        alignment = Alignment.CenterVertically
                    ),
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_1),
                        contentDescription = "Logo Unico",
                        modifier = Modifier.size(100.dp)
                    )

                    Text(
                        text = "Bem-vindo ao by Unico!",
                        textAlign = TextAlign.Center,
                        style = MyTypography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 24.sp
                        )
                    )

                    Text(
                        text = "Abaixo insira o link gerado:",
                        textAlign = TextAlign.Center,
                        style = MyTypography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp
                        )
                    )

                    TextField(
                        value = urlInput.value,
                        onValueChange = { urlInput.value = it },
                        label = {
                            Text(
                                "Insira uma URL aqui",
                                style = MyTypography.bodyMedium
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Button(
                        onClick = { openCustomTab(urlInput.value) },
                        modifier = Modifier.size(width = 290.dp, height = 60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UnicoBlue
                        )
                    ) {
                        Text(
                            text = "Confirmar",
                            style = MyTypography.titleLarge.copy(
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        )
                    }

                    // Exibe informações do callback se existirem
                    if (callbackScheme.value.isNotEmpty() && callbackHost.value.isNotEmpty()) {
                        CallbackInfoSection()
                    }
                }
            }
        }
    }

    @Composable
    fun CallbackInfoSection() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "✅ Callback Recebido:",
                style = MyTypography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "Scheme: ${callbackScheme.value}",
                style = MyTypography.bodyMedium
            )

            Text(
                text = "Host: ${callbackHost.value}",
                style = MyTypography.bodyMedium
            )

            if (callbackParams.value.isNotEmpty()) {
                Text(
                    text = "Parâmetros:",
                    style = MyTypography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                callbackParams.value.forEach { (key, value) ->
                    Text(
                        text = "  • $key: $value",
                        style = MyTypography.labelSmall
                    )
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MyAppPreview() {
        MyApp()
    }
}
