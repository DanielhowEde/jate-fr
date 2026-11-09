@Accessibility
Feature: Accessibility of the pages this suite visits
  Pa11y measures the page the browser is already on, over CDP, so a screen is judged
  in the state the test left it — signed in, clicked into shape, nothing reloaded.

  Put a scan step after the steps that have brought a screen into the state worth
  checking. There are three kinds:

    * fails on any finding:
      [Then] the "<name>" page passes the accessibility check
    * holds a known backlog, fails only when it grows:
      [Then] the "<name>" page has no more than <n> accessibility errors
    * records the page and carries on:
      [When] I scan the page for accessibility as "<name>"

  Every scanned page lands in one HTML report at the end of the run, next to the
  Extent report: ../test-reports/<run>/accessibility/accessibility-report.html —
  findings shared across pages first, because one fix in a shared header clears the most.

  Requires Chrome started with --remote-debugging-port=9222 --remote-allow-origins=*
  (the same browser chrome.debug.address attaches to) and `npm ci` already run in
  pa11yRunner/scanner. See the pa11y.* settings in config.properties.

  @A11y_Demo
  Scenario: The demo form is accessible when it loads
    Given I open the URL "http://localhost:3000/form-demo.html"
    Then the "Form demo" page passes the accessibility check

  @A11y_Demo
  Scenario: The demo form is still accessible once it has been used
    Given I open the URL "http://localhost:3000/form-demo.html"
    When I click the button "Submit Form"
    Then the "Form demo — after submit" page passes the accessibility check

  @A11y_Survey
  Scenario: Survey the sign-in page without failing the run
    Given I open the URL "https://login-etan-dev11-saasfademo1.ds-fa.oraclepdemos.com/oam/server/"
    When I scan the page for accessibility as "Oracle sign-in"

  @A11y_Survey
  Scenario: Hold the line on a page that is not clean yet
  # Run the scenario above once, read the count from the report, then set it here.
  # Lower the number as the page improves; the step logs when it is beatable.
    Given I open the URL "https://login-etan-dev11-saasfademo1.ds-fa.oraclepdemos.com/oam/server/"
    Then the "Oracle sign-in" page has no more than 10 accessibility errors

  @A11y_SignedIn
  Scenario: The pages behind the sign-in are accessible
  # The scan borrows the test's own tab, so the session is simply there —
  # no second login, and the page is measured where the navigation left it.
    Given I open the URL "https://fa-etan-dev11-saasfademo1.ds-fa.oraclepdemos.com/"
    And I login as "HRSpecialist" user
    When I scan the page for accessibility as "Home"
    And I navigate to "Home,My Client Groups,Hire an Employee"
    When I scan the page for accessibility as "Hire an Employee"

# Scanning after an Excel-driven fill — the state is kept, so this measures the
# completed form rather than the empty one. Needs testdata/FormDemo.xlsx, as in
# features/Demo/form_demo.feature:
#
#   @A11y_Demo @Data_FormDemo
#   Scenario: The completed demo form is accessible
#     Given I open the URL "http://localhost:3000/form-demo.html"
#     When I complete all fields on the page using "FormData" for "1"
#     Then the "Form demo — completed" page passes the accessibility check
