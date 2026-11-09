package common.stepdefinitions;

import com.automation.pa11y.Issue;
import com.automation.pa11y.ScanFailedException;
import com.automation.pa11y.ScanResult;

import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import com.aventstack.extentreports.ExtentTest;

import core.Hooks;
import utils.accessibility.AccessibilityScanner;

import java.util.List;
import java.util.stream.Collectors;

import static core.TestLogger.LOGGER;

/**
 * Accessibility steps, driven by Pa11y against the page the browser is already on.
 *
 * <p>These steps never navigate. Put one after the steps that have brought a screen
 * into the state worth checking and it is measured in that state, signed in, with
 * nothing reloaded:
 *
 * <pre>
 *   Given I open the URL "http://localhost:3000/form-demo.html"
 *   When I complete all fields on the page using "FormData" for "1"
 *   Then the "Form demo — completed" page passes the accessibility check
 * </pre>
 *
 * <p>Two kinds of step, for the two things a suite needs:
 *
 * <ul>
 *   <li><b>passes the accessibility check</b> — fails the scenario on any finding.
 *       For screens you intend to keep clean.</li>
 *   <li><b>scan the page for accessibility</b> — records the page and carries on.
 *       For surveying screens whose current state you do not want to block on.</li>
 * </ul>
 *
 * <p>Either way the page goes into the run's combined HTML report, written when the run
 * ends to {@code accessibility/accessibility-report.html} inside the same timestamped
 * folder as the Extent report.
 *
 * <p>A scan that cannot be carried out — no Chrome on the debug port, the Node scanner
 * not installed — fails the step loudly rather than reporting a clean page. See
 * {@link AccessibilityScanner} for the {@code pa11y.*} settings in {@code config.properties}.
 */
public class AccessibilitySteps {

	/** The most recent scan in this scenario, for the steps that interrogate it. */
	private ScanResult lastResult;
	private String lastPageName;
	private String scenarioName = "page";

	@Before
	public void rememberScenarioName(Scenario scenario) {
		scenarioName = scenario.getName();
	}

	/**
	 * Scans the current page and records it, whatever it finds. The scenario carries
	 * on — use this to survey a screen without blocking the build on its current state.
	 *
	 * <p>Example:
	 * <pre>
	 *   When I scan the page for accessibility as "Hire an Employee"
	 * </pre>
	 *
	 * @param pageName the name this page appears under in the report
	 */
	@When("I scan the page for accessibility as {string}")
	public void scanPageForAccessibility(String pageName) {
		scan(pageName);
		report(pageName, lastResult, false);
	}

	/**
	 * Scans the current page and fails the scenario if anything was found.
	 * The page is named after the scenario.
	 *
	 * <p>Example:
	 * <pre>
	 *   Then the page passes the accessibility check
	 * </pre>
	 */
	@Then("the page passes the accessibility check")
	public void pagePassesAccessibilityCheck() {
		namedPagePassesAccessibilityCheck(scenarioName);
	}

	/**
	 * Scans the current page under a name of your choosing and fails the scenario
	 * if anything was found. Prefer this when a scenario checks more than one screen,
	 * so each appears separately in the report.
	 *
	 * <p>Example:
	 * <pre>
	 *   Then the "Checkout" page passes the accessibility check
	 * </pre>
	 *
	 * @param pageName the name this page appears under in the report
	 */
	@Then("the {string} page passes the accessibility check")
	public void namedPagePassesAccessibilityCheck(String pageName) {
		scan(pageName);
		List<Issue> findings = AccessibilityScanner.countableIssues(lastResult);
		report(pageName, lastResult, !findings.isEmpty());

		if (!findings.isEmpty()) {
			throw new AssertionError(describe(pageName, findings));
		}
	}

