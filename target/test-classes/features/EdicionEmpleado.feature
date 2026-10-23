# language: es
@PIM @Edicion
Característica: Edición de empleados en el modulo PIM

  Escenario: El usuario modifica con exito el apellido de un empleado existente
    Dado que el usuario busca y selecciona un empleado en la lista de OrangeHRM
    Cuando edita el campo "Last Name" con el nuevo valor "Smith-Johnson"
    Y hace clic en el botón "Save" de la sección personal details
    Entonces el sistema debe mostrar un mensaje de éxito indicando que los datos fueron actualizados

  Escenario: El sistema valida que no se guarden cambios con campos obligatorios vacios
    Dado que el usuario está editando los datos personales de un empleado
    Cuando borra el contenido del campo obligatorio "First Name"
    Y hace clic en el botón "Save"
    Entonces el sistema debe mostrar un mensaje de error indicando que el campo es requerido

  Escenario: El usuario actualiza la informacion de contacto del empleado
    Dado que el usuario accede al perfil del empleado y selecciona la pestaña "Contact Details"
    Cuando ingresa el nuevo correo "nuevo.correo@example.com" en el campo correspondiente
    Y hace clic en el botón "Save"
    Entonces los datos de contacto deben actualizarse correctamente en el sistema
