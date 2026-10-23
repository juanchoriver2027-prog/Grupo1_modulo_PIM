package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class PersonalDetailsPage extends BasePage {
    private final Locator firstNameInput;
    private final Locator lastNameInput;
    private final Locator saveButton;
    private final Locator requiredError;
    private final Locator contactDetailsTab;
    private final Locator employeeListTab;

    public PersonalDetailsPage(Page page) {
        super(page);
        firstNameInput = page.locator("input[name='firstName']");
        lastNameInput  = page.locator("input[name='lastName']");
        saveButton     = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save")).first();   // el primero es el de Personal Details
        requiredError  = page.locator(".oxd-input-field-error-message",
                new Page.LocatorOptions().setHasText("Required")).first();
        contactDetailsTab = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Contact Details").setExact(true));
        employeeListTab = page.getByText("Employee List",
                new Page.GetByTextOptions().setExact(true)).first();
    }

    public PersonalDetailsPage waitUntilLoaded() {
        page.waitForURL("**/pim/viewPersonalDetails/**");
        return this;
    }

    /** Espera a que el formulario termine de precargarse (si no, lo escrito se pisa con los datos de la API). */
    public PersonalDetailsPage waitUntilFormLoaded() {
        waitUntilHasValue(firstNameInput);
        return this;
    }

    public PersonalDetailsPage fillField(String fieldName, String value) {
        fieldByName(fieldName).fill(value);
        return this;
    }

    public PersonalDetailsPage clearField(String fieldName) {
        fieldByName(fieldName).clear();
        return this;
    }

    public PersonalDetailsPage save() {
        saveButton.click();
        return this;
    }

    public ContactDetailsPage goToContactDetails() {
        contactDetailsTab.click();
        return new ContactDetailsPage(page);
    }

    public EmployeeListPage goToEmployeeList() {
        employeeListTab.click();
        return new EmployeeListPage(page);
    }

    public Locator firstName() {
        return firstNameInput;
    }

    public Locator lastName() {
        return lastNameInput;
    }

    public Locator requiredError() {
        return requiredError;
    }

    private Locator fieldByName(String fieldName) {
        if ("First Name".equals(fieldName)) {
            return firstNameInput;
        }
        if ("Last Name".equals(fieldName)) {
            return lastNameInput;
        }
        throw new IllegalArgumentException("Campo no soportado en Personal Details: " + fieldName);
    }
}
