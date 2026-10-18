# Metro - Línea 1: Control de Saldo, Pasajes y Gastos

Aplicación móvil Android nativa desarrollada en **Kotlin** con **Jetpack Compose (Material 3)**, **Room (SQLite)** y **WorkManager**, diseñada para la red del Metro de Lima (Línea 1). Administra tarjetas de transporte, registra viajes, recargas y conciliaciones de saldo, aplica tarifas automáticas inteligentes y genera reportes financieros con exportación a **CSV** y **PDF**, con funcionamiento **100% offline-first**. Incorpora un **Screen Load (Splash animado)** con identidad visual oficial.

---

## 🚀 Características Principales

1. **Dashboard Financiero Completo**:
   - Saldo calculado disponible vs. último saldo real verificado.
   - Cálculo automático de discrepancias contables.
   - Total gastado hoy, durante la semana y en el mes.
   - Gráfico de barras vectorial con gastos de los últimos 7 días.
   - Acceso directo para registrar viajes, recargar y conciliar.
   - Banner interactivo de viajes programados pendientes.

2. **Gestión Multitarjeta y Selección Rápida**:
   - Soporte para tarjetas con tarifas asignables dinámicamente (**Adulto**, **Escolar**, **Universitario** y cualquier tarifa personalizada creada por el usuario).
   - Selector rápido mediante ventana modal desde el **Menú Principal (Dashboard)** y desde la sección de **Recargas**, sin necesidad de navegar hasta configuración.
   - Saldo inicial configurable, identificación opcional de número físico y notas.
   - Selección de tarjeta predeterminada y eliminación de tarjeta con migración segura de historial de viajes/movimientos hacia otra tarjeta activa.

3. **Motor de Tarifas y Tarifario de Referencia Administrable (CRUD Completo)**:
   - **Gestión Total en Ajustes**: Permite **añadir nuevas tarifas**, **editar** tarifas existentes (L-V, sábados, domingos, feriados y periodos de vigencia opcionales) y **eliminar** tarifas que ya no se usen (con validación de integridad referencial si están asignadas a tarjetas).
   - **Vinculación Dinámica**: Las tarifas creadas en el tarifario aparecen automáticamente como opciones seleccionables al registrar o editar cualquier tarjeta.
   - Cálculo automático de tarifas según tipo de tarjeta, fecha y hora en zona horaria `America/Lima`.
   - Soporte de medio pasaje de lunes a viernes para escolares (con periodo de vigencia configurable) y de lunes a sábado para universitarios o perfiles especiales.
   - Detección de domingos y feriados nacionales peruanos (los feriados anulan el medio pasaje aplicando tarifa de feriado configurada).
   - Calendario editable de feriados peruanos (añadir o eliminar fechas festivas).
   - Guardado inmutable de la tarifa aplicada en cada transacción histórica.

4. **Automatización Inteligente de Viajes Diarios (CRUD Completo y Calendario)**:
   - **Gestión Total Editable**: Título personalizable, activación/desactivación general y modo de autoconfirmación (descontar directo o dejar como pendiente).
   - **Horarios Configurables**: Ajuste independiente de la hora de viaje matutino (ej. `07:30`) y vespertino (ej. `18:00`).
   - **Cantidad de Viajes Diarios Personalizable**: Opciones predefinidas de 1 o 2 viajes al día, además de una opción **Personalizada** que permite al usuario definir libremente cualquier cantidad de viajes diarios (3, 4, 5, etc.) según su rutina.
   - **Asignación Dinámica de Tarjeta**: Selector para elegir explícitamente a qué tarjeta registrada cargar los viajes automáticos o utilizar la predeterminada.
   - **Calendario y Selección de Días**: Selector de días de la semana activos (*Lun, Mar, Mié, Jue, Vie, Sáb, Dom*) y soporte para fechas específicas (`YYYY-MM-DD`). Solo genera viajes en los días seleccionados.
   - **Eliminación / Restablecimiento**: Opción para eliminar la configuración y restablecerla a valores de fábrica, cancelando las tareas en curso de WorkManager.
   - Prevención de duplicados mediante clave idempotente (`cardId_YYYYMMDD_slot`) y notificaciones interactivas.

5. **Conciliación de Saldo Físico**:
   - Comparación del saldo calculado contra el saldo real verificado en los torniquetes o estaciones.
   - Registro automático de transacciones de ajuste (positivo o negativo) con motivo obligatorio y bitácora de auditoría.

