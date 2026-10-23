package steps;

import context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.DashboardPage;
import pages.PersonalDetailsPage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class EmpleadoSteps {
    private final TestContext ctx;
    private PersonalDetailsPage personalDetails;
    private String firstName;
    private String lastName;

    public EmpleadoSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    @When("el administrador realiza el registro de un empleado nuevo")
    public void registraEmpleadoNuevo() {
        firstName = "Juan Carlos";
        lastName = "Prueba" + System.currentTimeMillis() % 100000;

        personalDetails = new DashboardPage(ctx.getPage())
                .goToPim()
                .waitUntilLoaded()
                .goToAddEmployee()
                .waitUntilLoaded()
                .registerEmployee(firstName, lastName);
    }

    @Then("el empleado es registrado en el sistema")
    public void empleadoRegistrado() {
        personalDetails.waitUntilLoaded();
        assertThat(personalDetails.firstName()).hasValue(firstName);
        assertThat(personalDetails.lastName()).hasValue(lastName);
    }
}