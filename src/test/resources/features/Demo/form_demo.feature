@Demo @Data_FormDemo
Feature: Form Demo — label-based auto-fill

  Scenario: Fill all field types from Excel iteration 1
    Given I open the URL "http://localhost:3000/form-demo.html"
    When I complete all fields on the page using "FormData" for "1"
    And I click the submit button
