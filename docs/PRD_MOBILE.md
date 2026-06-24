**Product Requirements Document (PRD)**

**Proyecto:** DentiCore Mobile (App Nativa Android)

**Naturaleza:** Cliente Frontend (BFF Consumer)

**Versión:** 1.0

**1. Propósito del Documento**

Este documento define los requisitos, restricciones y el alcance operativo para la construcción de la aplicación DentiCore Mobile. Actúa como el contrato técnico inmutable para el desarrollo de la interfaz nativa en Android utilizando Kotlin y Jetpack Compose. La aplicación operará exclusivamente como una capa de presentación y consumo de los endpoints RESTful ya definidos en el API Gateway de DentiCore 2.0 (Spring Boot), y actuará como receptor de notificaciones asíncronas para la fidelización de pacientes.

**2. Alcance y Exclusiones**

Para garantizar un desarrollo ágil y un despliegue viable del MVP (Producto Mínimo Viable) en el corto plazo estipulado, se establecen límites estrictos sobre la responsabilidad de la aplicación móvil.

**2.1. In-Scope (En alcance)**

El desarrollo y codificación del cliente móvil se restringirá a:

* **Autenticación y Seguridad Local:** Consumo del endpoint /auth/login y almacenamiento local encriptado del token JWT en el dispositivo (ej. EncryptedSharedPreferences).
* **Autogestión de Citas (Frontend):** Interfaz para consultar horarios (consumo de /citas/disponibilidad) y solicitar reservas (petición POST a /citas), manejando las respuestas HTTP de éxito (201) o conflicto de concurrencia (409).
* **Visualización Clínica (Lectura):** Interfaz para renderizar el historial clínico y el estado del odontograma en base a los datos proveídos por el endpoint /clinica/odontograma (o su equivalente de consulta de historial).
* **Recepción Push Asíncrona:** Integración del SDK de Firebase Cloud Messaging (FCM) para interceptar notificaciones silenciosas y visuales provenientes del Message Broker (RabbitMQ) del backend.

**2.2. Out-of-Scope (Fuera de alcance)**

Queda explícitamente excluido del desarrollo de este proyecto móvil:

* **Desarrollo Multiplataforma (iOS):** La aplicación será 100% nativa para el ecosistema Android; no se utilizarán frameworks híbridos (Flutter/React Native) ni se compilará para dispositivos Apple.
* **Lógica de Negocio Transaccional:** La app móvil **no** realizará cálculos de presupuestos, **no** cruzará horarios internamente, ni procesará validaciones de la base de datos PostgreSQL. Toda la lógica ACID pertenece al backend.
* **Pasarelas de Pago Nativas:** No se integrarán SDKs de cobro (Stripe, MercadoPago, Niubiz) dentro de la aplicación móvil en esta fase.
* **Gestión Back-Office:** La aplicación móvil está destinada exclusivamente al rol de *Paciente*. Las interfaces para el rol de *Odontólogo* o *Administrador* (modificación de odontogramas, facturación) corresponden al proyecto DentiCore 2.0 Web y no existirán en la app.

## 3. Modelado de Procesos Críticos (Perspectiva Móvil)

El cliente móvil interactúa de forma pasiva (lectura) y activa (solicitudes), delegando el procesamiento al *Backend for Frontend* (BFF).

### 3.1. Flujo de Autenticación y Retención de Sesión

* **Escenario As-Is:** El paciente no posee un canal digital propio; la identificación se basa en el número de teléfono al comunicarse por WhatsApp, careciendo de validación de identidad formal.
* **Escenario To-Be (DentiCore Mobile):** El paciente ingresa sus credenciales en la aplicación nativa. La app emite un POST al *endpoint* de autenticación y recibe un Token JWT. Este token se inyecta en el almacenamiento seguro del sistema operativo Android (EncryptedSharedPreferences), garantizando que el usuario no deba iniciar sesión en cada apertura de la app, logrando la "fricción cero".

### 3.2. Flujo de Agendamiento Autogestionado

* **Escenario As-Is:** Negociación asíncrona manual propensa a colisiones de horarios por demoras en la respuesta humana.
* **Escenario To-Be (DentiCore Mobile):** La aplicación consume el *endpoint* GET /citas/disponibilidad y renderiza un calendario nativo. Al seleccionar un cupo, la app envía la solicitud (POST /citas). Si el BFF responde con un HTTP 409 (Conflicto), la interfaz alerta al usuario que el turno fue tomado por alguien más y refresca la vista automáticamente, asegurando consistencia de datos sin lógica compleja en el cliente.

### 3.3. Flujo de Fidelización y Alertas (Push)

* **Escenario As-Is:** Recepcionistas extraen manualmente la agenda diaria y envían recordatorios uno por uno, generando cuellos de botella y errores por omisión.
* **Escenario To-Be (DentiCore Mobile):** La aplicación implementa un servicio en segundo plano (ej. FirebaseMessagingService) que escucha activamente los *payloads* enviados por el sistema de mensajería del backend (FCM/RabbitMQ). La alerta se renderiza de forma nativa en el centro de notificaciones del dispositivo Android, dirigiendo al usuario a la pantalla de detalle de su cita mediante un *deeplink* al hacer *tap*.

## 4. Requisitos Funcionales (Historias de Usuario Épicas)

El desarrollo del MVP móvil se segmenta en cuatro Épicas funcionales, correlacionadas directamente con los Casos de Uso Móvil (CUM).

