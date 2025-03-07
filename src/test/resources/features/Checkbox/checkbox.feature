Feature: Checkbox selection

  Scenario: User selects multiple checkboxes and views result
    Given I open the login page  
    When I login as "test" user
    Given the user is on the Checkbox page
    When the user selects "Option 1" and "Option 3"
    And clicks the Go button
    Then the displayed value should contain "option1"
    And the displayed value should contain "option3"
