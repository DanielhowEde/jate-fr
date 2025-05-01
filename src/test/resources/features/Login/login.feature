@Login
Feature: Test Login page
  I want to use this template for my feature file
    
	Scenario: Successful Login check
	    Given I open the URL "https://fa-etan-dev11-saasfademo1.ds-fa.oraclepdemos.com/"
	    And I login as "HRSpecialist" user
	    And I navigate to "Home,My Client Groups,Hire an Employee"