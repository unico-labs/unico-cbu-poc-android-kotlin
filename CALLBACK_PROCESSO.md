# 🔄 Processo de Callback do Custom Tab - Guia Técnico

## 📋 Visão Geral

Este documento explica como o sistema de callback funciona no aplicativo Unico Custom Tab, detalhando cada etapa desde a abertura da URL até o fechamento automático do Custom Tab.

---

## 🔧 Como o Callback é Configurado

### 1. **Configuração no AndroidManifest.xml**

O callback é configurado através de **Deep Links** no manifest:

```xml
<!-- Deep Link customizado para callback -->
<intent-filter android:label="App CustomTab Callback" android:priority="999">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    
    <!-- Configuração da URL de callback -->
    <data android:scheme="foobar" android:host="success"/>
</intent-filter>
```

**O que isso significa**:
- Quando uma URL com formato `foobar://success?parametros` for acessada
- O Android automaticamente **retorna para o nosso app**
- A Activity é aberta em modo `singleTop` (não cria nova instância)

---

## 📡 Como Detectamos que o Callback Foi Recebido

### 1. **Método `onNewIntent()` na MainActivity**

```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    // CALLBACK: Recebe URL de retorno do Custom Tab
    callbackHandler.processCallback(intent)
}
```

**Processo**:
1. **Custom Tab navega** para URL de callback (ex: `foobar://success?code=1234`)
2. **Android detecta** o scheme `foobar://` 
3. **Automaticamente retorna** para nosso app
4. **`onNewIntent()` é chamado** com os dados da URL

### 2. **Processamento no CallbackHandler**

```kotlin
fun processCallback(intent: Intent) {
    // CALLBACK: Processamento da URL de retorno do Custom Tab
    println("=== CALLBACK RECEBIDO ===")
    val uri = intent.data
    
    if (uri != null) {
        logUriInfo(uri)        // Log detalhado
        updateCallbackInfo(uri) // Atualiza estado da UI
        println("✅ Callback processado com sucesso!")
    }
}
```

**Dados extraídos**:
- **Scheme**: `foobar`
- **Host**: `success` 
- **Parâmetros**: `code=1234`, `status=completed`, etc.

---

## ✅ Como Verificamos que o Processo Foi Concluído

### 1. **Verificação Automática de Dados**

```kotlin
fun hasCallbackData(): Boolean {
    return _callbackInfo.value.scheme.isNotEmpty() && 
           _callbackInfo.value.host.isNotEmpty()
}
```

**Critérios de conclusão**:
- ✅ **Scheme válido** recebido (`foobar`)
- ✅ **Host válido** recebido (`success`)
- ✅ **Parâmetros extraídos** e armazenados
- ✅ **Estado da UI atualizado** automaticamente

### 2. **Exibição Visual na Interface**

```kotlin
// CALLBACK: Exibe informações quando URL retorna dados
if (callbackHandler.hasCallbackData()) {
    CallbackInfoSection() // Mostra seção com dados recebidos
}
```

**O usuário vê**:
```
✅ Callback Recebido:
Scheme: foobar
Host: success
Parâmetros:
  • code: 1234
  • status: completed
  • user_id: abc123
```

---

## 🔒 Como o Custom Tab é Fechado Automaticamente

### 1. **Fechamento Automático via Deep Link**

**Fluxo técnico**:
1. **URL de callback é acessada** no Custom Tab
2. **Android detecta** o scheme registrado (`foobar://`)
3. **Custom Tab perde foco** automaticamente
4. **App principal ganha foco** via `onNewIntent()`
5. **Custom Tab fica em background** ou é encerrado pelo sistema

### 2. **Configurações que Garantem o Fechamento**

No `CustomTabManager.kt`:

```kotlin
intent.apply {
    // CALLBACK: Configurações para retorno automático ao app
    putExtra("android.support.customtabs.extra.user_opt_out_from_session", false)
    putExtra("androidx.browser.customtabs.extra.user_opt_out_from_session", false)
    
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
}
```

