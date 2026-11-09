package utils.accessibility;

import com.automation.pa11y.Pa11yRunner;
import com.automation.pa11y.ScanRequest;
import com.automation.pa11y.ScanResult;
import com.automation.pa11y.Standard;
import com.automation.pa11y.report.CombinedReport;
import com.automation.pa11y.report.HtmlReport;
import com.automation.pa11y.report.PageReport;
import com.automation.pa11y.report.ReportStore;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;

import core.Hooks;
import core.WebDriverManager;
import utils.config.ConfigReader;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static core.TestLogger.LOGGER;

/**
 * Accessibility scanning for the page the test is already on, using the
 * <a href="https://pa11y.org/">Pa11y</a> engine through the local
 * {@code pa11y-runner} library.
 *
 * <p>Nothing is reloaded and no second browser is started. The scanner attaches
 * over CDP to the Chrome this framework's {@link WebDriverManager} is driving and
 * measures the DOM in whatever state the test left it, so a screen that has been
 * clicked into shape — and is signed in — is scanned as it stands.
 *
 * <p>Every setting comes from {@code config.properties}:
 *
 * <pre>
 *   pa11y.chrome.url        DevTools endpoint; blank falls back to chrome.debug.address,
 *                           then to the port ChromeDriver reports for its own browser
 *   pa11y.scanner.dir       folder holding the Node scanner's node_modules
 *   pa11y.standard          WCAG2A | WCAG2AA | WCAG2AAA
 *   pa11y.include.warnings  whether warnings count as findings
 *   pa11y.root.element      narrow every scan to one container, e.g. #main
 *   pa11y.ignore            comma-separated rule codes to drop everywhere
 *   pa11y.wait.after.load   milliseconds to settle before measuring
 * </pre>
 *
 * <p>Each scan is saved as a page in the run's report folder; {@link #writeCombinedReport()}
 * turns the saved pages into one self-contained HTML file that separates findings shared
 * across pages (one fix in a header clears many) from each page's own.
 *
 * <p>A scan that <em>ran</em> always returns a {@link ScanResult}, however bad the page is.
 * A scan that could not run at all throws
 * {@link com.automation.pa11y.ScanFailedException} — an unreachable browser must not look
 * like a clean page.
 */
public final class AccessibilityScanner {

	/** Where scans are saved when the Extent run folder is not available. */
	private static final String FALLBACK_REPORTS_DIR = "target/pa11y-reports";

	private static volatile Pa11yRunner runner;
	private static volatile ScanRequest policy;
	private static volatile ReportStore store;

	private AccessibilityScanner() {
	}

	/**
	 * Scans the page currently open in the test's browser and saves it under
	 * {@code pageName} for the combined report.
	 *
	 * <p>Scanning the same name twice replaces the earlier result rather than
	 * doubling it, so a page revisited later in a suite is recorded once.
	 *
	 * @param pageName the name this page appears under in the report
	 * @return what the scan found
	 * @throws com.automation.pa11y.ScanFailedException if the scan could not be carried out
	 */
	public static ScanResult scanCurrentPage(String pageName) {
		ScanResult result = runner().scan(policy());

		LOGGER.info("Accessibility scan of '{}' ({}) — {} error(s), {} warning(s), took {}ms",
				pageName, result.pageUrl(), result.errors().size(), result.warnings().size(),
				result.duration().toMillis());

		try {
			Path saved = store().write(PageReport.of(pageName, policy(), result));
			LOGGER.debug("Accessibility page saved: {}", saved);
		} catch (RuntimeException e) {
			// A page we could not save is a lost line in the report, not a failed test.
			LOGGER.warn("Could not save accessibility page '{}': {}", pageName, e.getMessage());
		}

		return result;
	}

	/**
	 * The findings that should fail a check, which is errors alone unless
	 * {@code pa11y.include.warnings} is on.
	 *
	 * @param result a completed scan
	 * @return the issues that count against the page
	 */
	public static List<com.automation.pa11y.Issue> countableIssues(ScanResult result) {
		if (!includeWarnings()) {
			return result.errors();
		}
		return java.util.stream.Stream.concat(result.errors().stream(), result.warnings().stream())
				.toList();
	}

	/**
	 * Combines every page scanned in this run into one self-contained HTML file.
	 *
	 * <p>Called once at the end of the run. Does nothing when no page was scanned —
	 * a suite with no accessibility steps should not leave an empty report behind.
	 *
	 * @return the file written, or empty if there was nothing to combine
	 */
	public static Optional<Path> writeCombinedReport() {
		if (store == null) {
			return Optional.empty();
		}

		List<PageReport> pages = store.readAll();
		if (pages.isEmpty()) {
			LOGGER.info("No accessibility scans were recorded — no combined report written.");
			return Optional.empty();
		}

		CombinedReport combined = CombinedReport.from(pages);
		Path file = store.directory().resolve("accessibility-report.html");
		String title = Hooks.reportNameFromConfig + " — accessibility";

		HtmlReport.write(combined, title, file);
		LOGGER.info("Accessibility report written: {} ({} page(s), {} finding(s))",
				file.toAbsolutePath(), pages.size(), combined.totalIssues());
		return Optional.of(file);
	}

	/** Forgets the cached runner, policy and store. For tests that change config mid-JVM. */
	public static synchronized void reset() {
		runner = null;
		policy = null;
		store = null;
	}

	// ── wiring ───────────────────────────────────────────────────────────────

