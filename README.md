# Grupo1_Módulo PIM — Automatización de pruebas del módulo PIM (OrangeHRM)

Framework de pruebas end-to-end con **Playwright (Java)**, **Cucumber (BDD)** y **JUnit 5**, bajo el patrón **Page Object Model (POM)**. Prueba el demo público de OrangeHRM: autenticación y gestión de empleados (búsqueda, registro, edición y eliminación).

- **Sistema bajo prueba:** https://opensource-demo.orangehrmlive.com/ (usuario `Admin`, clave `admin123`)
- **Escenarios:** se escriben en Gherkin; los nuevos usan español (`# language: es`)

---

## 1. Requisitos previos

| Herramienta | Versión | Verificación |
|---|---|---|
| JDK | 17 o superior | `java -version` |
| Maven | 3.8 o superior | `mvn -v` |
| Acceso a internet | — | Descarga de dependencias y acceso al demo de OrangeHRM |

No hace falta instalar navegadores por separado: Playwright descarga Chromium automáticamente en la primera ejecución (puede tardar unos minutos).

---

## 2. Cómo ejecutar las pruebas

> En **Windows PowerShell** el argumento `-D...` debe ir **completo entre comillas**; de lo contrario PowerShell lo parte en el punto y Maven falla con `Unknown lifecycle phase`.

### Ejecutar todo

```powershell
mvn test
```

### Ejecutar por etiqueta (tag)

```powershell
mvn test "-Dcucumber.filter.tags=@HU_01"        # autenticación
mvn test "-Dcucumber.filter.tags=@Busqueda"     # búsqueda de empleados
mvn test "-Dcucumber.filter.tags=@Edicion"      # edición de empleados
mvn test "-Dcucumber.filter.tags=@Eliminacion"  # eliminación de empleados
mvn test "-Dcucumber.filter.tags=@PIM"          # todos los de PIM
mvn test "-Dcucumber.filter.tags=@PIM and not @Eliminacion"   # combinaciones
```

### Ejecutar un archivo `.feature` o un escenario

```powershell
mvn test "-Dcucumber.features=src/test/resources/features/EliminacionEmpleado.feature"
mvn test "-Dcucumber.filter.name=Registrar un empleado nuevo"
```

### Desde IntelliJ IDEA

Pulsa el triángulo verde junto a `Característica:` / `Feature:` para correr todo el archivo, o junto a un `Escenario:` para correr solo ese.

### Opciones de ejecución

| Propiedad | Valor por defecto | Descripción |
|---|---|---|
| `-Dheadless=true` | `false` | Ejecuta sin abrir ventana del navegador (recomendado en CI) |
| `-DslowMo=300` | `0` | Pausa en ms entre acciones; útil para que el video sea legible |

```powershell
mvn test "-Dheadless=true" "-Dcucumber.filter.tags=@Busqueda"
```

---

## 3. Resultados y evidencia

Cada ejecución genera en `target/`:

| Ruta | Contenido |
|---|---|
| `target/cucumber-report.html` | Reporte de Cucumber con capturas y video adjuntos |
| `target/screenshots/<escenario>/` | Una captura por paso (`paso_01_OK.png`, `paso_02_FAILED.png`...) |
| `target/videos/<escenario>.webm` | Video completo del escenario |
| `target/traces/<escenario>.zip` | Trace de Playwright (acciones, DOM, red y capturas) |
| `target/surefire-reports/` | Resultado de JUnit/Surefire |

Para depurar un fallo, abre el trace:

```powershell
npx playwright show-trace target/traces/<escenario>.zip
```

---

## 4. Estructura del proyecto

```
Demo/
├── pom.xml                          # Dependencias y plugins de Maven
└── src/test/
    ├── java/
    │   ├── context/
    │   │   └── TestContext.java     # Ciclo de vida de Playwright: navegador, página, video y trace
    │   ├── hooks/
    │   │   └── Hooks.java           # @Before abre navegador · @AfterStep captura · @After cierra y adjunta video
    │   ├── pages/                   # Page Objects (extienden BasePage, interfaz fluida)
    │   │   ├── BasePage.java        # Utilidades comunes: clic en botón, toast, esperas
    │   │   ├── LoginPage.java
    │   │   ├── DashboardPage.java
    │   │   ├── PimPage.java
    │   │   ├── EmployeeListPage.java
    │   │   ├── AddEmployeePage.java
    │   │   ├── PersonalDetailsPage.java
    │   │   └── ContactDetailsPage.java
    │   ├── steps/                   # Definiciones de los steps de Gherkin
    │   │   ├── LoginSteps.java
    │   │   ├── EmpleadoSteps.java
    │   │   └── GestionEmpleadoSteps.java
    │   └── runners/
    │       └── RunCucumberTest.java # Runner JUnit Platform Suite
    └── resources/features/          # Escenarios Gherkin (.feature)
```

> El repositorio también contiene `package.json`, `playwright.config.ts`, `e2e/` y `tests/`. Son restos de la plantilla de Playwright para Node/TypeScript y **no forman parte** del framework Java.

### Cómo se conectan las piezas

1. `RunCucumberTest` lee los `.feature` de `resources/features` y busca el *glue* en los paquetes `steps`, `hooks` y `context`.
2. **PicoContainer** crea un `TestContext` por escenario y lo inyecta en `Hooks` y en cada clase de steps.
3. Los **steps** no usan Playwright directamente: delegan en los **Page Objects**, que encapsulan los locators y las acciones.
4. Los **hooks** abren el navegador antes de cada escenario, capturan pantalla después de cada paso y, al terminar, guardan el video y el trace.

---

## 5. Cobertura actual

