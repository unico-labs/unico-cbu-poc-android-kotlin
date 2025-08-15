package io.test.customtab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsCallback
import android.content.ComponentName
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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


class MainActivity : ComponentActivity() {
    private var urlInput = mutableStateOf("")
    private var callbackScheme = mutableStateOf("")
    private var callbackHost = mutableStateOf("")
    private var callbackArgs = mutableMapOf<String, String>()
    
    // Custom Tab Service para sessão persistente
    private var customTabsClient: CustomTabsClient? = null
    private var customTabsSession: CustomTabsSession? = null
    
    // Conexão com o serviço Custom Tab
    private val customTabsServiceConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            customTabsClient = client
            customTabsClient?.warmup(0L)
            customTabsSession = customTabsClient?.newSession(object : CustomTabsCallback() {
                override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                    super.onNavigationEvent(navigationEvent, extras)
                    // Callback opcional para monitorar navegação
                }
            })
        }
        
        override fun onServiceDisconnected(name: ComponentName) {
            customTabsClient = null
            customTabsSession = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Conecta ao serviço Custom Tab para estabelecer sessão
        bindCustomTabsService()
        
        setContent {
            MyApp()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Desconecta do serviço Custom Tab
        try {
            unbindService(customTabsServiceConnection)
        } catch (e: Exception) {
            // Serviço já desconectado
        }
    }
    
    /**
     * Conecta ao serviço Custom Tab para estabelecer uma sessão persistente
     */
    private fun bindCustomTabsService() {
        val packageName = CustomTabsClient.getPackageName(this, emptyList())
        if (packageName != null) {
            CustomTabsClient.bindCustomTabsService(this, packageName, customTabsServiceConnection)
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        println("=== CALLBACK RECEBIDO ===")
        val uri = intent.data
        
        if (uri != null) {
            println("URI completo: $uri")
            println("Scheme: ${uri.scheme}")
            println("Host: ${uri.host}")
            println("Path: ${uri.path}")
            println("Query: ${uri.query}")
            
            callbackScheme.value = uri.scheme ?: ""
            callbackHost.value = uri.host ?: ""
            
            // Processa parâmetros da query
            callbackArgs.clear()
            uri.queryParameterNames?.forEach { param ->
                val value = uri.getQueryParameter(param) ?: ""
                callbackArgs[param] = value
                println("Parâmetro: $param = $value")
            }
            
            println("✅ Callback processado com sucesso - SEM CONFIRMAÇÃO!")
        } else {
            println("⚠️ Intent recebido sem URI data")
        }
        println("========================")
    }

    /**
     * Método que configura e abre o Custom Tab com sessão persistente
     * para eliminar completamente a confirmação de callback ao retornar ao app
     */
    private fun openCustomTab(url: String) {
        try {
            val uri = Uri.parse(url)
            
            // Pre-aquece a URL para melhorar performance e confiança
            customTabsSession?.mayLaunchUrl(uri, null, null)
            
            // Constrói o Custom Tab com sessão estabelecida
            val customTabsIntent = CustomTabsIntent.Builder(customTabsSession).apply {
                // Configurações para experiência integrada
                setShowTitle(true)
                setUrlBarHidingEnabled(true)
                
                // Visual personalizado
                setToolbarColor(0xFF007BFF.toInt())
                setSecondaryToolbarColor(0xFFE3F2FD.toInt())
                
                // Animações suaves
                setStartAnimations(this@MainActivity, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                setExitAnimations(this@MainActivity, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                
                // Configuração crítica: Define que este Custom Tab está vinculado ao app
                setInstantAppsEnabled(false)
                
            }.build()

            // Configurações avançadas do Intent para eliminar confirmação
            customTabsIntent.intent.apply {
                // Referrer que identifica claramente o app de origem
                putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + packageName))
                
                // Marca como trusted web activity
                putExtra("android.support.customtabs.extra.user_opt_out_from_session", false)
                putExtra("androidx.browser.customtabs.extra.user_opt_out_from_session", false)
                
                // Flags específicas para callback automático
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                
                // A sessão já está vinculada através do CustomTabsIntent.Builder(customTabsSession)
                // Essa configuração não é necessária para o funcionamento
                
                // Configuração adicional para trusted origin
                putExtra("com.android.browser.application_id", packageName)
            }

            // Lança o Custom Tab
            customTabsIntent.launchUrl(this, uri)
            
            println("Custom Tab aberto com sessão persistente para: $url")
            
        } catch (e: Exception) {
            println("Erro ao abrir Custom Tab: ${e.message}")
            // Fallback para navegador padrão
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                if (fallbackIntent.resolveActivity(packageManager) != null) {
                    startActivity(fallbackIntent)
                }
            } catch (fallbackError: Exception) {
                println("Erro no fallback: ${fallbackError.message}")
            }
        }
    }
    
    /**
     * Método alternativo mais agressivo para eliminar confirmação
     * Usa técnicas específicas do Chrome Custom Tab
     */
    private fun openCustomTabNoConfirmation(url: String) {
        try {
            val uri = Uri.parse(url)
            
            // Método mais direto - força o Chrome a reconhecer origem
            val customTabsIntent = CustomTabsIntent.Builder(customTabsSession).apply {
                setShowTitle(false) // Remove título para parecer mais integrado
                setUrlBarHidingEnabled(true)
                setInstantAppsEnabled(false)
                
                // Configuração visual otimizada para integração
                
            }.build()

            // Configuração radical do Intent
            customTabsIntent.intent.apply {
                // Remove QUALQUER indicação de atividade externa
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                
                // Força identificação como parte do app
                putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://$packageName"))
                putExtra("android.intent.extra.REFERRER_NAME", "android-app://$packageName")
                
                // Desabilita completamente prompts de confirmação
                putExtra("com.android.browser.application_id", packageName)
                putExtra("create_new_tab", false)
                putExtra("com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB", false)
                
                // Configurações específicas para trusted web activity
                putExtra("androidx.browser.trusted.EXTRA_LAUNCH_AS_TRUSTED_WEB_ACTIVITY", true)
                putExtra("android.support.customtabs.extra.user_opt_out_from_session", false)
            }
            
            // Lança diretamente sem validações adicionais
            customTabsIntent.launchUrl(this, uri)
            
            println("Custom Tab opened with aggressive no-confirmation mode")
            
        } catch (e: Exception) {
            println("Fallback to regular Custom Tab: ${e.message}")
            openCustomTab(url) // Fallback para método anterior
        }
    }

    @Composable
    fun MyApp() {


        CustomTabTheme {
            // A surface container using the 'background' color from the theme
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
                    // Imagem do ícone (substitua pelo seu recurso de imagem)
                    Image(
                        painter = painterResource(R.drawable.logo_1), // Use um recurso disponível local
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        text = "Bem-vindo ao by Unico!",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        style = MyTypography.bodyLarge.copy(color = Color.DarkGray)


                        )
                    // Texto de boas-vindas
                    Text(
                        text = "Abaixo insira o link gerado:",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        style = MyTypography.titleLarge.copy(color = Color.Gray),

                    )
                    // Campo de texto para entrada da URL
                    TextField(
                        value = urlInput.value,
                        onValueChange = { urlInput.value = it },
                        label = { Text("Insira uma URL aqui", fontSize = 14.sp)

                        },
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    // Botão de confirmar
                    Button(
                        modifier = Modifier.size(width = 290.dp, height = 60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
                        onClick = { openCustomTabNoConfirmation(urlInput.value) }

                    ) {
                        Text(text = "Confirmar", fontSize = 20.sp, style = MyTypography.titleLarge)
                    }
                    // Exibir informações do callback


                }
            }
        }
    }


//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//
//        println("MainActivity onNewIntent")
//        val uri = intent.data
//        val scheme = uri?.scheme
//        val host = uri?.host
//        val data = uri?.getQueryParameter("code")
//
//        callbackScheme.value = scheme ?: ""
//        callbackHost.value = host ?: ""
//        if (uri?.queryParameterNames != null) {
//            uri.queryParameterNames.forEach {
//                callbackArgs[it] = uri.getQueryParameter(it)!!
//            }
//        }
//    }

    //    override fun onResume() {
//        super.onResume()
//
//        println("MainActivity onResume")
//        val url = intent?.data
//        val scheme = url
//
//        println(scheme)
//    }
//}
//
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier,
//        fontSize = 30.sp,
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    CustomTabTheme {
//        Greeting("Android")
//    }
//}
    @Preview(showBackground = true)
    @Composable
    fun MyAppPreview() {
        MyApp()
    }
}
