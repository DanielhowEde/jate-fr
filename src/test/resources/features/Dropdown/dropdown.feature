Feature: Dropdown selection

  Scenario: User selects a dropdown option and sees the selected value
    Given I open the login page  
    When I login as "test" user
    Given the user is on the Dropdown page
    When the user selects "Option 2" from the dropdown
    And clicks the Go button
    Then the displayed value should be "option2"
