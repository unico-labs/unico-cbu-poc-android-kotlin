package io.test.customtab

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.test.customtab.ui.theme.CustomTabTheme
import io.test.customtab.ui.theme.MyTypography
import io.test.customtab.ui.theme.UnicoBlue

class MainActivity : ComponentActivity() {
    
    private var urlInput = mutableStateOf("")
    private lateinit var customTabManager: CustomTabManager
    private lateinit var callbackHandler: CallbackHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeManagers()
        setContent {
            MyApp()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        customTabManager.cleanup()
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // CALLBACK: Recebe URL de retorno do Custom Tab
        callbackHandler.processCallback(intent)
    }
    
    private fun initializeManagers() {
        customTabManager = CustomTabManager(this)
        callbackHandler = CallbackHandler()
        customTabManager.initialize()
    }
    
    private fun openUrl(url: String) {
        if (url.isNotBlank()) {
            customTabManager.openCustomTab(url)
        }
    }

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
                        onClick = { openUrl(urlInput.value) },
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
                    
                    // CALLBACK: Exibe informações quando URL retorna dados
                    if (callbackHandler.hasCallbackData()) {
                        CallbackInfoSection()
                    }
                }
            }
        }
    }
    
    @Composable
    fun CallbackInfoSection() {
        val callbackInfo = callbackHandler.callbackInfo.value
        
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
                text = "Scheme: ${callbackInfo.scheme}",
                style = MyTypography.bodyMedium
            )
            
            Text(
                text = "Host: ${callbackInfo.host}",
                style = MyTypography.bodyMedium
            )
            
            if (callbackInfo.parameters.isNotEmpty()) {
                Text(
                    text = "Parâmetros:",
                    style = MyTypography.bodyMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
                callbackInfo.parameters.forEach { (key, value) ->
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
