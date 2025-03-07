@Login
Feature: Test Login page
  I want to use this template for my feature file

  Scenario: Successfull login
    Given I open the login page
    When I enter "test" into the username field
    And I enter "test" into the password field
    And I click the login button
    Then I should be redirected to the home page

  Scenario: Error on failure to Login
    Given I open the login page
    When I enter "admin" into the username field
    And I enter "admin" into the password field
    And I click the login button
    Then I should see an error message
    
Scenario: Login using jks file
    Given I open the login page  
    When I login as "test" user
    Then I should be redirected to the home page
    
Scenario: Failure check
    Given I open the login page  
    When I login as "fail" user
    Then I should be redirected to the home page