package steps;

import context.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.LoginPage;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginSteps {
    private static final String USUARIO_INVALIDO = "UsuarioInvalido";
    private static final String CLAVE_INVALIDA = "ClaveInvalida123";

    private final TestContext ctx;
    private LoginPage loginPage;

    public LoginSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    @Given("el administrador ha ingresado al sistema")
    public void elAdministradorHaIngresadoAlSistema() {
        new LoginPage(ctx.getPage())
                .navigate()
                .loginAs("Admin", "admin123")
                .waitUntilLoaded();
    }

    // HU-01 CA-02: en Gherkin "es", "Dado " es la keyword, por eso el texto empieza en "que"
    @Given("que estoy en la pantalla de login")
    public void estoyEnLaPantallaDeLogin() {
        loginPage = new LoginPage(ctx.getPage())
                .navigate()
                .waitUntilLoaded();
    }

    @When("ingreso credenciales inválidas")
    public void ingresoCredencialesInvalidas() {
        loginPage.attemptLogin(USUARIO_INVALIDO, CLAVE_INVALIDA);
    }

    @Then("el sistema muestra {string} y no permite el acceso")
    public void elSistemaMuestraMensajeYNoPermiteElAcceso(String mensaje) {
        assertThat(loginPage.errorMessage()).hasText(mensaje);
        assertThat(ctx.getPage()).hasURL(Pattern.compile(".*/auth/login.*"));
        assertThat(loginPage.loginButton()).isVisible();
    }
}
