Feature: Calendar date selection

  Scenario: User selects a date and sees it displayed
    Given I open the login page  
    When I login as "test" user
    Given the user is on the Calendar page
    When the user selects the date "2024-12-25"
    And clicks the Go button
    Then the displayed value should be "2024-12-25"
