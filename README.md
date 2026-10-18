# MetroApp: Control de Saldo, Pasajes y Gastos (Línea 1 del Metro de Lima)

Aplicación móvil Android nativa desarrollada en **Kotlin** con **Jetpack Compose (Material 3)**, **Room (SQLite)** y **WorkManager**, diseñada para administrar tarjetas de transporte, registrar viajes, recargas y conciliaciones de saldo, aplicar tarifas automáticas y generar reportes financieros con exportación a **CSV** y **PDF**, con funcionamiento **100% offline-first**.

---

## 🚀 Características Principales

1. **Dashboard Financiero Completo**:
   - Saldo calculado disponible vs. último saldo real verificado.
   - Cálculo automático de discrepancias contables.
   - Total gastado hoy, durante la semana y en el mes.
   - Gráfico de barras vectorial con gastos de los últimos 7 días.
   - Acceso directo para registrar viajes, recargar y conciliar.
   - Banner interactivo de viajes programados pendientes.

2. **Gestión Multitarjeta**:
   - Soporte para tarjetas de tipo **Adulto**, **Escolar** y **Universitario/Instituto**.
   - Saldo inicial configurable, identificación opcional de número físico y notas.
   - Selección de tarjeta predeterminada.

3. **Motor de Tarifas y Calendario Peruano**:
   - Cálculo automático de tarifas según tipo de tarjeta, fecha y hora en zona horaria `America/Lima`.
   - Soporte de medio pasaje de lunes a viernes para escolares (con periodo de vigencia configurable) y de lunes a sábado para universitarios.
   - Detección de domingos y feriados nacionales peruanos (los feriados anulan el medio pasaje aplicando tarifa completa de S/ 1.50).
   - Calendario editable de feriados peruanos (añadir o eliminar fechas festivas).
   - Guardado inmutable de la tarifa aplicada en cada transacción histórica.

4. **Automatización Inteligente (WorkManager)**:
   - Programación en segundo plano de dos viajes diarios (mañana y tarde).
   - Prevención de duplicados mediante clave idempotente (`cardId_YYYYMMDD_slot`).
   - Movimientos generados como **PENDIENTES** por defecto (sin alterar el saldo hasta su confirmación).
   - Opción de autoconfirmación configurable.
   - Notificaciones interactivas para confirmar o descartar.

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
