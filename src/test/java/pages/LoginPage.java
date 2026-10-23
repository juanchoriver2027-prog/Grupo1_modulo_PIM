package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class LoginPage extends BasePage {
    private static final String URL = "https://opensource-demo.orangehrmlive.com/";

    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator errorMessage;

    public LoginPage(Page page) {
        super(page);
        usernameInput = page.locator("input[name='username']");
        passwordInput = page.locator("input[name='password']");
        loginButton   = page.locator("button[type='submit']");
        errorMessage  = page.locator(".oxd-alert-content-text");
    }

    public LoginPage navigate() {
        page.navigate(URL);
        return this;
    }

    public LoginPage waitUntilLoaded() {
        loginButton.waitFor();
        return this;
    }

    public DashboardPage loginAs(String user, String password) {
        attemptLogin(user, password);
        return new DashboardPage(page);
    }

    public LoginPage attemptLogin(String user, String password) {
        usernameInput.fill(user);
        passwordInput.fill(password);
        loginButton.click();
        return this;
    }

    public Locator errorMessage() {
        return errorMessage;
    }

    public Locator loginButton() {
        return loginButton;
    }
}
