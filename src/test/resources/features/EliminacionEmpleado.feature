# language: es
@PIM @Eliminacion
Característica: Eliminacion de empleados en el modulo PIM

  Escenario: El usuario elimina un empleado seleccionandolo desde el grid de resultados
    Dado que el usuario busca un empleado en la lista de OrangeHRM
    Cuando selecciona la casilla de verificación (checkbox) del empleado a eliminar
    Y hace clic en el botón "Delete" seleccionado
    Y confirma la acción en el modal emergente
    Entonces el registro del empleado debe desaparecer de la lista y mostrarse un mensaje de éxito

  Escenario: El usuario cancela el proceso de eliminacion de un empleado
    Dado que el usuario hace clic en el icono de eliminar de un empleado en la tabla
    Cuando aparece el modal de confirmación y hace clic en el botón "Cancel"
    Entonces el empleado no debe ser eliminado y debe permanecer visible en la lista

  Escenario: El usuario elimina varios empleados de forma simultanea
    Dado que el usuario selecciona los checkboxes de múltiples empleados en la lista
    Cuando hace clic en el botón "Delete Selected"
    Y confirma la acción en el cuadro de diálogo
    Entonces todos los empleados seleccionados deben ser eliminados del sistema
