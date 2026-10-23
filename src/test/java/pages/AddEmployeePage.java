package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class AddEmployeePage extends BasePage {
    private final Locator firstNameInput;
    private final Locator lastNameInput;
    private final Locator employeeIdInput;
    private final Locator saveButton;

    public AddEmployeePage(Page page) {
        super(page);
        firstNameInput  = page.locator("input[name='firstName']");
        lastNameInput   = page.locator("input[name='lastName']");
        employeeIdInput = page.locator(".oxd-input-group")
                .filter(new Locator.FilterOptions().setHasText("Employee Id"))
                .locator("input");
        saveButton      = page.locator("button[type='submit']");
    }

    public AddEmployeePage waitUntilLoaded() {
        page.waitForURL("**/pim/addEmployee");
        return this;
    }

    public PersonalDetailsPage registerEmployee(String firstName, String lastName) {
        firstNameInput.fill(firstName);
        lastNameInput.fill(lastName);
        saveButton.click();
        return new PersonalDetailsPage(page);
    }

    public PersonalDetailsPage registerEmployee(String firstName, String lastName, String employeeId) {
        firstNameInput.fill(firstName);
        lastNameInput.fill(lastName);
        waitUntilHasValue(employeeIdInput);   // el ID autogenerado llega por API y pisaría el valor
        employeeIdInput.fill(employeeId);
        saveButton.click();
        return new PersonalDetailsPage(page);
    }
}
