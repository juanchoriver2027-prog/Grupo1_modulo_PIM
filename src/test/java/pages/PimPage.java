package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class PimPage extends BasePage {
    private final Locator addEmployeeTab;
    private final Locator employeeListTab;

    public PimPage(Page page) {
        super(page);
        addEmployeeTab = page.getByText("Add Employee",
                new Page.GetByTextOptions().setExact(true));
        employeeListTab = page.getByText("Employee List",
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
}