**Essas configurações**:
- ✅ **Eliminam confirmação** de retorno ao app
- ✅ **Garantem transição suave** entre Custom Tab e app
- ✅ **Evitam múltiplas instâncias** da Activity

### 3. **Monitoramento de Navegação**

```kotlin
customTabsSession = customTabsClient?.newSession(object : CustomTabsCallback() {
    override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
        // CALLBACK: Monitor de navegação no Custom Tab
        if (navigationEvent == CustomTabsCallback.NAVIGATION_FINISHED) {
            println("Custom Tab: Navegação finalizada - pronto para callback")
        }
    }
})
```

**Estados monitorados**:
- 🔄 **NAVIGATION_STARTED**: Página começou a carregar
- 🔄 **NAVIGATION_FINISHED**: Página terminou de carregar
- 🔄 **TAB_SHOWN**: Custom Tab foi exibido
- 🔄 **TAB_HIDDEN**: Custom Tab foi ocultado (callback executado)

---

## 📊 Fluxo Completo Step-by-Step

### **Passo 1: Abertura do Custom Tab**
```
[App] → openUrl() → CustomTabManager → launchUrl() → [Custom Tab Aberto]
```

### **Passo 2: Usuário Interage no Custom Tab**
```
[Custom Tab] → Usuário preenche formulário → Clica "Concluir" → [Processo Unico executado]
```

### **Passo 3: Redirecionamento para Callback**
```
[Servidor Unico] → Redireciona para: foobar://success?code=1234&status=ok
```

### **Passo 4: Android Detecta Deep Link**
```
[Android System] → Detecta "foobar://" → Localiza app → Prepara Intent
```

### **Passo 5: Retorno Automático ao App**
```
[Custom Tab] → Perde foco → [App] → Ganha foco → onNewIntent() → [Callback processado]
```

### **Passo 6: Atualização da Interface**
```
[CallbackHandler] → Extrai dados → Atualiza estado → [UI mostra resultado]
```

### **Passo 7: Custom Tab Fechado**
```
[Sistema] → Custom Tab em background → Coletado pelo GC → [Memória liberada]
```

---

## 🔍 Como Identificar Problemas no Callback

### **Logs de Debugging**

O sistema gera logs detalhados para identificar problemas:

```bash
# Sucesso
=== CALLBACK RECEBIDO ===
URI completo: foobar://success?code=1234
Scheme: foobar
Host: success
✅ Callback processado com sucesso!

# Problema
⚠️ Intent recebido sem URI data
```

### **Problemas Comuns e Soluções**

| Problema | Causa | Solução |
|----------|-------|---------|
| Callback não recebido | Deep link mal configurado | Verificar AndroidManifest.xml |
| Custom Tab não fecha | Flags incorretas | Verificar configurações do Intent |
| Dados não extraídos | URL mal formada | Verificar formato da URL de callback |
| App não abre | Priority baixa | Aumentar android:priority no manifest |

---

## 🛡️ Garantias de Funcionamento

### **Redundâncias Implementadas**

1. **Múltiplos Schemes de Callback**:
   - `foobar://success` (principal)
   - `https://customtab.test.io/callback` (alternativo)

2. **Fallbacks Automáticos**:
   - Se Custom Tab falhar → Abre navegador padrão
   - Se callback falhar → Mantém sessão ativa
   - Se dados não chegarem → Interface permanece responsiva

3. **Tratamento de Erros**:
   - Try/catch em todas as operações críticas
   - Logs detalhados para debugging
   - Estados de fallback definidos

---

## 🎯 Conclusão Técnica

O sistema de callback é **robusto e automático**:

✅ **Configuração**: Deep links registrados corretamente  
✅ **Detecção**: `onNewIntent()` captura retorno automaticamente  
✅ **Processamento**: Dados extraídos e validados  
✅ **Interface**: Atualizada automaticamente via estado reativo  
✅ **Fechamento**: Custom Tab fechado automaticamente pelo Android  

O processo é **transparente para o usuário** e **confiável tecnicamente**, garantindo uma experiência fluida entre o Custom Tab e o aplicativo principal.