	/**
	 * Scans the current page and allows a known number of findings through, failing
	 * only if there are more. For a screen with a backlog you are working down:
	 * the step holds the line without pretending the page is clean.
	 *
	 * <p>Example:
	 * <pre>
	 *   Then the "Legacy search" page has no more than 4 accessibility errors
	 * </pre>
	 *
	 * @param pageName the name this page appears under in the report
	 * @param allowed  the number of findings currently tolerated on this page
	 */
	@Then("the {string} page has no more than {int} accessibility errors")
	public void namedPageHasNoMoreThanNErrors(String pageName, int allowed) {
		scan(pageName);
		List<Issue> findings = AccessibilityScanner.countableIssues(lastResult);
		boolean over = findings.size() > allowed;
		report(pageName, lastResult, over);

		if (over) {
			throw new AssertionError(
					"%s: %d accessibility finding(s), which is more than the %d allowed.%n%s"
							.formatted(pageName, findings.size(), allowed, lines(findings)));
		}

		if (findings.size() < allowed) {
			LOGGER.info("'{}' is down to {} finding(s) from the {} allowed — tighten the step.",
					pageName, findings.size(), allowed);
		}
	}

	/**
	 * Asserts that the last scan in this scenario reported a particular Pa11y rule.
	 *
	 * <p>Mostly for proving the scan really measured the live DOM: inject a known
	 * defect, scan, and assert its rule code came back. Also useful to pin a defect
	 * you have deliberately accepted.
	 *
	 * <p>Example:
	 * <pre>
	 *   Then the accessibility scan reports the rule "WCAG2AA.Principle1.Guideline1_1.1_1_1.H37"
	 * </pre>
	 *
	 * @param ruleCode a Pa11y rule code, or any leading part of one
	 */
	@Then("the accessibility scan reports the rule {string}")
	public void accessibilityScanReportsRule(String ruleCode) {
		if (lastResult == null) {
			throw new IllegalStateException(
					"No page has been scanned in this scenario. Put a scan step before this one.");
		}

		boolean found = lastResult.issues().stream()
				.anyMatch(issue -> issue.code().startsWith(ruleCode));

		if (!found) {
			throw new AssertionError(
					"The scan of '%s' did not report the rule '%s'. It reported:%n%s"
							.formatted(lastPageName, ruleCode, lines(lastResult.issues())));
		}

		LOGGER.info("Scan of '{}' reported the expected rule '{}'", lastPageName, ruleCode);
	}

	/**
	 * Combines every page scanned in the run into one HTML report.
	 *
	 * <p>Findings shared across pages come first, most widespread first — twenty pages
	 * reporting the same unlabelled search box is one defect in a shared header, and the
	 * top of that list is where one fix clears the most findings.
	 */
	@AfterAll
	public static void writeAccessibilityReport() {
		try {
			AccessibilityScanner.writeCombinedReport();
		} catch (RuntimeException e) {
			// The suite has finished; a report we could not write must not mask its result.
			LOGGER.warn("Could not write the accessibility report: {}", e.getMessage(), e);
		}
	}

	// ── internals ────────────────────────────────────────────────────────────

	private void scan(String pageName) {
		try {
			lastResult = AccessibilityScanner.scanCurrentPage(pageName);
			lastPageName = pageName;
		} catch (ScanFailedException e) {
			// The scan did not happen. Saying so is the whole point — an unreachable
			// browser must never read as a page with no accessibility problems.
			throw new AssertionError(
					"The accessibility scan of '%s' could not be carried out (%s): %s"
							.formatted(pageName, e.kind(), e.getMessage()), e);
		}
	}

	private void report(String pageName, ScanResult result, boolean failed) {
		String summary = "%s — %d error(s), %d warning(s) on %s".formatted(
				pageName, result.errors().size(), result.warnings().size(), result.pageUrl());

		ExtentTest extentTest = Hooks.getExtentTest();
		if (extentTest == null) {
			return;
		}

		List<Issue> findings = AccessibilityScanner.countableIssues(result);
		String detail = findings.isEmpty()
				? summary
				: summary + "<pre>" + escapeHtml(lines(findings)) + "</pre>";

		if (failed) {
			extentTest.warning("Accessibility: " + detail);
		} else {
			extentTest.info("Accessibility: " + detail);
		}
	}

	private static String describe(String pageName, List<Issue> findings) {
		return "%s: %d accessibility finding(s).%n%s".formatted(pageName, findings.size(), lines(findings));
	}

	private static String lines(List<Issue> issues) {
		return issues.stream()
				.map(issue -> "  - " + issue.describe())
				.collect(Collectors.joining(System.lineSeparator()));
	}

	private static String escapeHtml(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
