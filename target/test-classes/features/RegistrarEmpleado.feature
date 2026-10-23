Feature: Módulo PIM

  Scenario: Registrar un empleado nuevo
    Given el administrador ha ingresado al sistema
    When el administrador realiza el registro de un empleado nuevo
    Then el empleado es registrado en el sistema