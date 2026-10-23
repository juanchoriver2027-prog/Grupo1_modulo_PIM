package steps;

import context.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.AddEmployeePage;
import pages.BasePage;
import pages.ContactDetailsPage;
import pages.DashboardPage;
import pages.EmployeeListPage;
import pages.LoginPage;
import pages.PersonalDetailsPage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Steps de búsqueda, edición y eliminación de empleados (módulo PIM).
 *
 * Los escenarios de edición/eliminación crean su PROPIO empleado (apellido único) para no
 * modificar ni borrar datos compartidos del demo. Esa precondición de datos se prepara en el
 * último step "Dado" de cada escenario.
 */
public class GestionEmpleadoSteps {
    private static final String USUARIO = "Admin";
    private static final String CLAVE = "admin123";

    // Datos que deben existir para los escenarios de búsqueda (coinciden con el Gherkin)
    private static final String NOMBRE_BASE = "John";
    private static final String ID_BASE = "0123";

    private final TestContext ctx;

    private BasePage paginaActual;                 // página sobre la que actúan los clics genéricos
    private EmployeeListPage listaEmpleados;
    private PersonalDetailsPage detallesPersonales;
    private ContactDetailsPage detallesContacto;
    private String apellidoEmpleado;
    private String apellidoEditado;
    private String correoNuevo;
    private String toastCapturado;                 // mensaje emergente capturado justo tras la acción

    public GestionEmpleadoSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    // ================================================================== DADO

    @Given("que el usuario inicia sesión en OrangeHRM")
    public void inicioSesion() {
        iniciarSesion();
    }

    @Given("navega a la sección {string} y luego a {string}")
    public void navegaAEmployeeList(String seccion, String subseccion) {
        validar(seccion, "PIM");
        validar(subseccion, "Employee List");
        listaEmpleados = new DashboardPage(ctx.getPage())
                .goToPim().waitUntilLoaded()
                .goToEmployeeList().waitUntilLoaded();
        paginaActual = listaEmpleados;
        asegurarEmpleadoConNombre(NOMBRE_BASE);   // precondición de datos
    }

    @Given("que el usuario se encuentra en la lista de empleados de OrangeHRM")
    public void enLaListaDeEmpleados() {
        irAListaDeEmpleados();
        asegurarEmpleadoConId(ID_BASE);           // precondición de datos
    }

    @Given("que el usuario está en la pantalla {string} de OrangeHRM")
    public void enLaPantalla(String pantalla) {
        validar(pantalla, "Employee List");
        irAListaDeEmpleados();
    }

    @Given("que el usuario busca y selecciona un empleado en la lista de OrangeHRM")
    public void buscaYSeleccionaEmpleado() {
        irAListaDeEmpleados();
        apellidoEmpleado = "Edicion" + sufijo();
        volverALaLista(crearEmpleado("Juan", apellidoEmpleado, null));

        detallesPersonales = listaEmpleados
                .searchByName(apellidoEmpleado)
                .openEmployee(apellidoEmpleado)
                .waitUntilLoaded()
                .waitUntilFormLoaded();
        paginaActual = detallesPersonales;
    }

    @Given("que el usuario está editando los datos personales de un empleado")
    public void editandoDatosPersonales() {
        irAListaDeEmpleados();
        detallesPersonales = crearEmpleado("Juan", "Validacion" + sufijo(), null).waitUntilFormLoaded();
        paginaActual = detallesPersonales;
    }

    @Given("que el usuario accede al perfil del empleado y selecciona la pestaña {string}")
    public void accedeAlPerfilYSeleccionaPestana(String pestana) {
        validar(pestana, "Contact Details");
        irAListaDeEmpleados();
        detallesPersonales = crearEmpleado("Juan", "Contacto" + sufijo(), null);
        detallesContacto = detallesPersonales.goToContactDetails().waitUntilLoaded();
        paginaActual = detallesContacto;
    }

    @Given("que el usuario busca un empleado en la lista de OrangeHRM")
    public void buscaUnEmpleado() {
        prepararListaConEmpleados("Eliminar", "Juan");
    }

    @Given("que el usuario hace clic en el icono de eliminar de un empleado en la tabla")
    public void clicEnIconoEliminar() {
        prepararListaConEmpleados("Cancelar", "Juan");
        listaEmpleados.clickDeleteIconOf(apellidoEmpleado);
    }

    @Given("que el usuario selecciona los checkboxes de múltiples empleados en la lista")
    public void seleccionaVariosEmpleados() {
        prepararListaConEmpleados("Masivo", "Ana", "Luis");
        listaEmpleados.selectAllRows();
    }