6. **Historial y Auditoría**:
   - Filtros por tipo (Viajes, Recargas, Ajustes, Pendientes, Anulados).
   - Capacidad de anular transacciones (reversión de saldo sin pérdida de trazabilidad).
   - Capacidad de corrección y edición con recálculo atómico del saldo.

7. **Reportes y Exportación**:
   - Resumen de gastos, proyecciones de fin de mes y promedios diarios.
   - Gráficos nativos en Jetpack Compose Canvas (gastos diarios y comparativa mensual).
   - Exportación nativa a **CSV** y **PDF** formateado mediante `PdfDocument` y compartición directa vía Android Sharesheet.

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje**: Kotlin 2.2.10
- **UI**: Jetpack Compose con Material 3 (1.9+)
- **Base de Datos**: Room 2.7.2 sobre SQLite local
- **Procesamiento de Anotaciones**: KSP 2 (`ksp-2.2.10-2.0.2`)
- **Tareas de Fondo**: AndroidX WorkManager 2.9.1
- **Arquitectura**: MVVM + Clean Architecture con StateFlow reactivo
- **Precisión Monetaria**: Céntimos enteros (`Long`), 0% de errores de punto flotante binario IEEE 754.

---

## 📂 Estructura del Código

```text
com.metroapp.lima/
├── data/
│   ├── local/
│   │   ├── MetroDatabase.kt            # Base de datos Room con callback prepoblado
│   │   ├── dao/
│   │   │   ├── CardDao.kt
│   │   │   ├── TransactionDao.kt
│   │   │   └── OtherDaos.kt            # HolidayDao, FareRuleDao, AuditLogDao, etc.
│   │   └── entity/
│   │       ├── CardEntity.kt
│   │       ├── TransactionEntity.kt
│   │       └── OtherEntities.kt
│   ├── repository/
│   │   └── MetroRepository.kt          # Repositorio unificado con transacciones atómicas
│   └── worker/
│       ├── DailyTripWorker.kt          # Worker de WorkManager con notificaciones
│       └── AutomationScheduler.kt     # Programador de periodicidad
├── domain/
│   ├── engine/
│   │   └── TariffEngine.kt             # Motor de cálculo tarifario de la Línea 1
│   ├── model/
│   │   ├── Enums.kt                    # CardType, MovementType, TransactionStatus
│   │   └── Models.kt                   # Modelos de dominio limpios
│   └── usecase/
│       ├── StatsUseCase.kt             # Métricas, promedios y proyecciones
│       └── ExportUseCase.kt            # Generación nativa de CSV y PDF
├── ui/
│   ├── components/
│   │   └── Charts.kt                   # Gráficos vectoriales en Canvas
│   ├── navigation/
│   │   └── AppNavigation.kt            # Barra de navegación con 5 secciones
│   ├── screens/
│   │   ├── dashboard/DashboardScreen.kt
│   │   ├── transactions/TransactionsScreen.kt
│   │   ├── topup/TopUpScreen.kt
│   │   ├── reports/ReportsScreen.kt
│   │   └── settings/SettingsScreen.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   └── Theme.kt                    # Colores oficiales de la Línea 1
│   └── viewmodel/
│       └── MetroViewModel.kt           # ViewModel principal reactivo
└── util/
    └── FormatUtils.kt                  # Manejo de moneda (S/) y fecha en America/Lima
```

---

## 📲 Instalación y Ejecución

### 1. Ubicación del APK Generado
El APK de depuración se encuentra listo para instalar en:
```text
app/build/outputs/apk/debug/app-debug.apk
```

### 2. Instalación en Dispositivo Físico o Emulador
Para instalar en tu celular Android conectado por USB (con depuración USB activada):
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

O transfiere directamente el archivo `app-debug.apk` a tu celular mediante WhatsApp, Drive o cable USB, y ábrelo para instalar (habilitando *Instalar aplicaciones de fuentes desconocidas* si te lo solicita el sistema).

### 3. Compilación desde Código Fuente
Para compilar y ejecutar los tests automatizados:
```powershell
# Ejecutar todas las pruebas unitarias
.\gradlew.bat testDebugUnitTest

# Compilar nuevo APK de depuración
.\gradlew.bat assembleDebug
```
