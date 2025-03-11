<p align='center'>
  <a href='https://unico.io'>
    <img width='350' src='https://unico.io/wp-content/uploads/2024/05/idcloud-horizontal-color.svg'></img>
  </a>
</p>

<h1 align='center'>CBU - WEBVIEW - Android</h1>


<div align='center'>
  
  ### POC de implementação do By Unico em Android
  

  <br/>
</div>

Esse é um projeto de apoio para testes e implementação, todos os passos abaixo já foram inseridos nele, mas segue para conhecimento e adaptação:  <br/>

## ✨ Passo 1: Usando CustomTabs para integração

1 - Insira no app/build.gradle a dependência necessária para o uso de CustomTabs:


```kotlin
implementation("androidx.browser:browser:1.5.0")
```
---
## ✨ Passo 2: Abrindo uma CustomTab


```kotlin
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent

class CustomTabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)

       openCustomTab(<URL_CBU>)
    }

    fun openCustomTab(url: String) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(url))
    }
}
```
---

## ✨ Passo 3: Modificando AndroidManifest

iColoque no AndroidManifest.xml as permissões e intents necessários na Activity que deseja receber a callback_uri.
É necessário incluir o atributo `android:launchMode="singleTop"` como também a tag `<data>` informando os dados da URI.
```kotlin
<uses-feature android:name="android.hardware.camera" android:required="false"/>
<uses-permission android:name="android.permission.CAMERA"/> 
// necessário ter as permissões de câmera e geolocalização 

<activity
    android:name=".CustomTabActivity"
    android:exported="true"
    android:label="@string/app_name"
    android:theme="@style/Theme.Customtabs"
    android:launchMode="singleTop">

    <intent-filter android:label="Custom Tab">
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <!-- scheme e host são os dados fornecidos na criação de um processo no campo callback_uri
        callback_uri: "foobar://success?code=1234" -->
        <data android:scheme="foobar" android:host="success"/>
    </intent-filter>

</activity>
```

As seguintes permissões são necessárias para funcionar corretamente:

* Câmera

* Geolocalização

---

## ✨ Passo 4: Pegando informações de retorno

Para pegar as informações de redirect com os dados fornecidos, você pode usar o seguinte código no método `onNewIntent` da sua Activity:

```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)

    val url = intent.data
    val scheme = url.scheme // "foobar"
    val host = url.host // "success"
    val code = url.getQueryParameter("code") // "1234"
}
```



---

## ✨ Link da nossa documentacao: 

https://devcenter.unico.io/idcloud/integracao/integracao-by-unico/controlando-a-experiencia/redirecionando-o-usuario