    // ================================================================== CUANDO

    @When("ingresa el nombre {string} en el campo de búsqueda de empleado")
    public void ingresaNombre(String nombre) {
        listaEmpleados.typeEmployeeName(nombre);
    }

    @When("ingresa el ID {string} en el campo {string}")
    public void ingresaId(String id, String campo) {
        validar(campo, "Employee ID");
        listaEmpleados.typeEmployeeId(id);
    }

    @When("ingresa el nombre {string} y hace clic en {string}")
    public void ingresaNombreYHaceClic(String nombre, String boton) {
        listaEmpleados.typeEmployeeName(nombre);
        listaEmpleados.clickButton(boton);
    }

    /** Clic genérico: actúa sobre la página en la que está el usuario (Search, Save, Delete Selected...). */
    @When("hace clic en el botón {string}")
    public void haceClicEnElBoton(String boton) {
        paginaActual.clickButton(boton);
        if (paginaActual == detallesContacto && "Save".equalsIgnoreCase(boton)) {
            toastCapturado = detallesContacto.waitForToastText("Successfully Updated");
        }
    }

    @When("edita el campo {string} con el nuevo valor {string}")
    public void editaCampo(String campo, String valor) {
        detallesPersonales.fillField(campo, valor);
        if ("Last Name".equals(campo)) {
            apellidoEditado = valor;
        }
    }

    @When("hace clic en el botón {string} de la sección personal details")
    public void guardaDetallesPersonales(String boton) {
        validar(boton, "Save");
        detallesPersonales.save();
        toastCapturado = detallesPersonales.waitForToastText("Successfully Updated");
    }

    @When("borra el contenido del campo obligatorio {string}")
    public void borraCampoObligatorio(String campo) {
        detallesPersonales.clearField(campo);
    }

    @When("ingresa el nuevo correo {string} en el campo correspondiente")
    public void ingresaNuevoCorreo(String correo) {
        correoNuevo = correo;
        detallesContacto.setWorkEmail(correo);
    }

    @When("selecciona la casilla de verificación \\(checkbox\\) del empleado a eliminar")
    public void seleccionaCheckbox() {
        listaEmpleados.selectEmployee(apellidoEmpleado);
    }

    @When("hace clic en el botón {string} seleccionado")
    public void haceClicEnEliminarSeleccionado(String boton) {
        // En pantalla el botón se llama "Delete Selected"; el Gherkin lo nombra "Delete"
        listaEmpleados.clickDeleteSelected();
    }

    @When("confirma la acción en el modal emergente")
    public void confirmaEnElModal() {
        listaEmpleados.confirmDelete();
        toastCapturado = listaEmpleados.waitForToastText("Successfully Deleted");
    }

    @When("confirma la acción en el cuadro de diálogo")
    public void confirmaEnElCuadroDeDialogo() {
        listaEmpleados.confirmDelete();
        toastCapturado = listaEmpleados.waitForToastText("Successfully Deleted");
    }

    @When("aparece el modal de confirmación y hace clic en el botón {string}")
    public void modalYClicEnElBoton(String boton) {
        assertThat(listaEmpleados.confirmDeleteButton()).isVisible();
        listaEmpleados.clickButton(boton);
    }

    // ================================================================== ENTONCES

    @Then("el sistema debe mostrar los registros que coincidan con el nombre {string} en la tabla de resultados")
    public void muestraRegistrosPorNombre(String nombre) {
        assertThat(listaEmpleados.rowsNotMatchingName(nombre)).hasCount(0);   // ninguna fila ajena al filtro
        assertThat(listaEmpleados.rows()).not().hasCount(0);                  // y hay al menos un resultado
    }

    @Then("el sistema debe mostrar únicamente el registro correspondiente al ID {string}")
    public void muestraUnicamenteElRegistroDelId(String id) {
        assertThat(listaEmpleados.rows()).hasCount(1);
        assertThat(listaEmpleados.rowsWithId(id)).hasCount(1);
    }

    @Then("el sistema debe mostrar el mensaje {string} en la tabla de resultados")
    public void muestraMensajeEnResultados(String mensaje) {
        assertThat(listaEmpleados.messageInResults(mensaje)).isVisible();
    }

    @Then("el sistema debe mostrar un mensaje de éxito indicando que los datos fueron actualizados")
    public void muestraMensajeDeActualizacion() {
        verificarToast("Successfully Updated");
        if (apellidoEditado != null) {
            assertThat(detallesPersonales.lastName()).hasValue(apellidoEditado);
        }
    }