	private static Pa11yRunner runner() {
		Pa11yRunner existing = runner;
		if (existing != null) {
			return existing;
		}
		synchronized (AccessibilityScanner.class) {
			if (runner == null) {
				String chromeUrl = resolveChromeDebuggerUrl();
				Pa11yRunner.Builder builder = Pa11yRunner.builder().chromeDebuggerUrl(chromeUrl);

				String scannerDir = ConfigReader.getProperty("pa11y.scanner.dir");
				if (!scannerDir.isBlank()) {
					builder.scannerDirectory(Path.of(scannerDir));
				}

				LOGGER.info("Pa11y will scan through {}{}", chromeUrl,
						scannerDir.isBlank() ? "" : " using the scanner at " + scannerDir);
				runner = builder.build();
			}
			return runner;
		}
	}

	private static ScanRequest policy() {
		ScanRequest existing = policy;
		if (existing != null) {
			return existing;
		}
		synchronized (AccessibilityScanner.class) {
			if (policy == null) {
				ScanRequest.Builder builder = ScanRequest.currentPage()
						.standard(standard())
						.includeWarnings(includeWarnings());

				String rootElement = ConfigReader.getProperty("pa11y.root.element");
				if (!rootElement.isBlank()) {
					builder.rootElement(rootElement);
				}

				String[] ignored = ConfigReader.getProperty("pa11y.ignore").split(",");
				List<String> codes = java.util.Arrays.stream(ignored)
						.map(String::trim)
						.filter(code -> !code.isEmpty())
						.toList();
				if (!codes.isEmpty()) {
					builder.ignore(codes.toArray(String[]::new));
				}

				int settle = ConfigReader.getIntProperty("pa11y.wait.after.load", 0);
				if (settle > 0) {
					builder.waitAfterLoad(Duration.ofMillis(settle));
				}

				policy = builder.build();
			}
			return policy;
		}
	}

	private static ReportStore store() {
		ReportStore existing = store;
		if (existing != null) {
			return existing;
		}
		synchronized (AccessibilityScanner.class) {
			if (store == null) {
				String runFolder = Hooks.getReportDirectory();
				Path directory = runFolder == null || runFolder.isBlank()
						? Path.of(FALLBACK_REPORTS_DIR)
						: Path.of(runFolder, "accessibility");
				store = new ReportStore(directory);
			}
			return store;
		}
	}

	private static Standard standard() {
		String configured = ConfigReader.getProperty("pa11y.standard");
		if (configured.isBlank()) {
			return Standard.WCAG2AA;
		}
		try {
			return Standard.valueOf(configured.toUpperCase());
		} catch (IllegalArgumentException e) {
			LOGGER.warn("pa11y.standard '{}' is not WCAG2A, WCAG2AA or WCAG2AAA — using WCAG2AA",
					configured);
			return Standard.WCAG2AA;
		}
	}

	private static boolean includeWarnings() {
		return ConfigReader.getBooleanProperty("pa11y.include.warnings", false);
	}

	/**
	 * Works out which DevTools endpoint to attach to, in the order that puts the
	 * explicit choice first and the guess last:
	 *
	 * <ol>
	 *   <li>{@code pa11y.chrome.url} — set this for a Grid, where it is the session's
	 *       {@code /se/cdp} WebSocket rather than a port</li>
	 *   <li>{@code chrome.debug.address} — the Chrome this framework was told to attach to</li>
	 *   <li>the {@code debuggerAddress} ChromeDriver reports for the browser it launched,
	 *       which is a fresh port on each run</li>
	 * </ol>
	 *
	 * @return a URL the scanner can attach to
	 * @throws IllegalStateException if none of the three yielded an address
	 */
	private static String resolveChromeDebuggerUrl() {
		String configured = ConfigReader.getProperty("pa11y.chrome.url");
		if (!configured.isBlank()) {
			return withScheme(configured);
		}

		String attached = ConfigReader.getProperty("chrome.debug.address");
		if (!attached.isBlank()) {
			return withScheme(attached);
		}

		return withScheme(debuggerAddressFromDriver().orElseThrow(() -> new IllegalStateException(
				"""
				No Chrome DevTools endpoint for the accessibility scan.

				Set one in config.properties:
				  chrome.debug.address=localhost:9222   to attach to a Chrome you started yourself, or
				  pa11y.chrome.url=...                  for a Grid session's /se/cdp endpoint

				Chrome needs --remote-debugging-port=9222 --remote-allow-origins=* to accept the scan.""")));
	}

	/**
	 * Asks the running ChromeDriver which DevTools address its browser is on.
	 * Chromedriver reports the real port here even when it chose the port itself.
	 */
	private static Optional<String> debuggerAddressFromDriver() {
		try {
			WebDriver driver = WebDriverManager.getDriver();
			if (!(driver instanceof HasCapabilities withCapabilities)) {
				return Optional.empty();
			}
			Capabilities capabilities = withCapabilities.getCapabilities();
			if (!(capabilities.getCapability("goog:chromeOptions") instanceof Map<?, ?> chromeOptions)) {
				return Optional.empty();
			}
			Object address = chromeOptions.get("debuggerAddress");
			return address == null || address.toString().isBlank()
					? Optional.empty()
					: Optional.of(address.toString());
		} catch (RuntimeException e) {
			LOGGER.debug("Could not read the debugger address from the driver: {}", e.getMessage());
			return Optional.empty();
		}
	}

	/** Leaves {@code ws://}, {@code wss://} and {@code http(s)://} alone; assumes http for a bare host:port. */
	private static String withScheme(String address) {
		String trimmed = address.trim();
		return trimmed.matches("^(https?|wss?)://.*") ? trimmed : "http://" + trimmed;
	}
}
