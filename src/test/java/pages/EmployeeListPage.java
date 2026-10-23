package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

public class EmployeeListPage extends BasePage {
    public static final String NO_RECORDS = "No Records Found";

    // Columnas de la tabla de resultados (nth-child es base 1; la columna 1 es el checkbox)
    private static final int COL_ID = 2;
    private static final int COL_FIRST_MIDDLE_NAME = 3;
    private static final int COL_LAST_NAME = 4;

    private final Locator employeeNameInput;
    private final Locator employeeIdInput;
    private final Locator searchButton;
    private final Locator rows;
    private final Locator deleteSelectedButton;
    private final Locator confirmDeleteButton;
    private final Locator addEmployeeTab;

    public EmployeeListPage(Page page) {
        super(page);
        employeeNameInput = page.locator(".oxd-input-group")
                .filter(new Locator.FilterOptions().setHasText("Employee Name"))
                .getByPlaceholder("Type for hints...");
        employeeIdInput = page.locator(".oxd-input-group")
                .filter(new Locator.FilterOptions().setHasText("Employee Id"))
                .locator("input");
        searchButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Search").setExact(true));
        rows = page.locator(".oxd-table-body .oxd-table-card");
        deleteSelectedButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Delete Selected"));
        confirmDeleteButton = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Yes, Delete"));
        addEmployeeTab = page.getByText("Add Employee",
                new Page.GetByTextOptions().setExact(true));
    }

    public EmployeeListPage waitUntilLoaded() {
        page.waitForURL("**/pim/viewEmployeeList");
        searchButton.waitFor();
        return this;
    }

    public AddEmployeePage goToAddEmployee() {
        addEmployeeTab.click();
        return new AddEmployeePage(page);
    }

    // ---------------------------------------------------------------- filtros

    public EmployeeListPage typeEmployeeName(String name) {
        employeeNameInput.fill(name);
        return this;
    }

    public EmployeeListPage typeEmployeeId(String id) {
        employeeIdInput.fill(id);
        return this;
    }

    public EmployeeListPage search() {
        searchButton.click();
        waitForSpinnerToDisappear();
        return this;
    }

    public EmployeeListPage searchByName(String name) {
        return typeEmployeeName(name).search();
    }

    public EmployeeListPage searchById(String id) {
        return typeEmployeeId(id).search();
    }

    /** Clic genérico en un botón; en esta pantalla además espera a que termine de cargar la tabla. */
    @Override
    public void clickButton(String name) {
        super.clickButton(name);
        waitForSpinnerToDisappear();
    }

    // ---------------------------------------------------------- consultas

    /** Busca por nombre y espera a que haya coincidencias o el mensaje "No Records Found". */
    public boolean existsEmployeeNamed(String name) {
        searchByName(name);
        Locator match = rowsMatchingName(name).first();
        match.or(messageInResults(NO_RECORDS)).first().waitFor();
        return match.isVisible();
    }

    /** Busca por ID y espera a que haya coincidencias o el mensaje "No Records Found". */
    public boolean existsEmployeeWithId(String id) {
        searchById(id);
        Locator match = rowsWithId(id).first();
        match.or(messageInResults(NO_RECORDS)).first().waitFor();
        return match.isVisible();
    }

    public Locator rows() {
        return rows;
    }

    public Locator rowsWithText(String text) {
        return rows.filter(new Locator.FilterOptions().setHasText(text));
    }

    /** Filas cuyo nombre (First & Middle o Last) contiene el texto. */
    public Locator rowsMatchingName(String name) {
        return rows.filter(new Locator.FilterOptions().setHas(nameCellsMatching(name)));
    }

    /** Filas cuyo nombre (First & Middle o Last) NO contiene el texto. */
    public Locator rowsNotMatchingName(String name) {
        return rows.filter(new Locator.FilterOptions().setHasNot(nameCellsMatching(name)));
    }

    public Locator rowsWithId(String id) {
        Locator idCell = page.locator(cell(COL_ID)).filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("^\\s*" + escapeRegex(id) + "\\s*$")));
        return rows.filter(new Locator.FilterOptions().setHas(idCell));
    }

    public Locator messageInResults(String text) {
        return page.getByText(text).first();
    }

    public Locator confirmDeleteButton() {
        return confirmDeleteButton;
    }

    // ---------------------------------------------------------- acciones sobre filas

    public PersonalDetailsPage openEmployee(String lastName) {
        rowsWithText(lastName).first().locator(cell(COL_LAST_NAME)).click();
        return new PersonalDetailsPage(page);
    }

    public EmployeeListPage selectEmployee(String lastName) {
        rowsWithText(lastName).first().locator(".oxd-checkbox-input").click();
        return this;
    }

    public EmployeeListPage selectAllRows() {
        int total = rows.count();
        for (int i = 0; i < total; i++) {
            rows.nth(i).locator(".oxd-checkbox-input").click();
        }
        return this;
    }

    public EmployeeListPage clickDeleteIconOf(String lastName) {
        rowsWithText(lastName).first().locator("button:has(i.bi-trash)").click();
        return this;
    }

    public EmployeeListPage clickDeleteSelected() {
        deleteSelectedButton.click();
        return this;
    }

    public EmployeeListPage confirmDelete() {
        confirmDeleteButton.click();   // sin esperar aquí: el toast de éxito dura pocos segundos
        return this;
    }

    // ---------------------------------------------------------- utilitarios

    private Locator nameCellsMatching(String name) {
        return page.locator(cell(COL_FIRST_MIDDLE_NAME) + ", " + cell(COL_LAST_NAME))
                .filter(new Locator.FilterOptions()
                        .setHasText(name));   // texto: Playwright compara sin distinguir mayúsculas
    }

    /**
     * Escapa metacaracteres con sintaxis válida en JavaScript. No usar Pattern.quote():
     * genera \\Q...\\E, que Playwright envía al navegador y JavaScript no entiende.
     */
    private static String escapeRegex(String text) {
        return text.replaceAll("[.*+?^${}()|\\[\\]\\\\]", "\\\\$0");
    }

    private static String cell(int column) {
        return ".oxd-table-cell:nth-child(" + column + ")";
    }
}