    @Then("el sistema debe mostrar un mensaje de error indicando que el campo es requerido")
    public void muestraErrorDeCampoRequerido() {
        assertThat(detallesPersonales.requiredError()).isVisible();
        assertThat(detallesPersonales.toastWithText("Successfully Updated")).isHidden();   // no se guardó nada
    }

    @Then("los datos de contacto deben actualizarse correctamente en el sistema")
    public void contactoActualizado() {
        verificarToast("Successfully Updated");
        assertThat(detallesContacto.workEmail()).hasValue(correoNuevo);
    }

    @Then("el registro del empleado debe desaparecer de la lista y mostrarse un mensaje de éxito")
    public void empleadoEliminado() {
        verificarToast("Successfully Deleted");
        assertThat(listaEmpleados.rowsWithText(apellidoEmpleado)).hasCount(0);
    }

    @Then("el empleado no debe ser eliminado y debe permanecer visible en la lista")
    public void empleadoPermanece() {
        assertThat(listaEmpleados.confirmDeleteButton()).isHidden();
        assertThat(listaEmpleados.rowsWithText(apellidoEmpleado)).hasCount(1);
    }

    @Then("todos los empleados seleccionados deben ser eliminados del sistema")
    public void empleadosEliminados() {
        verificarToast("Successfully Deleted");
        assertThat(listaEmpleados.rowsWithText(apellidoEmpleado)).hasCount(0);
    }

    // ================================================================== AUXILIARES

    private DashboardPage iniciarSesion() {
        return new LoginPage(ctx.getPage())
                .navigate()
                .loginAs(USUARIO, CLAVE)
                .waitUntilLoaded();
    }

    private void irAListaDeEmpleados() {
        listaEmpleados = iniciarSesion()
                .goToPim().waitUntilLoaded()
                .goToEmployeeList().waitUntilLoaded();
        paginaActual = listaEmpleados;
    }

    /** Crea un empleado desde Add Employee y deja al usuario en sus Personal Details. */
    private PersonalDetailsPage crearEmpleado(String nombre, String apellido, String id) {
        AddEmployeePage alta = listaEmpleados.goToAddEmployee().waitUntilLoaded();
        PersonalDetailsPage detalles = (id == null)
                ? alta.registerEmployee(nombre, apellido)
                : alta.registerEmployee(nombre, apellido, id);
        return detalles.waitUntilLoaded();
    }

    private void volverALaLista(PersonalDetailsPage detalles) {
        listaEmpleados = detalles.goToEmployeeList().waitUntilLoaded();
        paginaActual = listaEmpleados;
    }

    /** Crea uno o varios empleados con el mismo apellido único y filtra la lista por ese apellido. */
    private void prepararListaConEmpleados(String prefijoApellido, String... nombres) {
        irAListaDeEmpleados();
        apellidoEmpleado = prefijoApellido + sufijo();
        for (String nombre : nombres) {
            volverALaLista(crearEmpleado(nombre, apellidoEmpleado, null));
        }
        listaEmpleados.searchByName(apellidoEmpleado);
        // Sincroniza con el filtro: evita actuar sobre filas de la lista anterior mientras recarga
        assertThat(listaEmpleados.rows()).hasCount(nombres.length);
    }

    private void asegurarEmpleadoConNombre(String nombre) {
        if (!listaEmpleados.existsEmployeeNamed(nombre)) {
            volverALaLista(crearEmpleado(nombre, "Prueba" + sufijo(), null));
        }
    }

    private void asegurarEmpleadoConId(String id) {
        if (!listaEmpleados.existsEmployeeWithId(id)) {
            volverALaLista(crearEmpleado("Juan", "Busqueda" + sufijo(), id));
        }
    }

    /** El toast se capturó en el step de la acción (dura pocos segundos); aquí solo se verifica. */
    private void verificarToast(String esperado) {
        if (toastCapturado == null || !toastCapturado.contains(esperado)) {
            throw new AssertionError("No se mostró el mensaje '" + esperado
                    + "'. Mensaje capturado: " + toastCapturado);
        }
    }

    private static String sufijo() {
        return String.valueOf(System.currentTimeMillis() % 100000);
    }

    private static void validar(String recibido, String esperado) {
        if (!esperado.equalsIgnoreCase(recibido)) {
            throw new IllegalArgumentException(
                    "Valor no soportado por el step: '" + recibido + "' (se esperaba '" + esperado + "')");
        }
    }
}