| Tag | Funcionalidad | Escenarios |
|---|---|---|
| `@HU_01` `@CA_02` | Login con credenciales inválidas | 1 |
| `@PIM` | Registro de empleado nuevo | 1 |
| `@PIM` `@Busqueda` | Búsqueda por nombre, por ID y sin resultados | 3 |
| `@PIM` `@Edicion` | Edición de apellido, validación de obligatorios y datos de contacto | 3 |
| `@PIM` `@Eliminacion` | Eliminación individual, cancelación y eliminación múltiple | 3 |
| `@PIM` `@Visual` | Regresión visual de 6 pantallas del módulo (Percy) | 6 |

**Datos de prueba:** los escenarios de edición y eliminación crean su propio empleado con apellido único para no alterar datos compartidos del demo. Los de búsqueda necesitan un empleado "John" y otro con ID `0123`; si no existen, el escenario los crea.

---

## 6. Dependencias

Definidas en `pom.xml` (Java 17):

| Dependencia | Versión | Uso |
|---|---|---|
| `com.microsoft.playwright:playwright` | 1.63.0 | Automatización del navegador |
| `io.cucumber:cucumber-java` | 7.20.1 (BOM) | Steps y hooks (BDD) |
| `io.cucumber:cucumber-junit-platform-engine` | 7.20.1 (BOM) | Motor de Cucumber sobre JUnit Platform |
| `io.cucumber:cucumber-picocontainer` | 7.20.1 (BOM) | Inyección de `TestContext` en steps y hooks |
| `org.junit.platform:junit-platform-suite` | 5.10.2 (BOM) | Runner `@Suite` |
| `org.junit.jupiter:junit-jupiter` | 5.10.2 (BOM) | Motor de pruebas JUnit 5 |
| `io.percy:percy-playwright-java` | 1.2.1 | Regresión visual (snapshots en Percy) |
| `maven-surefire-plugin` | 3.2.5 | Ejecución de las pruebas con `mvn test` |

---

## 7. Cómo agregar un escenario nuevo

1. **Feature:** escribe el escenario en `src/test/resources/features/` (con `# language: es` en la primera línea si usas español) y asígnale una etiqueta.
2. **Page Object:** si la pantalla no existe, crea una clase en `pages/` que extienda `BasePage`. Los locators van como campos y las acciones devuelven la siguiente página (interfaz fluida).
3. **Steps:** agrega las definiciones en `steps/`. Recibe `TestContext` por constructor y delega en los Page Objects.
4. **Verifica:** `mvn test "-Dcucumber.filter.tags=@MiEtiqueta"`.

En Gherkin en español, la palabra clave (`Dado`, `Cuando`, `Entonces`, `Y`) **no** forma parte del texto del step: `Dado que estoy en el login` se define como `@Given("que estoy en el login")`.

---

## 8. Solución de problemas

| Síntoma | Causa y solución |
|---|---|
| `Unknown lifecycle phase ".filter.tags=..."` | PowerShell partió el argumento. Entrecomilla todo: `"-Dcucumber.filter.tags=@Busqueda"` |
| Steps no encontrados (`Undefined step`) | La clase de steps no está en `src/test/java/steps` o su `package` no coincide con la carpeta |
| Timeout esperando un elemento | Revisa el *Call log* del error y abre el trace en `target/traces/` |
| El escenario de búsqueda por ID ve más de una fila | El filtro "Employee Id" puede hacer coincidencia parcial; ajusta el ID de prueba |
| Fallos intermitentes | El demo público es compartido y se reinicia a diario; considera un ambiente propio |

---

## 9. Limitaciones conocidas

- El pipeline de CI actual (`.github/workflows/playwright.yml`) ejecuta `npx playwright test` sobre la plantilla de Node. **No ejecuta estas pruebas Java**; falta un job con `mvn test`.
- Credenciales y URL están en el código; conviene externalizarlas a propiedades o variables de entorno.
- Los escenarios de edición y contacto dejan empleados creados en el demo.
- Dependencia de un sitio público externo: la disponibilidad y los datos pueden cambiar.

---

## 10. Regresión visual con Percy

Las pruebas `@Visual` toman un *snapshot* de 6 pantallas del módulo PIM y Percy lo compara contra una línea base: Employee List, Employee List sin resultados, Add Employee, Reports, Configuration → Optional Fields y Configuration → Data Import.

**Requisitos:** una cuenta en [Percy](https://percy.io) con un proyecto de tipo *Web* (de ahí sale el `PERCY_TOKEN`) y Node.js. El CLI `@percy/cli` ya figura en `package.json`; instálalo con `npm install`.

```powershell
$Env:PERCY_TOKEN="<tu token>"
npx percy exec -- mvn test "-Dcucumber.filter.tags=@Visual"
```

- **Primera ejecución:** crea la línea base.
- **Siguientes:** Percy compara y marca las diferencias para aprobarlas o rechazarlas en su panel.
- **Demostrar la detección:** `npx percy exec -- mvn test "-Dcucumber.filter.tags=@Visual" "-DsimularCambioVisual=true"` introduce un cambio de color que Percy debe señalar.
- **Ejecución normal:** sin `percy exec` los snapshots se omiten. Para no gastar tiempo, excluye estas pruebas: `mvn test "-Dcucumber.filter.tags=not @Visual"`.
- **No subas el token al repositorio.** Usa variable de entorno (o secreto en CI).

Los datos que cambian en cada ejecución (usuario del encabezado, tabla de empleados, ID autogenerado, mensajes emergentes) se ocultan con `percyCSS` para evitar falsos positivos; ver `VisualCheck` y `VisualSteps`.
