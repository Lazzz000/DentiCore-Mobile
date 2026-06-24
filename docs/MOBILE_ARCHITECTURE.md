# Directrices Estrictas de Arquitectura y Código (DentiCore Mobile)

Este documento define las reglas de ingeniería innegociables para la generación de código en este proyecto. Todo código generado debe adherirse a los siguientes estándares:

## 1. Patrón Arquitectónico (Clean Architecture + MVVM)
* **Presentación:** UI reactiva gestionada por ViewModels.
* **Dominio:** Modelos de negocio puros, interfaces de repositorios y Casos de Uso (Use Cases) si la lógica lo amerita.
* **Datos:** Implementación de repositorios, DTOs y clientes de red (Retrofit).

## 2. Stack Tecnológico Restringido
* **UI:** Únicamente **Jetpack Compose**. Está estrictamente prohibido usar vistas XML o Fragments.
* **Estado:** Uso exclusivo de `StateFlow` y `MutableStateFlow` en los ViewModels. Prohibido usar `LiveData`.
* **Navegación:** Jetpack Navigation Compose.
* **Red:** Retrofit 2 + OkHttp 3. Todas las llamadas deben ser funciones `suspend` utilizando Corrutinas (Coroutines) en `Dispatchers.IO`. Prohibido usar callbacks tradicionales (Call<T>).
* **Serialización:** Gson.

## 3. Reglas de Seguridad (Hard Rules)
* El Token JWT devuelto por el login NUNCA debe guardarse en SharedPreferences estándar ni en variables de memoria estáticas vulnerables a la recolección de basura.
* Se exige el uso de la librería `androidx.security:security-crypto` (`EncryptedSharedPreferences`) para guardar cualquier credencial o token.
* Toda petición HTTP (excepto el login público) debe incluir el token en la cabecera mediante un interceptor de OkHttp (`Interceptor`).

## 4. Calidad de Código
* Todo el código debe estar escrito en Kotlin.
* Prevenir la lógica de negocio en funciones `@Composable`. La UI debe ser pasiva y limitarse a emitir intenciones (Intents/Events) al ViewModel.