# language: es
@PIM @Busqueda
Característica: Búsqueda de empleados en el modulo PIM

  Escenario: El usuario busca un empleado por su nombre en el sistema
    Dado que el usuario inicia sesión en OrangeHRM
    Y navega a la sección "PIM" y luego a "Employee List"
    Cuando ingresa el nombre "John" en el campo de búsqueda de empleado
    Y hace clic en el botón "Search"
    Entonces el sistema debe mostrar los registros que coincidan con el nombre "John" en la tabla de resultados

  Escenario: El usuario busca un empleado utilizando su ID unico
    Dado que el usuario se encuentra en la lista de empleados de OrangeHRM
    Cuando ingresa el ID "0123" en el campo "Employee ID"
    Y hace clic en el botón "Search"
    Entonces el sistema debe mostrar unicamente el registro correspondiente al ID "0123"

  Escenario: El sistema informa cuando no se encuentran registros de un empleado
    Dado que el usuario está en la pantalla "Employee List" de OrangeHRM
    Cuando ingresa el nombre "UsuarioInexistenteXYZ" y hace clic en "Search"
    Entonces el sistema debe mostrar el mensaje "No Records Found" en la tabla de resultados
