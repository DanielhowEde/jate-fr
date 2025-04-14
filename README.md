This README.md provides:
✅ Project Overview
✅ Setup Instructions
✅ How to Run Tests
✅ Folder Structure Explanation

📂 README.md
📍 Location: jate-fr/README.md

# 🚀 Selenium Test Java Automation Framework

## 📌 Overview
This is a **modular Selenium test automation framework** built using **Java, JUnit 5, Selenium WebDriver, and Cucumber**.  
It supports **headless execution, dynamic test discovery, secure credential storage,** and **automatic test reporting** using ExtentReports.

---

## 📂 Project Structure
```
core-framework/ 
  │── src/
  │ │── core/base/
  │ │ ├── Hooks.java # Handles setup/teardown, screenshots, video recording
  │ │ ├── WebDriverManager.java # Manages WebDriver instances (supports headless mode)
  │ │ ├── BasePage.java # Handles UI interactions (click, input, dropdown, modals, toast popups)
  │ │── core/runner/ 
  │ │ ├── TestRunner.java # Dynamically detects & runs test repositories
  │ │── utils/ 
  │ │ │── reports/ 
  │ │ │ ├── ExtentManager.java # Configures ExtentReports 
  │ │ │── config/ 
  │ │ │ ├── ConfigReader.java # Reads config.properties 
  │ │ │── jks/ 
  │ │ │ ├── JKSReader.java # Reads JKS (Secure DB credentials) 
  │ │ │── database/ 
  │ │ │ ├── DatabaseManager.java # JDBC Database Connection & Queries 
  │ │ │── secrets/ 
  │ │ │ ├── SecretManagerUtil.java # AWS Secrets Manager + Env Variable Fallback 
  │ │ │── context/ 
  │ │ │ ├── ScenarioContext.java # Stores shared data between Cucumber scenarios 
  │ │ │── video/ 
  │ │ │ ├── VideoRecorder.java # Generates test execution videos 
  │── src/test/framework/ 
  │ │── ConfigReaderTest.java # ✅ Tests reading from config.properties 
  │ │── JKSReaderTest.java # ✅ Tests retrieving credentials from JKS 
  │ │── DatabaseTest.java # ✅ Tests database read & write operations 
  │ │── WebDriverManagerTest.java # ✅ Tests WebDriver initialization 
  │ │── BasePageTest.java # ✅ Tests UI interactions (clicks, inputs, dropdowns) 
  │ │── ScenarioContextTest.java # ✅ Tests scenario data storage 
  │ │── VideoRecorderTest.java # ✅ Tests video generation 
  │ │── ToastPopupTest.java # ✅ Tests toast popups 
  │ │── ModalHandlingTest.java # ✅ Tests modal handling 
  │ │── TestRunner.java # ✅ Auto-detects & runs tests from -tests repos 
  │── src/test/resources/ 
  │ │── config.properties # WebDriver, DB, and report configurations 
  │ │── db-credentials.jks # 🔒 Securely stores DB credentials 
  │── screenshots/ # 📷 Stores test screenshots 
  │── videos/ # 🎥 Stores generated test execution videos 
  │── pom.xml
  │── README.md
---
```
## 🛠️ **Setup Instructions**

### ** Install Dependencies**
Ensure you have the following installed:
- **Java 17+**
- **Maven**
- **Google Chrome / Firefox / Edge**
- **ChromeDriver / GeckoDriver** (Managed automatically)

### ** Configure `config.properties`**
Modify `src/test/resources/config.properties` to match your environment:

