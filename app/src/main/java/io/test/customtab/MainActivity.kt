package io.test.customtab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            MyApp()
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        println("MainActivity onNewIntent")
        val uri = intent.data
        callbackScheme.value = uri?.scheme ?: ""
        callbackHost.value = uri?.host ?: ""
        uri?.queryParameterNames?.forEach {
            callbackArgs[it] = uri.getQueryParameter(it) ?: ""
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
                        onClick = { val intent = CustomTabsIntent.Builder().build()
                            intent.launchUrl(this@MainActivity, Uri.parse(urlInput.value))}

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
