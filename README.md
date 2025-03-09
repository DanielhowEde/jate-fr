This README.md provides:
✅ Project Overview
✅ Setup Instructions
✅ How to Run Tests
✅ Folder Structure Explanation

📂 README.md
📍 Location: core-framework/README.md

# 🚀 Selenium Test Automation Framework

## 📌 Overview
This is a **modular Selenium test automation framework** built using **Java, JUnit 5, Selenium WebDriver, and Cucumber**.  
It supports **headless execution, dynamic test discovery, secure credential storage,** and **automatic test reporting** using ExtentReports.

---

## 📂 Project Structure

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

## 🛠️ **Setup Instructions**

### **1️⃣ Install Dependencies**
Ensure you have the following installed:
- **Java 17+**
- **Maven**
- **Google Chrome / Firefox / Edge**
- **ChromeDriver / GeckoDriver** (Managed automatically)

### **2️⃣ Configure `config.properties`**
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
3️⃣ Secure Credentials Using Java KeyStore (JKS)
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

Open reports/ExtentReport_<timestamp>.html in a browser.
📊 Key Features
✅ Selenium WebDriver with Headless Mode
✅ Secure Credentials with JKS & AWS Secrets Manager
✅ Data-Driven Testing with MySQL/PostgreSQL
✅ ExtentReports for Test Reporting
✅ Automatic Test Discovery (-tests Repos)
✅ Video Recording of Tests
