package io.test.customtab

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession

class CustomTabManager(private val context: Context) {

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
            customTabsSession = customTabsClient?.newSession(object : CustomTabsCallback() {
                override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                    super.onNavigationEvent(navigationEvent, extras)
                    // CALLBACK: Monitor de navegação no Custom Tab
                    if (navigationEvent == CustomTabsCallback.NAVIGATION_FINISHED) {
                        println("Custom Tab: Navegação finalizada - pronto para callback")
                    }
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName) {
            customTabsClient = null
            customTabsSession = null
        }
    }

    fun initialize() {
        bindCustomTabsService()
    }

    fun cleanup() {
        try {
            if (context is androidx.activity.ComponentActivity) {
                // CALLBACK: Desconecta serviço quando app é fechado
                context.unbindService(customTabsServiceConnection)
            }
        } catch (e: Exception) {
            // Serviço já desconectado
        }
    }

    private fun bindCustomTabsService() {
        val packageName = CustomTabsClient.getPackageName(context, emptyList())
        if (packageName != null) {
            CustomTabsClient.bindCustomTabsService(context, packageName, customTabsServiceConnection)
        }
    }

    fun openCustomTab(url: String) {
        try {
            val uri = Uri.parse(url)
            customTabsSession?.mayLaunchUrl(uri, null, null)
            val customTabsIntent = createCustomTabsIntent()
            customTabsIntent.launchUrl(context, uri)
            println("Custom Tab aberto com sucesso para: $url")

        } catch (e: Exception) {
            println("Erro ao abrir Custom Tab: ${e.message}")
            openFallbackBrowser(url)
        }
    }

    private fun createCustomTabsIntent(): CustomTabsIntent {
        return CustomTabsIntent.Builder(customTabsSession).apply {
            setShowTitle(true)
            setUrlBarHidingEnabled(true)
            setToolbarColor(UNICO_PRIMARY_COLOR)
            setSecondaryToolbarColor(UNICO_SECONDARY_COLOR)

            if (context is androidx.activity.ComponentActivity) {
                setStartAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }

            setInstantAppsEnabled(false)

        }.build().apply {
            intent.apply {
                putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + context.packageName))

                // CALLBACK: Configurações para retorno automático ao app
                putExtra("android.support.customtabs.extra.user_opt_out_from_session", false)
                putExtra("androidx.browser.customtabs.extra.user_opt_out_from_session", false)

                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

                putExtra("com.android.browser.application_id", context.packageName)
            }
        }
    }

    private fun openFallbackBrowser(url: String) {
        try {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(fallbackIntent)
            }
        } catch (fallbackError: Exception) {
            println("Erro no fallback: ${fallbackError.message}")
        }
    }
}