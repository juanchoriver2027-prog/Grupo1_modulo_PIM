# language: es
Característica: HU-01 Autenticación en el sistema Orange

  @HU_01 @CA_02
  Escenario: HU-01 CA-02 — Autenticación en el sistema Orange
    Dado que estoy en la pantalla de login
    Cuando ingreso credenciales inválidas
    Entonces el sistema muestra 'Invalid credentials' y no permite el acceso