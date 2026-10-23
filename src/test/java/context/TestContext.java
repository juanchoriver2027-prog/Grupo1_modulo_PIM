package context;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.Video;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestContext {
    private static final Path VIDEOS_DIR = Paths.get("target/videos");
    private static final Path TRACES_DIR = Paths.get("target/traces");

    // Ajustables por consola: mvn test -DslowMo=300 -Dheadless=true
    private static final boolean HEADLESS = Boolean.parseBoolean(System.getProperty("headless", "false"));
    private static final double SLOW_MO = Double.parseDouble(System.getProperty("slowMo", "0"));
    // El video se corta ~1 s antes del cierre; esta pausa deja grabado el estado final
    private static final double VIDEO_TAIL_MS = 1000;

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private Video video;

    public void start() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(HEADLESS).setSlowMo(SLOW_MO));
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 720)
                .setRecordVideoDir(VIDEOS_DIR)
                .setRecordVideoSize(1280, 720));   // sin esto el video sale reducido (~800x450)

        // Trace: graba TODO (acciones, DOM, red, capturas) y se abre con el Trace Viewer
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true));

        page = context.newPage();
        video = page.video();
        page.setDefaultTimeout(50000);
    }

    /** Cierra todo y devuelve la ruta del video del escenario (o null si no se pudo guardar). */
    public Path stop(String escenario) {
        Path videoFinal = null;
        try {
            if (page != null && !page.isClosed()) page.waitForTimeout(VIDEO_TAIL_MS);

            if (context != null) {
                try {
                    Files.createDirectories(TRACES_DIR);
                    context.tracing().stop(new Tracing.StopOptions()
                            .setPath(TRACES_DIR.resolve(escenario + ".zip")));
                } catch (Exception e) {
                    System.err.println("[TestContext] No se pudo guardar el trace: " + e.getMessage());
                }
                context.close();   // aquí se finaliza el video
            }

            if (video != null) {
                Files.createDirectories(VIDEOS_DIR);
                videoFinal = VIDEOS_DIR.resolve(escenario + ".webm");
                video.saveAs(videoFinal);   // nombre legible en lugar de page@<hash>.webm
                video.delete();             // elimina el archivo con nombre aleatorio
            }
        } catch (Exception e) {
            System.err.println("[TestContext] No se pudo guardar el video: " + e.getMessage());
            videoFinal = null;
        } finally {
            // Siempre se liberan los recursos, aunque algo de lo anterior falle
            try { if (browser != null) browser.close(); } catch (Exception ignored) { }
            try { if (playwright != null) playwright.close(); } catch (Exception ignored) { }
        }
        return videoFinal;
    }

    /** Espera a que la página termine de pintar antes de capturar (evita capturas en blanco o con loader). */
    public void waitUntilSettled() {
        if (page == null || page.isClosed()) return;
        try {
            page.waitForLoadState(LoadState.LOAD,
                    new Page.WaitForLoadStateOptions().setTimeout(5000));
            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(2000));
        } catch (PlaywrightException ignored) {
            // Si el timeout vence se captura igual: nunca debe romper la prueba
        }
        try {
            // Spinner de OrangeHRM; si no existe, la espera termina de inmediato
            page.locator(".oxd-loading-spinner").first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(3000));
        } catch (PlaywrightException ignored) {
        }
    }

    public Page getPage() {
        return page;
    }
}