* **EPIC-M01 (Autenticación y Red Segura - CU-M01):** La aplicación debe renderizar una interfaz de inicio de sesión nativa y gestionar el estado de carga. A nivel de red, debe incorporar un interceptor HTTP en el cliente (Retrofit/OkHttp) que inyecte automáticamente el Token JWT almacenado localmente en las cabeceras de todas las peticiones subsecuentes.
* **EPIC-M02 (Gestor de Citas UI - CU-M02):** El sistema móvil debe proveer una interfaz gráfica mediante Jetpack Compose para visualizar listas de horarios disponibles. Debe manejar los estados reactivos de la solicitud de reserva (Carga, Éxito, Error de Red, Conflicto de Concurrencia) y notificar al usuario visualmente.
* **EPIC-M03 (Visualizador Clínico - CU-M03):** La aplicación debe poseer un módulo de "Mi Historial" que consuma el *endpoint* de datos clínicos del paciente. Debe deserializar la respuesta JSON para listar cronológicamente las atenciones pasadas y mostrar una representación gráfica (solo lectura) del estado actual del odontograma.
* **EPIC-M04 (Receptor de Notificaciones - CU-M04):** El dispositivo móvil debe solicitar los permisos nativos de Android (Android 13+) para mostrar notificaciones. Debe procesar *payloads* silenciosos recibidos desde Firebase Cloud Messaging (FCM) y renderizar alertas visuales que informen sobre cambios de estado en sus citas sin necesidad de mantener la aplicación abierta.

**5. Requisitos No Funcionales (NFRs) y Stack Tecnológico Móvil**

Para asegurar un rendimiento óptimo en dispositivos Android, la aplicación se regirá por restricciones que garantizan la seguridad, resiliencia y escalabilidad del cliente móvil.

**5.1. Requisitos No Funcionales (NFRs)**

* **NFR-M01 (Seguridad en Reposo):** El Token JWT recibido por el API Gateway nunca debe almacenarse en texto plano dentro del dispositivo. Se exige el uso imperativo de EncryptedSharedPreferences o Proto DataStore con cifrado AES-256 gestionado por el Android Keystore System.
* **NFR-M02 (Rendimiento e Hilo Principal):** Ninguna petición de red o serialización de datos JSON puede ejecutarse en el hilo principal de la interfaz de usuario UI. Todas las llamadas a las APIs RESTful deben ser asíncronas y gestionadas mediante Corrutinas de Kotlin bajo el despachador Dispatchers.IO.
* **NFR-M03 (Tolerancia a Fallos y Estado de Red):** La aplicación debe interceptar la pérdida de conectividad antes de emitir ráfagas de red HTTP hacia el BFF. Ante la ausencia de red, se debe mostrar una alerta de "Sin conexión" , evitando excepciones no controladas (*crashes*) y aplicando el principio de degradación elegante de la UI.
* **NFR-M04 (Compatibilidad Mínima):** El artefacto de software APK debe configurarse con un minSdkVersion 26 (Android 8.0 Oreo), asegurando compatibilidad con el ecosistema moderno y soporte nativo para los permisos estrictos de notificaciones exigidos en Android 13+ (API 33).

**5.2. Stack Tecnológico Estricto del Cliente Móvil**

* **Lenguaje de Programación:** Kotlin (versión LTS estable).
* **Framework de UI:** Jetpack Compose para interfaces de usuario declarativas y reactivas.
* **Gestión de Navegación:** Jetpack Navigation Compose para el control de rutas e inyección de *deeplinks*.
* **Cliente HTTP y Red:** Retrofit 2 integrado con OkHttp 3 para el enrutamiento y manejo de interceptores.
* **Serialización:** Gson o Kotlinx Serialization para la transformación exacta de DTOs.
* **Mensajería Push:** SDK Nativo de Firebase Cloud Messaging (FCM) acoplado a la infraestructura de RabbitMQ del backend.

**6. Hoja de Ruta de Desarrollo Móvil (Sprint Roadmap)**

El desarrollo del cliente móvil está sincronizado con el ciclo de vida del proyecto principal de backend para permitir integraciones progresivas y mitigar errores en el ecosistema contenerizado.

Plaintext

Fase 1: Setup y Cimientos de Red (Sprints 1 y 2)

│ ├── Inicialización del proyecto nativo en Android Studio y Gradle.

│ ├── Configuración de la estructura de paquetes por capas (Clean Architecture).

│ └── Implementación de RetrofitClient y JwtAuthInterceptor para la cabecera Bearer.

│

Fase 2: Implementación de Seguridad Local (Sprint 3)

│ ├── Construcción de la UI de Login (Jetpack Compose) vinculada a AuthViewModel (CU-M01).

│ ├── Integración del endpoint /auth/login expuesto por el API Gateway.

│ └── Configuración del almacenamiento seguro de tokens con EncryptedSharedPreferences.

│

Fase 3: Módulos de Lectura y Visualización Clínica (Sprint 4)

│ ├── Desarrollo del módulo de "Mi Historial" consumiendo los servicios del Core Clínico (CU-M03).

│ └── Deserialización de payloads JSON complejos y renderizado nativo de la lista de atenciones.

│

Fase 4: Transaccionalidad y Manejo de Concurrencia (Sprint 5)

│ ├── Construcción de la interfaz de selección de citas mediante un calendario nativo (CU-M02).

│ ├── Consumo del endpoint de horarios disponibles mediante peticiones GET estructuradas.

│ └── Programación de manejadores de errores para respuestas HTTP 409 (Horario Ocupado).

│

Fase 5: Integración de Eventos Asíncronos y Empaquetado (Sprints 6 y 7)

├── Configuración del FirebaseMessagingService para interceptar payloads en segundo plano (CU-M04).

├── Vinculación de alertas visuales en el dispositivo con redirección por deeplinks a la UI.

└── Pruebas de extremo a extremo (E2E) con los contenedores Docker y compilación del APK final.