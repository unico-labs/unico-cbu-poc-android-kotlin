package io.test.customtab

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class CallbackInfo(
    val scheme: String = "",
    val host: String = "",
    val parameters: Map<String, String> = emptyMap()
)

class CallbackHandler {
    
    private val _callbackInfo = mutableStateOf(CallbackInfo())
    val callbackInfo: MutableState<CallbackInfo> = _callbackInfo
    
    fun processCallback(intent: Intent) {
        // CALLBACK: Processamento da URL de retorno do Custom Tab
        println("=== CALLBACK RECEBIDO ===")
        val uri = intent.data
        
        if (uri != null) {
            logUriInfo(uri)
            updateCallbackInfo(uri)
            println("✅ Callback processado com sucesso!")
        } else {
            println("⚠️ Intent recebido sem URI data")
        }
        println("========================")
    }
    
    private fun logUriInfo(uri: Uri) {
        // CALLBACK: Log detalhado dos dados recebidos
        println("URI completo: $uri")
        println("Scheme: ${uri.scheme}")
        println("Host: ${uri.host}")
        println("Path: ${uri.path}")
        println("Query: ${uri.query}")
        
        uri.queryParameterNames?.forEach { param ->
            val value = uri.getQueryParameter(param) ?: ""
            println("Parâmetro: $param = $value")
        }
    }
    
    private fun updateCallbackInfo(uri: Uri) {
        val parameters = mutableMapOf<String, String>()
        
        uri.queryParameterNames?.forEach { param ->
            val value = uri.getQueryParameter(param) ?: ""
            parameters[param] = value
        }
        
        // CALLBACK: Atualiza estado com dados recebidos
        _callbackInfo.value = CallbackInfo(
            scheme = uri.scheme ?: "",
            host = uri.host ?: "",
            parameters = parameters
        )
    }
    
    fun clearCallback() {
        _callbackInfo.value = CallbackInfo()
    }
    
    fun hasCallbackData(): Boolean {
        return _callbackInfo.value.scheme.isNotEmpty() && _callbackInfo.value.host.isNotEmpty()
    }
    
    fun getParameter(key: String): String? {
        return _callbackInfo.value.parameters[key]
    }
    
    fun getAllParameters(): Map<String, String> {
        return _callbackInfo.value.parameters
    }
}
