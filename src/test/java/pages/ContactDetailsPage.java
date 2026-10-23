package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ContactDetailsPage extends BasePage {
    private final Locator workEmailInput;

    public ContactDetailsPage(Page page) {
        super(page);
        workEmailInput = page.locator(".oxd-input-group")
                .filter(new Locator.FilterOptions().setHasText("Work Email"))
                .locator("input");
    }

    public ContactDetailsPage waitUntilLoaded() {
        page.waitForURL("**/pim/contactDetails/**");
        workEmailInput.waitFor();
        waitForSpinnerToDisappear();
        return this;
    }

    public ContactDetailsPage setWorkEmail(String email) {
        workEmailInput.fill(email);
        return this;
    }

    public Locator workEmail() {
        return workEmailInput;
    }
}
