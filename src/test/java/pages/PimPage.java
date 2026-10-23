package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class PimPage extends BasePage {
    private final Locator addEmployeeTab;
    private final Locator employeeListTab;
    private final Locator reportsTab;
    private final Locator configurationTab;

    public PimPage(Page page) {
        super(page);
        addEmployeeTab = page.getByText("Add Employee",
                new Page.GetByTextOptions().setExact(true));
        employeeListTab = page.getByText("Employee List",
                new Page.GetByTextOptions().setExact(true)).first();
        reportsTab = page.getByText("Reports",
                new Page.GetByTextOptions().setExact(true)).first();
        configurationTab = page.getByText("Configuration",
                new Page.GetByTextOptions().setExact(true)).first();
    }

    public PimPage waitUntilLoaded() {
        page.waitForURL("**/pim/viewEmployeeList");
        return this;
    }

    public AddEmployeePage goToAddEmployee() {
        addEmployeeTab.click();
        return new AddEmployeePage(page);
    }

    public EmployeeListPage goToEmployeeList() {
        employeeListTab.click();
        return new EmployeeListPage(page);
    }

    public ReportsPage goToReports() {
        reportsTab.click();
        return new ReportsPage(page);
    }

    /** Abre el menú Configuration y elige una opción (Optional Fields, Data Import, ...). */
    public ConfigurationPage goToConfiguration(String option) {
        configurationTab.click();
        page.getByText(option, new Page.GetByTextOptions().setExact(true)).first().click();
        return new ConfigurationPage(page);
    }
}