```properties
# WebDriver Settings
browser=chrome
headless=false
timeout=10

# Screenshot & Video Settings
screenshotMode=everyStep
recordVideo=true

# Database Configuration
db.url=jdbc:mysql://localhost:3306/testdb
db.driver=com.mysql.cj.jdbc.Driver

# Extent Reports
enableExtentReports=true
Secure Credentials Using Java KeyStore (JKS)
To store credentials securely, use:

keytool -genseckey -keystore src/test/resources/db-credentials.jks -storepass changeit -storetype JCEKS \
-alias dbUsername -keyalg AES -keysize 128 -keypass changeit

keytool -genseckey -keystore src/test/resources/db-credentials.jks -storepass changeit -storetype JCEKS \
-alias dbPassword -keyalg AES -keysize 128 -keypass changeit
🚀 Running Tests
Run All Tests

mvn test
Run Tests in Headless Mode

mvn test -Dheadless=true
Run a Specific Test Suite

mvn test -Dtest=DatabaseTest
Generate Extent Reports
Reports are saved in the reports/ directory.
To view reports:
```
### ** Configure `BasePage Elements`**
```
| Function Name                        | Parameters                                                                  | Description |
|-------------------------------------|------------------------------------------------------------------------------|-------------|
| `clickElementByLocator`             | `locator` (String)                                                           | Clicks an element after waiting for it to be clickable. |
| `enterTextInField`                  | `locator` (String), `text` (String)                                          | Clears and enters text into a field. |
| `selectDropdownOptionByText`       | `locator` (String), `value` (String)                                          | Selects an option from a dropdown by visible text. |
| `isElementVisible`                  | `locator` (String)                                                           | Checks if an element is present in the DOM. |
| `setCheckboxCheckedState`          | `locator` (String), `shouldBeChecked` (boolean)                               | Checks or unchecks a checkbox based on a boolean input. |
| `focusOnModal`                      | `modalLocator` (String)                                                      | Switches focus to a modal window. |
| `closeModalWindow`                  | `closeButtonLocator` (String)                                                | Closes a modal using a provided close button locator. |
| `handleJavaScriptAlert`            | `action` (String), `inputText` (String)                                       | Handles JavaScript alerts: accept, dismiss, or input text. |
| `getToastPopupMessage`             | `toastLocator` (String)                                                       | Retrieves the text from a toast popup. |
| `getTextFromElement`               | `locator` (String)                                                            | Retrieves the text content of an element. |
| `captureScreenshot`                | `screenshotName` (String)                                                     | Takes a screenshot and saves it to the `screenshots/` directory. |
| `hoverOnElement`                   | `locator` (String)                                                            | Hovers the mouse over an element. |
| `doubleClickElement`               | `locator` (String)                                                            | Performs a double-click on an element. |
| `rightClickElement`                | `locator` (String)                                                            | Performs a right-click on an element. |
| `getInputFieldValue`               | `locator` (String)                                                            | Gets the current value from an input field. |
| `clearTextField`                   | `locator` (String)                                                            | Clears a text field using CTRL+A + DELETE. |
| `waitUntilElementVisible`          | `locator` (String), `shouldBeVisible` (boolean)                               | Waits for an element to become visible or invisible. |
| `waitUntilElementVisible` (overload)| `locator` (String)                                                           | Waits for an element to become visible (default to visible = true). |
| `waitUntilTextAppears`             | `locator` (String), `expectedText` (String)                                | Waits until specific text appears in an element. |
| `scrollToElementByLocator`         | `locator` (String)                                                         | Scrolls the page until the specified element is in view. |
| `scrollToWebElement`               | `element` (WebElement)                                                    | Scrolls to a given WebElement. |
| `highlightElementBorder`           | `locator` (String)                                                         | Highlights an element with a red border using JavaScript. |
| `getTooltipFromElement`            | `locator` (String)                                                         | Gets the tooltip text (`title` attribute) of an element. |
| `selectOptionFromDynamicList`      | `inputLocator` (String), `listItemsLocator` (String), `valueToSelect` (String) | Selects a value from a dynamic list using input field and list of items. |
```

### ** Other Useful Functions **

Information can be passed steps inside a feature file using the following to store it
Hooks.getContext().set("<key>", "<data>");

It can then be recovered with 
String value = (String) Hooks.getContext().get("<key>");

## ** Tools **

### ** Setting up JSK to hold usernames and passwords **

In root directory of the application create a csv with headers alias,username,password

i.e. 
alias,username,password
devuser1,dev@example.com,devpass
qauser2,qa@example.com,qapass

 Create .env file containing
- ``` JKS_PASSWORD=MySuperSecureRandomPwd123!```

run the commands:
- ```javac -d out src/main/java/tools/createjks/CreateJKS.java```

 - ```java -cp out tools.createjks.CreateJKS```
 
 Then copy the credentials.jks to:
 src/main/resources 
 

###The createFolder.java script is used to scaffold a new test module inside the test suite. It creates a clean and consistent structure for each testable application, including:

-Maven project setup
- Creates Feature folder
- Creates Step definition stubs
- Creates Cucumber runner
- Creates App-specific config.properties

Adds the new test folder to the Docker batch run list

#### ✅ How to Use
Compile the initializer:


```bash
javac jate-fr\src\main\java\tools\createfolders\createFolder.java
```
Run it with your desired app name:

```bash
java jate-fr\src\main\java\tools\createfolders\createFolder <MyAppName>
```
This will create a new directory:


```bash
root/createFolder-tests/
```
With the following structure:

```
src/
└── test/
    ├── java/
    │   ├── runner/                # CucumberTestRunner.java
    │   └── stepdefinitions/       # App-specific step defs
    └── resources/
        ├── features/              # Feature files
        └── config.properties      # browser=chrome, headless=false, etc.
```
#### 📦 What It Includes
- CucumberTestRunner.java: Configured with glue paths for:
- stepdefinitions (app-specific)
- common.stepdefinitions (shared steps from jate-fr)
- core (Hooks, WebDriverManager, etc.) from jate-fr
- config.properties: Each test module gets its own, e.g.:

```properties.config
browser=chrome
headless=false
report.name=myappname_report
```
Docker Test Support: Automatically adds the test folder name to:


```bash
docker/test-folders.list
```
So the module will be picked up when running tests in parallel via Docker:


```bash
./docker/test-batch-runner.sh
```

💡 Tip
Make sure to run this from the root directory where jate-fr/ and your existing test modules are located.


