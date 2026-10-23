package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public abstract class BasePage {
    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    public String getUrl() {
        return page.url();
    }

    /** Clic en un botón por su nombre accesible (coincidencia parcial, sin distinguir mayúsculas). */
    public void clickButton(String name) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name)).first().click();
    }

    /** Mensaje emergente (toast) que contiene el texto indicado. Filtra por texto porque pueden apilarse varios. */
    public Locator toastWithText(String text) {
        return page.locator(".oxd-toast-content", new Page.LocatorOptions().setHasText(text)).first();
    }

    /**
     * Espera el toast con ese texto y devuelve su contenido (null si no aparece).
     * Se debe llamar justo después de la acción: el toast desaparece a los pocos segundos
     * y no puede depender de que un step posterior lo encuentre todavía visible.
     */
    public String waitForToastText(String text) {
        try {
            Locator toast = toastWithText(text);
            toast.waitFor(new Locator.WaitForOptions().setTimeout(10000));
            return toast.innerText();
        } catch (PlaywrightException e) {
            return null;
        }
    }

    /** Espera, sin fallar, a que desaparezca el spinner de carga de OrangeHRM. */
    protected void waitForSpinnerToDisappear() {
        try {
            page.locator(".oxd-loading-spinner").first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(10000));
        } catch (PlaywrightException ignored) {
            // Mejor esfuerzo: las aserciones posteriores reintentan por sí solas
        }
    }

    /** Espera a que un input tenga valor: OrangeHRM precarga los formularios por API y pisaría lo que se escriba antes. */
    protected void waitUntilHasValue(Locator input) {
        input.waitFor();
        page.waitForFunction("el => el.value !== ''", input.elementHandle());
    }
}
