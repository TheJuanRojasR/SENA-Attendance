# Casos de Uso — SENA Attendance

## Actores

| Actor | Descripción |
|-------|-------------|
| **Aprendiz** | Se autorregistra, consulta su asistencia, gestiona justificaciones (enviar, subsanar, cancelar), recibe alertas y notificaciones. Puede existir sin ficha hasta que el Administrador lo vincule. |
| **Instructor** | Creado por el Administrador. Toma asistencia de sus materias, decide justificaciones, registra excepciones no lectivas, atiende alertas y consulta sus fichas y materias. |
| **Administrador** | Creado por otro Administrador. Gestiona usuarios, catálogos, programas, trimestres, fichas, materias, aprendices, tipos de justificación, configuración global, alertas, notificaciones y dashboard. |
| **Usuario** | Rol genérico para operaciones de cuenta (modificar datos, cerrar sesión, recuperar contraseña, notificaciones, dashboard). |
| **Sistema** | Actor automático: calcula estados por fecha, genera y resuelve alertas, envía notificaciones y ejecuta el cierre de trimestres por fecha. |

## Reglas de negocio vigentes

1. **Autorregistro:** solo los Aprendices se autorregistran (UC001). Instructores y Administradores los crea un Administrador (UC006).
2. **Credenciales de cuentas creadas por Administrador:** el Administrador define una contraseña temporal; la cuenta queda activa de inmediato y el usuario debe cambiar la contraseña en su primer inicio (indicador `mustChangePassword`). El autorregistro y el reset autoservicio no activan ese indicador.
3. **Rol único por cuenta:** un usuario tiene un solo rol. El Administrador puede cambiar el rol (por ejemplo, Aprendiz → Instructor) conservando toda la historia, porque los registros cuelgan del perfil y no del rol. El rol Coordinador no se usa.
4. **Política de contraseñas (los tres roles):** 8 a 20 caracteres, con al menos una mayúscula, una minúscula, un número y un carácter especial. El cambio exige la contraseña actual y la nueva debe ser distinta de la actual.
5. **Inicio de sesión:** por tipo y número de documento + contraseña. El nombre de usuario interno se deriva del documento (`<iniciales>_<número>`).
6. **Sesión:** token válido por 24 horas. No hay bloqueo por intentos fallidos ni cierre por inactividad. El cierre es manual o al expirar el token.
7. **Eliminación de datos:** cuando un registro no tiene datos asociados se puede **eliminar definitivamente**; cuando los tiene, **nunca se elimina**: cambia de estado (desactivar, cancelar, retirar) para conservar auditoría e integridad. No existe soft-delete general. Los usuarios nunca se eliminan.
8. **Estados de ficha (UC007):** Pendiente, Activa y Finalizada se calculan por fechas; Aplazada y Cancelada son decisiones manuales del Administrador. Cada estado define qué se puede modificar y si admite materias y asistencia.
9. **Materias (competencia = class section):** pertenecen a una única ficha (UC015). Tienen un instructor (opcional al crear) y horarios por trimestre. Los horarios deben caer dentro de la **jornada** de la ficha, no cruzar la medianoche y no solaparse con otros de la ficha en el mismo trimestre.
10. **Asistencia (UC009):** se registra por **materia** y fecha de sesión; estados **A (Asistió)**, **F (Falla)** y **J (Justificada)**. La lista se presenta con todos en A por defecto. Solo el instructor asignado a la materia la registra y modifica. La edición se bloquea al cerrarse el trimestre.
11. **Justificaciones (UC010/UC011):** se presentan por período y materias afectadas; cada materia se decide por separado. Tienen marca de plazo (en tiempo / fuera de tiempo) y consumen cupo por tipo (en días). La subsanación de una parte rechazada tiene 2 días hábiles.
12. **Alertas de inasistencia (UC013):** dos tipos: **consecutivas** (por materia, sobre las sesiones de esa materia) y **acumuladas** (por ficha, sobre todas sus materias). Umbrales configurables. Una sola alerta activa por combinación; si el problema reaparece tras resolverse, se genera una nueva.
13. **Trimestres (UC014):** estados Futuro, Activo y Cerrado calculados por fechas. No hay cierre manual: el Administrador acorta la fecha fin. No hay ventana de gracia.
14. **Días hábiles:** los plazos se cuentan en días hábiles (lunes a viernes) y no dependen de la jornada de la ficha.
15. **Notificaciones:** son el canal de aviso de los flujos. El estado de **entrega** (interno) es independiente del estado de **lectura** del usuario.
16. **Reportes:** fuera de alcance de esta versión.

## Criterios transversales

- **Autenticación:** JWT; cada petición valida el token. Al expirar, se pide iniciar sesión de nuevo.
- **Auditoría:** las modificaciones de asistencia, las decisiones de justificaciones y las acciones manuales sobre fichas, trimestres y configuración dejan registro de usuario, fecha/hora y valores anteriores/nuevos.
- **Permisos por rol:** cada caso de uso indica su actor; las acciones administrativas son exclusivas del Administrador.
- **Paginación:** las listas están paginadas, tamaño por defecto **20** registros, sin límite de páginas.
- **Zona horaria:** todo el sistema opera en horario de Colombia (UTC-5).
- **Eliminación:** ver regla 7.
- **Política de contraseñas:** ver regla 4.

## Índice de casos de uso

| UC | Nombre | Actor | Módulo |
|----|--------|-------|--------|
| UC001 | Registrarme | Aprendiz | Cuenta y acceso |
| UC002 | Iniciar sesión | Usuario | Cuenta y acceso |
| UC003 | Modificar datos | Usuario | Cuenta y acceso |
| UC004 | Cerrar sesión | Usuario | Cuenta y acceso |
| UC005 | Recuperar contraseña | Usuario | Cuenta y acceso |
| UC019 | Gestionar configuración global | Administrador | Configuración y catálogos |
| UC020 | Gestionar jornadas | Administrador | Configuración y catálogos |
| UC021 | Gestionar modalidades | Administrador | Configuración y catálogos |
| UC022 | Gestionar tipos de documento | Administrador | Configuración y catálogos |
| UC016 | Gestionar tipos de justificación | Administrador | Configuración y catálogos |
| UC006 | Gestionar perfiles | Administrador | Usuarios |
| UC012 | Gestionar programas de aprendizaje | Administrador | Programas y trimestres |
| UC014 | Gestionar trimestres académicos | Administrador | Programas y trimestres |
| UC007 | Gestionar fichas | Administrador | Fichas y materias |
| UC015 | Gestionar materias | Administrador | Fichas y materias |
| UC008 | Gestionar aprendices | Administrador | Aprendices e instructor |
| UC017 | Consultar mis fichas y materias | Instructor | Aprendices e instructor |
| UC009 | Gestionar listas de asistencia | Instructor | Asistencia |
| UC011 | Gestionar asistencia (Aprendiz) | Aprendiz | Justificaciones |
| UC010 | Gestionar justificaciones | Instructor | Justificaciones |
| UC013 | Gestionar alertas de inasistencia | Sistema / Instructor / Aprendiz | Alertas y notificaciones |
| UC018 | Gestionar notificaciones | Usuario | Alertas y notificaciones |
| UC023 | Consultar dashboard | Usuario | Dashboard |

---



## Módulo: Cuenta y acceso

### UC001 — Registrarme

**Actor:** Aprendiz

**Descripción:** Permite que una persona con calidad de aprendiz cree su propia cuenta en el sistema. El registro **no exige ficha**: el aprendiz puede existir sin ficha hasta que el Administrador lo vincule (UC008). La cuenta queda **activa de inmediato**, sin correo de activación, y al terminar el registro el sistema redirige al inicio de sesión.

#### Reglas

- El registro es público (no requiere sesión).
- Datos del formulario, **todos obligatorios**: tipo de documento, número de documento, primer nombre, primer apellido, correo, teléfono y contraseña.
- **Segundo nombre y segundo apellido son opcionales** (no todas las personas los tienen).
- **Número de documento:** solo dígitos.
- **Teléfono:** exactamente **10 dígitos** (celular de Colombia, sin indicativo).
- **Correo:** debe tener formato válido y ser único en el sistema.
- **Contraseña:** política completa (igual para todos los roles): 8 a 20 caracteres, con al menos una mayúscula, una minúscula, un número y un carácter especial.
- La cuenta se crea con **rol Aprendiz** y estado Activo. El nombre de usuario interno se deriva del documento.
- Si el documento ya está registrado —aunque su cuenta esté desactivada— el registro se bloquea y se informa al aprendiz que debe contactar al Administrador.

#### Precondiciones

- La persona no debe tener una cuenta previamente creada en el sistema.

#### Flujo básico — Registro exitoso

1. El Aprendiz hace click en "Registrarme" desde la página de inicio de sesión.
2. El sistema muestra el formulario: tipo de documento, número de documento, primer nombre, segundo nombre (opcional), primer apellido, segundo apellido (opcional), correo, teléfono y contraseña.
3. El Aprendiz completa el formulario.
4. El Aprendiz hace click en "Registrar".
5. El sistema valida el formato y la obligatoriedad de los datos: número de documento solo dígitos; teléfono de 10 dígitos; correo con formato válido; contraseña conforme a la política.
6. El sistema verifica que el número de documento no exista previamente y que el correo no esté en uso.
7. El sistema crea el perfil con rol Aprendiz y estado Activo.
8. El sistema muestra el mensaje de registro exitoso y redirige al inicio de sesión.

#### Flujos alternativos

- **A1 — Cancelar registro:** en el paso 3 o 4, el Aprendiz hace click en "Cancelar". El sistema descarta los datos ingresados y regresa a la página de inicio de sesión.

#### Excepciones

- **E1 — Documento ya registrado:** en el paso 6, el sistema detecta que el documento ya existe y muestra "Este documento ya está registrado en el sistema". Si la cuenta existente está desactivada, agrega que debe contactar al Administrador para reactivarla. Mantiene el formulario abierto (excepto la contraseña).
- **E2 — Formato de datos inválido:** en el paso 5, si el documento no es numérico, el teléfono no tiene 10 dígitos, el correo no tiene formato válido o la contraseña no cumple la política, el sistema resalta el campo específico con el error correspondiente y no envía el formulario.
- **E3 — Correo ya en uso:** el sistema muestra "Este correo ya está en uso" y no permite continuar.
- **E4 — Pérdida de conexión durante el envío:** si la conexión se interrumpe entre los pasos 4 y 7, el sistema muestra "No se pudo completar el registro, intenta nuevamente" y no crea ningún perfil parcial.

#### Postcondiciones

El aprendiz cuenta con una cuenta activa con rol Aprendiz. Si no tiene fichas asociadas, su panel no muestra fichas ni asistencias hasta que el Administrador lo vincule (UC008).

### UC002 — Iniciar sesión

**Actor:** Usuario (Aprendiz, Instructor o Administrador)

**Descripción:** Permite el acceso autenticado al sistema mediante **tipo y número de documento + contraseña**. Incluye el cambio obligatorio de contraseña para las cuentas creadas por un Administrador.

#### Reglas

- Las credenciales son: tipo de documento, número de documento y contraseña. **No** se inicia sesión con usuario ni con correo.
- La cuenta debe existir y estar **Activa**.
- El sistema maneja **un solo rol por cuenta**.
- **Cambio obligatorio de contraseña:** si la cuenta tiene activo el indicador `mustChangePassword`, después de validar las credenciales el sistema **obliga** al usuario a cambiar la contraseña antes de permitir cualquier otra pantalla.
- **Vigencia de la sesión:** el token dura **24 horas**. Al expirar, el sistema pide iniciar sesión nuevamente.
- **No hay bloqueo por intentos fallidos** ni **cierre de sesión por inactividad**. El cierre de sesión es manual (UC004); una cuenta desactivada no puede volver a iniciar sesión y su sesión ya abierta deja de ser válida al expirar el token.
- Las cuentas creadas por el Administrador nacen con `mustChangePassword` activo; el autorregistro (UC001) y el reset autoservicio (UC005) **no** lo activan, porque en esos casos la contraseña la eligió el propio usuario.

#### Precondiciones

- El usuario debe tener una cuenta existente y estar en estado **Activo**.

#### Flujo básico — Inicio de sesión

1. El Usuario entra a la página de inicio de sesión.
2. El sistema muestra el formulario: tipo de documento, número de documento y contraseña.
3. El Usuario completa el formulario y hace click en "Iniciar sesión".
4. El sistema valida que el documento exista, que la contraseña sea correcta y que la cuenta esté Activa.
5. Si la cuenta tiene `mustChangePassword` activo, el sistema redirige **obligatoriamente** al cambio de contraseña (UC003) y no permite usar otra pantalla hasta completarlo.
6. Si no, el sistema establece la sesión (validez de 24 horas) y redirige al menú principal correspondiente al rol del usuario.

#### Flujos alternativos

- **A1 — Cambio obligatorio de contraseña:** cuando `mustChangePassword` está activo, tras validar las credenciales el sistema muestra la pantalla de cambio de contraseña. Al completarla correctamente, redirige al menú principal del rol.

#### Excepciones

- **E1 — Credenciales incorrectas:** si el documento o la contraseña no coinciden, el sistema muestra **"Usuario o contraseña incorrecta"** (mensaje genérico, sin revelar cuál de los dos falló) y permanece en la página de inicio de sesión.
- **E2 — Cuenta inactiva:** si la cuenta existe pero fue desactivada por el Administrador (UC006), el sistema muestra **"Tu cuenta se encuentra inactiva, contacta al administrador"** y no permite el acceso.
- **E3 — Sesión expirada:** cuando el token cumple las 24 horas, el sistema redirige al inicio de sesión con **"Tu sesión ha expirado, inicia sesión nuevamente"**.
- **Nota explícita:** no existe bloqueo por intentos fallidos ni cierre de sesión por inactividad.

#### Postcondiciones

El usuario queda autenticado con un token válido por 24 horas y accede al menú principal de su rol. Si debía cambiar la contraseña, la cambió antes de acceder.

### UC003 — Modificar datos

**Actor:** Usuario (Aprendiz, Instructor o Administrador)

**Descripción:** Permite a cualquier usuario autenticado actualizar sus datos personales (nombres, correo y teléfono) y cambiar su contraseña. El documento de identidad es **inmutable**.

#### Reglas

- Campos editables: primer nombre, segundo nombre (opcional), primer apellido, segundo apellido (opcional), teléfono y correo.
- **Teléfono:** exactamente 10 dígitos (celular de Colombia, sin indicativo), obligatorio.
- **Correo:** obligatorio y **único** (no puede pertenecer a otra cuenta). Se acepta de forma directa, **sin verificación** en el MVP.
- **Documento (tipo y número): inmutable.** El usuario no puede cambiarlo; solo el Administrador puede corregirlo (UC006).
- La edición es **parcial**: los campos que no se envían quedan sin cambios.
- **Cambio de contraseña:** exige la contraseña actual; la nueva debe cumplir la política completa (8–20, mayúscula, minúscula, número y carácter especial) y ser **distinta de la actual**.
- **Cierre de sesión:**
  - Cambio de contraseña **voluntario** (el usuario entra a "Actualizar datos") → al completarlo, el sistema **cierra la sesión** y redirige al inicio de sesión.
  - Cambio de contraseña **obligatorio** (forzado por `mustChangePassword` tras el login, UC002) → **no** cierra la sesión; redirige al menú principal del rol.

#### Precondiciones

- El usuario tiene una sesión activa (UC002).

#### Flujo básico — Actualizar nombre, correo o teléfono

1. El Usuario hace click en "Actualizar datos".
2. El Usuario modifica uno o más campos.
3. El Usuario hace click en "Actualizar".
4. El sistema valida el formato y la obligatoriedad de los datos, y que el correo no esté en uso por otra cuenta.
5. El sistema actualiza la información y muestra "Información actualizada".

#### Flujos alternativos

- **A1 — Cambiar contraseña (voluntario):** el Usuario ingresa su contraseña actual y la nueva. El sistema valida primero la contraseña actual y luego que la nueva cumpla la política y sea distinta de la actual. Al actualizarla, cierra la sesión y redirige al inicio de sesión.
- **A2 — Cambio obligatorio de contraseña:** llega desde UC002 cuando `mustChangePassword` está activo. El Usuario ingresa su contraseña actual y la nueva; el sistema aplica las mismas validaciones. Al completarlo, **no** cierra la sesión y redirige al menú principal del rol.
- **A3 — Cancelar modificación:** el Usuario cancela; el sistema descarta los cambios y vuelve a la vista anterior sin alterar la información.

#### Excepciones

- **E1 — Dato inválido:** si el nombre, correo o teléfono no cumplen el formato o la obligatoriedad, el sistema muestra el mensaje en el campo correspondiente y no guarda.
- **E2 — Correo ya utilizado por otra cuenta:** el sistema muestra "Este correo ya está en uso" y no actualiza el dato.
- **E3 — Intento de modificar el documento:** el sistema no permite editar tipo ni número de documento bajo ninguna circunstancia; si se intenta mediante manipulación de la solicitud, rechaza la operación con "Este dato no puede modificarse".
- **E4 — Contraseña actual incorrecta:** el sistema muestra "La contraseña actual no es correcta" y no realiza ningún cambio, sin cerrar la sesión.
- **E5 — Contraseña nueva inválida:** si no cumple la política, el sistema muestra "Contraseña no válida" y no guarda.
- **E6 — Contraseña nueva igual a la actual:** el sistema muestra "La nueva contraseña debe ser distinta de la actual" y no guarda.

#### Postcondiciones

Los datos del usuario quedan actualizados. Si cambió la contraseña de forma voluntaria, la sesión queda cerrada y debe iniciar sesión de nuevo; si el cambio fue obligatorio, la sesión continúa y el usuario accede al menú principal.

### UC004 — Cerrar sesión

**Actor:** Usuario (Aprendiz, Instructor o Administrador)

**Descripción:** Permite finalizar la sesión activa de forma manual. El cierre aplica **solo a la sesión del dispositivo actual**; las demás sesiones abiertas del usuario no se ven afectadas.

#### Reglas

- El cierre es **inmediato**, sin confirmación.
- Aplica únicamente al dispositivo o navegador actual; las otras sesiones del usuario siguen activas.
- Al cerrar, el sistema descarta el token de la sesión actual y redirige al inicio de sesión.
- Una vez cerrada, el usuario debe autenticarse de nuevo para acceder (UC002).
- **Nota de alcance:** si una cuenta es desactivada mientras tiene una sesión abierta, esa sesión no se corta al instante; deja de ser válida cuando el token expira (máximo 24 horas) y, en cualquier caso, la cuenta desactivada ya no puede iniciar sesión de nuevo. *(La invalidación inmediata queda como mejora futura.)*

#### Precondiciones

- El usuario tiene una sesión activa (UC002).

#### Flujo básico — Cierre manual

1. El Usuario hace click en "Cerrar sesión".
2. El sistema descarta el token de la sesión actual y limpia los datos de sesión del cliente.
3. El sistema redirige a la página de inicio de sesión.

#### Flujos alternativos

- No aplica.

#### Excepciones

- **E1 — Sesión ya expirada:** si el token ya venció, el sistema redirige al inicio de sesión con "Tu sesión ha expirado, inicia sesión nuevamente" (mismo comportamiento que UC002-E3).

#### Postcondiciones

La sesión del dispositivo actual queda cerrada y el usuario debe autenticarse nuevamente para acceder. Las otras sesiones abiertas del usuario permanecen activas hasta su propia expiración.

### UC005 — Recuperar contraseña

**Actor:** Usuario (Aprendiz, Instructor o Administrador)

**Descripción:** Permite a un usuario existente restablecer su contraseña cuando la olvidó, mediante un enlace de **un solo uso** enviado a su correo y válido por **30 minutos**.

#### Reglas

- La solicitud se hace con **tipo y número de documento**.
- Por seguridad, el sistema **no revela si el usuario existe**: muestra siempre el mismo mensaje neutro ("Si los datos son correctos, recibirás un correo con las instrucciones"), incluso si el correo no puede enviarse.
- Si el usuario existe y está activo, el sistema envía a su correo registrado un **enlace de recuperación** válido por **30 minutos** y de **un solo uso**.
- **No hay límite** de solicitudes de recuperación.
- La nueva contraseña debe cumplir la política completa (8–20, mayúscula, minúscula, número y carácter especial).
- El reset autoservicio **no** activa `mustChangePassword`: el usuario eligió su propia contraseña.
- Al completar el cambio, el sistema redirige al inicio de sesión.

#### Precondiciones

- El usuario debe tener una cuenta previamente creada y activa en el sistema.

#### Flujo básico — Cambio de contraseña exitoso

1. El Usuario hace click en "Recuperar contraseña" desde la página de inicio de sesión.
2. El sistema muestra el formulario: tipo y número de documento.
3. El Usuario ingresa sus datos.
4. El sistema muestra el mensaje neutro (siempre) y, si el usuario existe y está activo, envía a su correo el enlace de recuperación.
5. El Usuario abre el enlace recibido.
6. El sistema muestra el formulario de nueva contraseña.
7. El Usuario ingresa y confirma la nueva contraseña.
8. El sistema valida la política y actualiza la contraseña; muestra la página de inicio de sesión.

#### Flujos alternativos

- No aplica.

#### Excepciones

- **E1 — Datos que no corresponden a ninguna cuenta:** por seguridad, el sistema responde exactamente igual que en el caso exitoso (mensaje neutro), sin revelar nada. *(Reemplaza el "Usuario no encontrado" del documento original.)*
- **E2 — Nueva contraseña inválida:** si no cumple la política, el sistema muestra "Contraseña no válida" y solicita ingresarla nuevamente.
- **E3 — Enlace expirado:** si el Usuario accede después de los 30 minutos, el sistema muestra "El enlace ha expirado, solicita uno nuevo" y lo redirige al paso 1.
- **E4 — Enlace ya utilizado:** si el enlace ya se usó para cambiar la contraseña, el sistema lo invalida y muestra "Este enlace ya no es válido".
- **E5 — Fallo en el envío del correo:** el sistema mantiene el mensaje neutro (no revela el problema), registra la falla internamente y el Usuario puede volver a solicitarlo.

#### Postcondiciones

La contraseña del usuario queda actualizada y puede iniciar sesión con la nueva. El enlace de recuperación queda invalidado.


## Módulo: Configuración y catálogos

### UC019 — Gestionar configuración global

**Actor:** Administrador

**Descripción:** Permite consultar y actualizar la configuración global del sistema: los plazos de las justificaciones y los umbrales de las alertas de inasistencia. Es un **singleton**: existe una única configuración para todo el sistema, sin importar ficha, materia o programa.

#### Parámetros y reglas

- **Días para justificar** (`studentJustificationDays`, default 5): ventana que tiene el aprendiz para presentar una justificación.
- **Días de respuesta del instructor** (`instructorResponseDays`, default 2): plazo esperado para que el instructor decida una justificación.
- **Umbral de fallas consecutivas** (`consecutiveAbsenceAlertThreshold`, default 3): para la alerta por materia (UC013).
- **Umbral de fallas acumuladas** (`accumulatedAbsenceAlertThreshold`, default 5): para la alerta por ficha (UC013).
- Los plazos se cuentan en **días hábiles (lunes a viernes)** y no dependen de la jornada de la ficha.
- La actualización es **parcial**: los campos omitidos quedan sin cambios.
- Los cambios aplican **hacia adelante**: no recalculan justificaciones, alertas ni conteos ya existentes.
- Si un valor no está definido, aplica su default.

#### Precondiciones

- El Administrador tiene una sesión activa (UC002).

#### Flujo básico — Consultar y actualizar la configuración

1. El Administrador abre "Configuración global".
2. El sistema muestra los valores vigentes de los cuatro parámetros.
3. El Administrador modifica uno o más valores y confirma.
4. El sistema valida que estén dentro de los rangos permitidos y guarda los cambios.
5. El sistema muestra "Configuración actualizada" e informa que los cambios aplican hacia adelante.

#### Flujos alternativos

- No aplica.

#### Excepciones

- **E1 — Valor inválido:** si un plazo queda fuera del rango permitido (1 a 30 días) o un umbral es menor a 1, el sistema muestra el mensaje en el campo correspondiente y no guarda.
- **E2 — Sin permisos:** cualquier usuario que no sea Administrador que intente modificar la configuración recibe "No tienes permisos para esta acción" y no se ejecuta el cambio.

#### Postcondiciones

La configuración global queda actualizada. Los flujos de justificaciones (UC010, UC011) y de alertas (UC013) usan los nuevos valores desde ese momento, sin recalcular lo ya ocurrido.

### UC020 — Gestionar jornadas

**Actor:** Administrador

**Descripción:** Permite administrar el catálogo de **jornadas** (Diurna, Nocturna, Madrugada, Mixta u otras). La jornada define la **disponibilidad horaria** de una ficha y actúa como **tope** de los horarios de sus materias (UC015): ninguna sesión puede salirse de ese rango.

#### Reglas

- Campos de la jornada: **nombre** (máximo 50), **hora inicio**, **hora fin** y **estado** (activo / inactivo).
- El **nombre es único**.
- La jornada nace **Activa**; el estado se cambia con Desactivar / Reactivar.
- **Rango horario:** normalmente la hora inicio es anterior a la hora fin. Se permite que **la hora fin sea menor que la hora inicio** para representar jornadas que **cruzan la medianoche** (ej. Madrugada 22:00–06:00). Una jornada **Mixta** se define con un rango amplio que cubra los dos turnos (ej. 06:00–22:00).
- La hora inicio y la hora fin **no pueden ser iguales**.
- La jornada es **editable**; si ya está en uso por fichas, el cambio de rango aplica a las validaciones **futuras** de horarios, sin recalcular los horarios ya existentes.
- **Desactivar:** la jornada deja de ofrecerse para crear fichas nuevas; las fichas existentes no se afectan. **Reactivar** la vuelve a habilitar.
- **Eliminar:** solo se elimina si **no está en uso por ninguna ficha**. Si está en uso, no se elimina: se desactiva.

#### Precondiciones

- El Administrador tiene una sesión activa (UC002).

#### Flujo básico — Crear jornada

1. El Administrador abre "Jornadas".
2. El sistema muestra la lista de jornadas (nombre, rango horario y estado).
3. El Administrador hace click en "Crear jornada".
4. El sistema muestra el formulario: nombre, hora inicio y hora fin.
5. El Administrador completa el formulario y confirma.
6. El sistema valida el nombre único y que las horas no sean iguales.
7. El sistema crea la jornada en estado Activo y muestra "Jornada creada".

#### Flujos alternativos

- **A1 — Modificar jornada:** el Administrador edita nombre, hora inicio o hora fin. El sistema valida y actualiza; el nuevo rango aplica a validaciones futuras.
- **A2 — Desactivar jornada:** deja de ofrecerse para fichas nuevas; las fichas existentes no se afectan.
- **A3 — Reactivar jornada:** vuelve a estar disponible.
- **A4 — Consultar jornadas:** el sistema muestra la lista paginada (20 registros por página por defecto).
- **A5 — Eliminar jornada:** solo si no está en uso por fichas; se elimina definitivamente tras confirmación.

#### Excepciones

- **E1 — Nombre duplicado:** el sistema muestra "Ya existe una jornada con este nombre" y no guarda.
- **E2 — Horas iguales:** si la hora inicio y la hora fin coinciden, el sistema muestra "La hora de inicio y la hora de fin no pueden ser iguales" y no guarda.
- **E3 — Eliminar una jornada en uso:** el sistema muestra "No es posible eliminar la jornada: está asignada a fichas. Puedes desactivarla" y no elimina.
- **E4 — Datos inválidos:** el sistema señala el campo con problema y no guarda hasta corregir.

#### Postcondiciones

La jornada queda creada, modificada, activa, inactiva o eliminada según la operación. Si está activa, queda disponible para seleccionarse al crear fichas (UC007) y como tope de los horarios de materias (UC015).

### UC021 — Gestionar modalidades

**Actor:** Administrador

**Descripción:** Permite administrar el catálogo de **modalidades** de formación (Presencial, Virtual, etc.) que se seleccionan al crear una ficha (UC007).

#### Reglas

- Campos de la modalidad: **nombre** (máximo 50) y **estado** (activo / inactivo).
- El **nombre es único**.
- La modalidad nace **Activa**; el estado se cambia con Desactivar / Reactivar.
- Es **editable** en cualquier momento, respetando la unicidad.
- **Desactivar:** deja de ofrecerse para crear fichas nuevas; las fichas existentes no se afectan. **Reactivar** la vuelve a habilitar.
- **Eliminar:** solo se elimina si **no está en uso por ninguna ficha**. Si está en uso, no se elimina: se desactiva.

#### Precondiciones

- El Administrador tiene una sesión activa (UC002).

#### Flujo básico — Crear modalidad

1. El Administrador abre "Modalidades".
2. El sistema muestra la lista de modalidades (nombre y estado).
3. El Administrador hace click en "Crear modalidad".
4. El sistema muestra el formulario: nombre.
5. El Administrador completa el formulario y confirma.
6. El sistema valida que el nombre no esté repetido.
7. El sistema crea la modalidad en estado Activo y muestra "Modalidad creada".

#### Flujos alternativos

- **A1 — Modificar modalidad:** el Administrador edita el nombre; el sistema valida la unicidad y actualiza.
- **A2 — Desactivar modalidad:** deja de ofrecerse para fichas nuevas; las fichas existentes no se afectan.
- **A3 — Reactivar modalidad:** vuelve a estar disponible.
- **A4 — Consultar modalidades:** el sistema muestra la lista paginada (20 registros por página por defecto).
- **A5 — Eliminar modalidad:** solo si no está en uso por fichas; se elimina definitivamente tras confirmación.

#### Excepciones

- **E1 — Nombre duplicado:** el sistema muestra "Ya existe una modalidad con este nombre" y no guarda.
- **E2 — Eliminar una modalidad en uso:** el sistema muestra "No es posible eliminar la modalidad: está asignada a fichas. Puedes desactivarla" y no elimina.
- **E3 — Datos inválidos:** el sistema señala el campo con problema y no guarda hasta corregir.

#### Postcondiciones

La modalidad queda creada, modificada, activa, inactiva o eliminada según la operación. Si está activa, queda disponible para seleccionarse al crear fichas (UC007).

### UC022 — Gestionar tipos de documento

**Actor:** Administrador

**Descripción:** Permite administrar el catálogo de **tipos de documento** (CC, TI, CE, Pasaporte, etc.). Sus **iniciales** se usan para construir el nombre de usuario interno (ej. `cc_12345678`) y aparecen en los formularios de registro e inicio de sesión.

#### Reglas

- Campos del tipo de documento: **nombre** (máximo 30), **iniciales** (máximo 10) y **estado** (activo / inactivo).
- **Nombre e iniciales son únicos** (el nombre sin distinguir mayúsculas; las iniciales se normalizan a mayúsculas).
- El tipo nace **Activo**; el estado se cambia con Desactivar / Reactivar.
- **Edición de iniciales:** si el tipo **ya está en uso** por usuarios, las iniciales **no se pueden modificar** (los logins existentes quedarían desincronizados). El nombre sí se puede modificar en cualquier momento.
- **Desactivar:** deja de ofrecerse en el registro y en la creación de perfiles; los usuarios ya creados con ese tipo no se afectan.
- **Eliminar:** solo se elimina si **ningún perfil lo usa**. Si está en uso, no se elimina: se desactiva.

#### Precondiciones

- El Administrador tiene una sesión activa (UC002).

#### Flujo básico — Crear tipo de documento

1. El Administrador abre "Tipos de documento".
2. El sistema muestra la lista de tipos (nombre, iniciales y estado).
3. El Administrador hace click en "Crear tipo".
4. El sistema muestra el formulario: nombre e iniciales.
5. El Administrador completa el formulario y confirma.
6. El sistema valida que el nombre y las iniciales no estén repetidos.
7. El sistema crea el tipo en estado Activo y muestra "Tipo de documento creado".

#### Flujos alternativos

- **A1 — Modificar tipo:** el Administrador edita el nombre siempre; las iniciales solo si el tipo no está en uso. El sistema valida y actualiza.
- **A2 — Desactivar tipo:** deja de ofrecerse en los formularios; los usuarios existentes no se afectan.
- **A3 — Reactivar tipo:** vuelve a estar disponible.
- **A4 — Consultar tipos:** el sistema muestra la lista paginada (20 registros por página por defecto).
- **A5 — Eliminar tipo:** solo si no lo usa ningún perfil; se elimina definitivamente tras confirmación.

#### Excepciones

- **E1 — Nombre duplicado:** el sistema muestra "Ya existe un tipo de documento con este nombre" y no guarda.
- **E2 — Iniciales duplicadas:** el sistema muestra "Ya existe un tipo de documento con estas iniciales" y no guarda.
- **E3 — Iniciales en uso:** al intentar cambiar las iniciales de un tipo ya usado, el sistema muestra "No se pueden modificar las iniciales de un tipo de documento en uso" y no guarda.
- **E4 — Eliminar un tipo en uso:** el sistema muestra "No es posible eliminar el tipo de documento: está en uso por usuarios. Puedes desactivarlo" y no elimina.
- **E5 — Datos inválidos:** el sistema señala el campo con problema y no guarda hasta corregir.

#### Postcondiciones

El tipo de documento queda creado, modificado, activo, inactivo o eliminado según la operación. Si está activo, queda disponible en el registro de aprendices (UC001) y en la creación de perfiles (UC006).

### UC016 — Gestionar tipos de justificación

**Actor:** Administrador

**Descripción:** Permite al Administrador administrar el catálogo de tipos de justificación (motivos) que los aprendices seleccionan al justificar una inasistencia (UC011). Cada tipo define cuántos **días** se pueden justificar con él por trimestre.

#### Reglas del catálogo

- Cada tipo de justificación tiene: **nombre**, **límite de días por trimestre** y **estado** (Activo / Inactivo).
- El límite se mide en **días justificables por trimestre** (no en cantidad de justificaciones). Un día cuenta una sola vez, sin importar cuántas materias tenga ese día.
- Solo los tipos **Activos** están disponibles para los aprendices al justificar. Un tipo inactivo no se ofrece, pero las justificaciones ya presentadas con él no se ven afectadas.
- Los cambios de límite aplican **hacia adelante**: no recalculan justificaciones ni contadores ya usados.
- **Eliminar:** un tipo que nunca fue usado en ninguna justificación puede eliminarse definitivamente. Si ya tiene justificaciones asociadas, no se elimina: se **desactiva** para conservar la trazabilidad.

#### Precondiciones

- El Administrador tiene una sesión activa (UC002).

#### Flujo básico — Crear tipo de justificación

1. El Administrador accede al módulo "Tipos de justificación".
2. El sistema muestra la lista de tipos existentes (nombre, límite de días, estado).
3. El Administrador hace click en "Crear tipo".
4. El sistema muestra el formulario: nombre y límite de días por trimestre.
5. El Administrador completa los datos y confirma.
6. El sistema valida que el nombre no esté repetido y que el límite sea un número entero mayor a 0.
7. El sistema crea el tipo en estado Activo y muestra "Tipo de justificación creado".

#### Flujos alternativos

- **A1 — Modificar tipo:** el Administrador edita el nombre o el límite de días. El sistema valida y actualiza; el nuevo límite aplica hacia adelante.
- **A2 — Desactivar tipo:** el Administrador desactiva un tipo; deja de estar disponible para nuevas justificaciones, sin afectar las existentes.
- **A3 — Reactivar tipo:** el Administrador reactiva un tipo inactivo; vuelve a estar disponible en el formulario del aprendiz.
- **A4 — Eliminar tipo:** si el tipo nunca fue usado, el Administrador lo elimina definitivamente tras confirmación. Si ya tiene justificaciones asociadas, el sistema no permite eliminarlo y ofrece desactivarlo.

#### Excepciones

- **E1 — Nombre duplicado:** si ya existe un tipo con ese nombre, el sistema muestra "Ya existe un tipo de justificación con este nombre" y no guarda.
- **E2 — Límite inválido:** si el límite no es un número entero mayor a 0, el sistema muestra "El límite debe ser un número mayor a 0" y no guarda.
- **E3 — Eliminar un tipo en uso:** si el tipo ya tiene justificaciones asociadas, el sistema muestra "No es posible eliminar el tipo: ya fue usado en justificaciones. Puedes desactivarlo" y no elimina.
- **E4 — Datos inválidos:** el sistema señala el campo con problema y no guarda hasta corregir.

#### Postcondiciones

El tipo de justificación queda creado, modificado, activo, inactivo o eliminado según la operación. Los tipos activos quedan disponibles para el registro de justificaciones (UC011), respetando su límite de días por trimestre.


## Módulo: Usuarios

### UC006 — Gestionar perfiles

**Actor:** Administrador

**Descripción:** Permite al Administrador crear, consultar, modificar, desactivar y reactivar cuentas de **Instructores y Administradores**, crear **Aprendices como excepción** cuando el autorregistro (UC001) no sea posible, y corregir los datos de perfiles de cualquier rol. Los Aprendices **no** se crean en el flujo normal: se autorregistran. El sistema maneja **un solo rol por cuenta**; un usuario puede **cambiar de rol** conservando toda su historia.

#### Reglas

- Los roles del sistema son **Administrador**, **Instructor** y **Aprendiz** (el rol Coordinador no se usa).
- **Un solo rol por cuenta.** El Administrador puede cambiar el rol de un usuario (por ejemplo, Aprendiz → Instructor) **sin perder historia**: los registros de aprendiz y las materias asignadas cuelgan del perfil, no del rol.
- **Guarda al desactivar o al quitar el rol Instructor:** si el usuario es el **único instructor** asignado a materias de fichas en estado Pendiente o Activa, el sistema bloquea la operación y muestra las materias y fichas afectadas, exigiendo reasignar antes de continuar.
- **Login derivado:** el nombre de usuario se construye automáticamente como `<iniciales del tipo de documento>_<número de documento>` (ej. `cc_12345678`). No se edita a mano. Si el Administrador corrige el documento, el login se recalcula.
- **Documento:** el propio usuario no puede cambiarlo (UC003), pero el **Administrador sí**, para corregir errores de digitación, respetando la unicidad del documento.
- **Correo:** editable por el Administrador, siempre que no lo tenga ya otra cuenta (es único).
- **Contraseña:** el Administrador define una **contraseña temporal** al crear el usuario; el sistema envía el correo de activación; el usuario debe **cambiarla obligatoriamente en su primer inicio de sesión**. Si el correo no puede enviarse, queda una notificación pendiente para reenvío manual (UC018).
- **Política de contraseñas** (igual para los tres roles): 8 a 20 caracteres, con al menos una mayúscula, una minúscula, un número y un carácter especial.
- **Desactivar / Reactivar:** cambia el estado de la cuenta; un usuario desactivado no puede iniciar sesión. **Los usuarios nunca se eliminan.** Un Aprendiz se desactiva sin restricciones; sus asistencias y justificaciones se conservan.
- **Guarda de administradores:** no se puede desactivar la **última cuenta de Administrador activa**, y la cuenta `admin` está **protegida** (no se desactiva nunca).
- **Aprendiz creado por excepción:** si el autorregistro falla, el Administrador puede crear un aprendiz con los mismos datos de UC001. Crearlo **no** lo vincula a una ficha (eso es UC008).

#### Precondiciones

- El Administrador tiene una sesión activa (UC002).

#### Flujo básico — Crear usuario (Instructor o Administrador)

1. El Administrador abre "Gestionar usuarios".
2. El sistema muestra la lista de usuarios (paginada y filtrable).
3. El Administrador hace click en "Crear usuario".
4. El sistema muestra el formulario: datos personales, tipo y número de documento, correo, teléfono, rol y contraseña temporal.
5. El Administrador completa el formulario y confirma.
6. El sistema valida los datos: documento único, correo único y contraseña conforme a la política.
7. El sistema crea el usuario y su perfil, genera el login, envía el correo de activación y muestra "Usuario creado".
8. Si el correo de activación no puede enviarse, el sistema conserva el usuario creado y deja una notificación pendiente para reenvío.

#### Flujos alternativos

- **A1 — Modificar usuario:** el Administrador edita nombres, correo, teléfono, tipo y número de documento, y rol, con las reglas y guardas anteriores. El sistema valida y actualiza.
- **A2 — Desactivar usuario:** el Administrador confirma; si es Instructor, el sistema valida la guarda del instructor único; si es Administrador, valida que quede otro administrador activo; cambia el estado a Inactivo.
- **A3 — Reactivar usuario:** el Administrador reactiva una cuenta inactiva y el sistema cambia el estado a Activo.
- **A4 — Consultar usuarios:** el Administrador filtra por rol, estado, nombre o documento; el sistema muestra los resultados paginados (20 registros por página por defecto).
- **A5 — Desactivar / Reactivar Aprendiz:** sin restricciones; el sistema cambia el estado de la cuenta del aprendiz.
- **A6 — Crear Aprendiz por excepción:** el Administrador usa el mismo formulario de creación con rol Aprendiz, para el caso en que el autorregistro no sea posible. El aprendiz queda creado sin ficha asociada.

#### Excepciones

- **E1 — Datos inválidos al crear/modificar:** el sistema muestra el mensaje en el campo específico y no guarda hasta corregir.
- **E2 — Documento ya registrado:** si el documento pertenece a otro usuario (incluido un Aprendiz autorregistrado), el sistema muestra "Este documento ya está en uso" y no permite continuar.
- **E3 — Correo ya registrado:** si el correo ya lo usa otra cuenta, el sistema muestra "Este correo ya está en uso" y no actualiza el dato.
- **E4 — Contraseña fuera de política:** si no cumple los requisitos, el sistema muestra "Contraseña no válida" y no crea el usuario.
- **E5 — Instructor único en fichas operativas:** si el instructor es el único asignado a materias de fichas Pendiente o Activa, el sistema bloquea la desactivación o el cambio de rol, muestra las materias y fichas afectadas, y exige reasignar antes de continuar.
- **E6 — Guarda de administradores:** el sistema bloquea la operación con "Debe existir al menos un Administrador activo" o "La cuenta admin está protegida y no puede desactivarse".
- **E7 — Fallo al enviar las credenciales:** el usuario se crea igualmente; el sistema marca la notificación como pendiente de reenvío y permite al Administrador reenviarla manualmente.

#### Postcondiciones

El usuario queda creado, modificado, activo, inactivo o con rol cambiado según la operación. El perfil y el login quedan coherentes con el documento. Las credenciales se envían al usuario (o quedan pendientes de reenvío). Nunca se elimina un usuario ni se pierde su historia.


## Módulo: Programas y trimestres

### UC012 — Gestionar programas de aprendizaje

**Actor:** Administrador

**Descripción:** Permite crear, modificar, desactivar, reactivar, consultar y eliminar programas de aprendizaje. Un programa agrupa fichas; solo los programas activos pueden recibir fichas nuevas. Las **competencias/materias no se gestionan aquí**: viven por ficha (UC015).

#### Reglas

- Campos del programa: **nombre** (máximo 200), **iniciales** (máximo 10), **código** (solo números, máximo 30), **cantidad de trimestres** (1 a 12) y **estado** (activo / inactivo).
- El programa **nace siempre Activo**; el estado no se elige al crear, solo se cambia con Desactivar / Reactivar.
- Son **únicos** (sin distinguir mayúsculas): nombre, iniciales y código. Las iniciales se normalizan a mayúsculas y el código es numérico.
- La **cantidad de trimestres** es informativa (la duración declarada del programa); no restringe las fichas en el MVP.
- Los campos son **editables en cualquier momento**, incluso si el programa ya tiene fichas, respetando la unicidad. Editar no recalcula fichas existentes.
- **Desactivar:** el programa deja de estar disponible para crear fichas nuevas; las fichas existentes, sus materias y asistencias no se afectan. Si el programa tiene fichas activas, el sistema **permite** la desactivación pero muestra una advertencia con la cantidad de fichas afectadas.
- **Reactivar:** el programa vuelve a estar disponible para crear fichas.
- **Eliminar:** solo se puede eliminar un programa que **no tenga fichas asociadas en ningún estado**. Si tiene fichas (aunque estén canceladas o finalizadas), no se elimina: solo se desactiva.
- Los programas activos son los que alimentan la creación de fichas (UC007) y la validación del programa activo.

#### Precondiciones

- El Administrador tiene una sesión activa (UC002).

#### Flujo básico — Crear programa de aprendizaje

1. El Administrador abre "Programas de aprendizaje".
2. El sistema muestra la página de gestión con la lista de programas (paginada).
3. El Administrador hace click en "Crear programa".
4. El sistema muestra el formulario: nombre, iniciales, código y cantidad de trimestres.
5. El Administrador completa el formulario y confirma.
6. El sistema valida: campos completos; código solo numérico; cantidad de trimestres entre 1 y 12; nombre, iniciales y código no repetidos.
7. El sistema crea el programa en estado Activo y muestra "Programa creado".

#### Flujos alternativos

- **A1 — Modificar programa:** el Administrador edita nombre, iniciales, código o cantidad de trimestres y confirma. El sistema valida la unicidad y actualiza; no recalcula fichas existentes.
- **A2 — Desactivar programa:** el Administrador confirma la desactivación. Si hay fichas activas, el sistema muestra la advertencia con la cantidad y, aun así, deja el programa Inactivo.
- **A3 — Reactivar programa:** el Administrador reactiva un programa inactivo y el sistema lo marca Activo.
- **A4 — Consultar programas:** el Administrador filtra por nombre, código o estado; el sistema muestra los resultados paginados (20 registros por página, estándar del sistema).

#### Excepciones

- **E1 — Datos inválidos:** el sistema señala el campo con problema y no guarda hasta corregir.
- **E2 — Código duplicado:** el sistema muestra "Ya existe un programa con este código" y no guarda.
- **E3 — Nombre duplicado:** el sistema muestra "Ya existe un programa con este nombre" y no guarda.
- **E4 — Iniciales duplicadas:** el sistema muestra "Ya existe un programa con estas iniciales" y no guarda.
- **E5 — Código no numérico:** si el código contiene caracteres distintos de dígitos, el sistema muestra "El código debe contener solo números" y no guarda.
- **E6 — Cantidad de trimestres fuera de rango:** si no está entre 1 y 12, el sistema muestra "La cantidad de trimestres debe estar entre 1 y 12" y no guarda.
- **E7 — Desactivar con fichas activas:** el sistema no bloquea la desactivación; muestra la advertencia "Este programa tiene N fichas activas; no podrán crearse nuevas fichas bajo este programa hasta reactivarlo".
- **E8 — Eliminar un programa con fichas:** el sistema muestra "No es posible eliminar el programa: tiene fichas asociadas. Puedes desactivarlo" y no elimina.

#### Postcondiciones

El programa queda creado, modificado, activo, inactivo o eliminado según la operación. Si está activo, queda disponible para la creación de fichas (UC007); si está inactivo, no se pueden crear fichas nuevas bajo él.

### UC014 — Gestionar trimestres académicos

**Actor:** Administrador

**Descripción:** Permite crear, modificar, consultar y eliminar trimestres académicos globales. El **trimestre activo** define el período dentro del cual se registran y modifican asistencias, horarios y justificaciones. No existe cierre manual: el estado del trimestre se determina automáticamente por sus fechas.

#### Estados y reglas

El trimestre tiene tres estados, calculados siempre a partir de sus fechas (un proceso diario mantiene el indicador sincronizado):

| Estado | Condición | Qué se puede modificar |
|---|---|---|
| **Futuro** | fecha inicio > hoy | Nombre, fecha inicio (debe seguir siendo futura), fecha fin |
| **Activo** | fecha inicio ≤ hoy ≤ fecha fin | Nombre, fecha fin (no anterior a hoy). La **fecha inicio está congelada** |
| **Cerrado** | fecha fin < hoy | Nada |

Reglas generales:

- La **fecha inicio** debe ser anterior a la **fecha fin**.
- No se puede crear un trimestre con la fecha fin ya vencida.
- Al crear, la **fecha inicio debe ser desde mañana** en adelante.
- Los trimestres no pueden **solaparse** entre sí. Como los rangos no se solapan, solo puede existir un trimestre activo a la vez.
- **Desactivar un trimestre:** el Administrador acorta la fecha fin (mínimo el día de hoy). Si pone la fecha fin en hoy, el trimestre sigue activo durante todo el día y queda cerrado al día siguiente. No hay cierre instantáneo.
- Cambiar la fecha de inicio queda **bloqueado si el trimestre ya tiene asistencias** registradas.
- **Eliminar:** un trimestre solo se elimina si no tiene **horarios ni asistencias** asociados. Si tiene cualquiera de los dos, no se elimina.
- El cierre del trimestre bloquea la edición de asistencia (regla detallada en UC009); no existe ventana de gracia.

#### Precondiciones

- El Administrador tiene una sesión activa (UC002).

#### Flujo básico — Crear trimestre académico

1. El Administrador abre "Trimestres académicos".
2. El sistema muestra la lista de trimestres con su estado.
3. El Administrador hace click en "Crear trimestre".
4. El sistema muestra el formulario: nombre, fecha inicio y fecha fin.
5. El Administrador completa el formulario y guarda.
6. El sistema valida que: el nombre esté completo; la fecha inicio sea anterior a la fecha fin; la fecha inicio sea desde mañana; las fechas no se solapen con otro trimestre.
7. El sistema crea el trimestre y calcula su estado (Futuro si la fecha inicio es posterior a hoy).
8. El sistema muestra "Trimestre creado exitosamente".

#### Flujos alternativos

- **A1 — Modificar trimestre:** el Administrador selecciona un trimestre y edita solo los campos habilitados según su estado (ver tabla). El sistema valida con las mismas reglas y actualiza.
- **A2 — Consultar trimestres:** el Administrador filtra por estado, nombre o rango de fechas; el sistema muestra los resultados paginados (20 registros por página).
- **A3 — Desactivar un trimestre activo:** el Administrador acorta la fecha fin (mínimo hoy). El trimestre se cierra al terminar ese día.
- **A4 — Eliminar trimestre:** el Administrador intenta eliminar un trimestre; si no tiene horarios ni asistencias, se elimina definitivamente tras confirmación.

#### Excepciones

- **E1 — Solapamiento de fechas:** el sistema muestra "Las fechas se solapan con el trimestre [nombre]" y no guarda.
- **E2 — Fechas inválidas:** si la fecha inicio no es anterior a la fin, o la fecha inicio no es desde mañana (al crear), el sistema muestra el mensaje correspondiente y no guarda.
- **E3 — Intento de modificar un trimestre cerrado:** el sistema muestra "No se puede modificar un trimestre cerrado" y no permite el cambio.
- **E4 — Cambiar la fecha inicio de un trimestre con asistencias:** el sistema bloquea el cambio y muestra "No se puede modificar la fecha de inicio de un trimestre que ya tiene asistencia registrada".
- **E5 — Fecha fin anterior a hoy en un trimestre activo:** el sistema muestra "La fecha fin no puede ser anterior a la fecha actual" y no guarda.
- **E6 — Eliminar un trimestre con datos:** si el trimestre tiene horarios o asistencias asociados, el sistema muestra "No es posible eliminar el trimestre: tiene horarios o asistencias registradas" y no elimina.

#### Postcondiciones

El trimestre queda creado, modificado, cerrado (por paso de fecha) o eliminado según la operación. Su estado sirve como referencia temporal para el registro de asistencia (UC009), el agendamiento de horarios (UC015) y la evaluación de alertas (UC013).


## Módulo: Fichas y materias

### UC007 — Gestionar fichas

**Actor:** Administrador

**Descripción:** Una ficha es un grupo de aprendices que cursa un Programa de aprendizaje. Permite al Administrador organizar los grupos y controlar el ciclo de vida de cada uno: crearla, modificarla, aplazarla, cancelarla o eliminarla. Toda ficha vive en uno de estos estados:

- **Pendiente:** la ficha fue creada pero aún no empieza (la fecha de inicio es futura).
- **Activa:** la ficha está en curso (hoy está dentro de su rango de fechas).
- **Finalizada:** la ficha terminó (la fecha de fin ya pasó). Queda como histórico.
- **Aplazada:** el Administrador puso la ficha en pausa temporal; luego puede reanudarse.
- **Cancelada:** el Administrador retiró la ficha de operación de forma definitiva.

Los tres primeros estados los determina el sistema automáticamente según las fechas; Aplazada y Cancelada son decisiones manuales del Administrador. La asignación de instructores y la creación de materias (class sections) se gestionan en otros casos de uso (UC015) y quedan fuera de este.

#### Reglas por estado

| Estado | ¿Cómo se determina? | ¿Qué se puede modificar? | ¿Se pueden agregar materias? | ¿Se puede registrar asistencia? |
|---|---|---|---|---|
| Pendiente | Automático (la fecha de inicio es futura) | Todos los campos | Sí, para preparar la ficha | No |
| Activa | Automático (hoy está dentro del rango de fechas) | Fecha fin, programa, código* | Sí | Sí |
| Finalizada | Automático (la fecha de fin ya pasó) | Nada | No | No |
| Aplazada | Manual (decisión del Administrador) | Solo fecha fin | No | No |
| Cancelada | Manual (decisión del Administrador) | Todos los campos | No | No |

\* El código de la ficha solo puede cambiarse mientras la ficha no tenga materias ni aprendices asociados.

#### Precondiciones

- El Administrador tiene una sesión activa (UC002).
- Para crear una ficha debe existir al menos un Programa activo (UC012), una Jornada activa (UC020) y una Modalidad activa (UC021).

#### Flujo básico — Crear ficha

1. El Administrador hace click en "Crear ficha".
2. El sistema muestra el formulario con los campos: código, fecha inicio, fecha fin, programa, jornada y modalidad.
3. El Administrador completa el formulario.
4. El Administrador hace click en "Crear".
5. El sistema valida que: todos los campos estén completos; el código contenga solo números; el código no esté en uso por otra ficha; la fecha fin no sea anterior a la fecha inicio; la fecha inicio no sea anterior al día de hoy; el programa, la jornada y la modalidad seleccionados sigan activos.
6. El sistema crea la ficha y le asigna su estado inicial: Activa si arranca hoy (o ya arrancó) y Pendiente si arranca a futuro.
7. El sistema muestra el mensaje "Ficha creada" y la tarjeta de la nueva ficha en el listado.

#### Flujos alternativos

- **A1 — Modificar ficha:** El Administrador selecciona una ficha y hace click en "Modificar". El sistema muestra el formulario con solo los campos que el estado permite editar (ver tabla de reglas). El Administrador ajusta la información y confirma; el sistema valida con las mismas reglas de creación —incluido que el código nuevo no esté en uso— y guarda los cambios.
- **A2 — Aplazar ficha:** El Administrador selecciona una ficha Pendiente o Activa y hace click en "Aplazar". El sistema pide confirmación y la marca como Aplazada: mientras esté aplazada no se pueden agregar materias ni registrar asistencia. El botón "Reanudar" le devuelve a la ficha su estado según las fechas.
- **A3 — Cancelar ficha:** El Administrador selecciona una ficha que no esté Cancelada y hace click en "Cancelar". El sistema advierte que la acción es definitiva: la ficha no podrá reactivarse y, si tiene aprendices o asistencias, tampoco podrá eliminarse. Al confirmar, la ficha queda Cancelada: bloqueada para toda operación y disponible solo para consulta.

#### Flujo — Eliminar ficha

1. El Administrador selecciona una ficha y hace click en "Eliminar".
2. El sistema verifica si la ficha tiene aprendices vinculados o registros de asistencia.
   - **Si los tiene:** muestra el mensaje "No es posible eliminar la ficha: tiene aprendices vinculados y/o registros de asistencia. Si desea retirarla de operación, use Cancelar ficha" y termina el caso de uso.
   - **Si no los tiene:** muestra el mensaje de confirmación "¿Está seguro de eliminar esta ficha?".
3. El Administrador confirma.
4. El sistema elimina la ficha de forma definitiva —la ficha desaparece del sistema junto con toda su información, ya que por regla solo puede eliminarse cuando no tiene datos asociados— y muestra el mensaje "Eliminaste la ficha".

#### Excepciones

- **E1 — Código duplicado:** si el código ingresado ya lo usa otra ficha (al crear o al modificar), el sistema muestra "El código de ficha ya está en uso" y no guarda.
- **E2 — Sin permisos:** cualquier usuario sin permisos de Administrador que intente crear, modificar, aplazar, cancelar o eliminar una ficha recibe el mensaje "No tienes permisos para esta acción" y la operación no se ejecuta.
- **E3 — Programa, jornada o modalidad desactivado al enviar:** si alguno de estos elementos fue desactivado entre que se cargó el formulario y el envío, el sistema muestra "[Elemento] ya no está disponible, selecciona otro" y actualiza la lista con las opciones activas.
- **E4 — Datos inválidos:** el sistema muestra "Datos inválidos" señalando el campo con problema y no guarda hasta que se corrija.
- **E5 — Ficha inexistente:** si la ficha desapareció del sistema entre el listado y la acción, el sistema muestra "Ficha no encontrada" y refresca el listado.

#### Postcondiciones

La ficha queda creada, modificada, aplazada, cancelada o eliminada según la operación realizada. Una ficha con aprendices o asistencias nunca se elimina: su salida de operación es la cancelación. Los cambios de estado que hace el Administrador quedan registrados en la auditoría del sistema.

### UC015 — Gestionar materias

**Actor:** Administrador

**Descripción:** Una materia (también llamada competencia o class section) es la unidad de clase que se dicta dentro de una ficha. Permite al Administrador crear, modificar, asignar instructor y eliminar materias de una ficha, así como administrar sus horarios, que se definen por trimestre. Toda materia nace vinculada a una única ficha y no puede moverse a otra. Las excepciones festivas/no lectivas las marca el instructor de la materia en su flujo de asistencia (UC009); no forman parte de este caso de uso.

#### Reglas de la materia y sus horarios

- Solo se pueden crear y modificar materias en fichas **Pendiente** o **Activa**. En fichas Aplazada, Cancelada o Finalizada solo se consultan (UC007 define los estados).
- El nombre de la materia es **único dentro de la ficha**.
- Una materia puede crearse **sin instructor**; el instructor se asigna (o cambia) después.
- Los horarios son **por trimestre**: cada horario es un día de la semana con hora de inicio y hora de fin. Un trimestre cerrado congela sus horarios: no se pueden crear ni modificar (solo los de trimestres activos o futuros).
- Todo horario debe:
  - Caer **dentro de la jornada** (TimeSlot) de la ficha (ej. Diurna 6:00–18:00, Nocturna 18:00–22:00). Las materias no pueden salirse de la disponibilidad del grupo.
  - Iniciar y terminar **el mismo día** (hora inicio menor que hora fin; sin sesiones que crucen la medianoche).
  - **No solaparse** con ningún otro horario del mismo trimestre dentro de la ficha —ni de esta materia ni de las otras—, porque el grupo de aprendices no puede estar en dos materias a la vez.
- **Eliminar materia:** se elimina definitivamente **solo si no tiene registros de asistencia**; al eliminarse se borran en cascada sus horarios y excepciones. Si tiene asistencias, no se puede eliminar: se **desactiva** para conservar la auditoría y los registros.

#### Precondiciones

- El Administrador tiene una sesión activa (UC002).
- Para crear una materia: existe una ficha en estado Pendiente o Activa.
- Para definir horarios: existe el trimestre correspondiente (UC014) en estado Activo o aún no iniciado.
- Para asignar instructor: existe el instructor y su cuenta está activa (UC006).

#### Flujo básico — Crear materia

1. El Administrador abre la ficha y hace click en "Agregar materia".
2. El sistema muestra el formulario: nombre de la materia, instructor (opcional) y horarios por trimestre (día, hora inicio, hora fin).
3. El Administrador completa el formulario y confirma.
4. El sistema valida que: el nombre sea único dentro de la ficha; si se asigna instructor, exista y esté activo; cada horario esté dentro de la jornada de la ficha, empiece y termine el mismo día y no se solape con otros horarios de la ficha en ese trimestre.
5. El sistema crea la materia vinculada a la ficha y muestra el mensaje "Materia creada".

#### Flujos alternativos

- **A1 — Modificar materia:** El Administrador selecciona una materia y hace click en "Modificar". El sistema muestra el formulario con el nombre y el instructor actuales, y permite editarlos; desde esta misma vista puede editar los horarios de la materia (ver A2). El sistema valida con las mismas reglas de creación y guarda los cambios.
- **A2 — Gestionar horarios de la materia:** El Administrador agrega, edita o elimina horarios de un trimestre de la materia (día, hora inicio, hora fin). El sistema valida que cada horario esté dentro de la jornada de la ficha, empiece y termine el mismo día, y no se solape con ningún otro horario de la ficha en ese trimestre. Los horarios de un trimestre cerrado no se pueden modificar.
- **A3 — Eliminar materia:** El Administrador selecciona una materia y hace click en "Eliminar".
  - Si la materia **no tiene asistencias**: el sistema pide confirmación ("¿Está seguro de eliminar esta materia?") y, al confirmar, la elimina definitivamente junto con sus horarios y excepciones.
  - Si la materia **tiene asistencias**: el sistema muestra "No es posible eliminar la materia: tiene registros de asistencia. Puede desactivarla para retirarla de operación" y ofrece la acción de desactivar.
- **A4 — Desactivar / reactivar materia:** El Administrador desactiva una materia: deja de estar disponible para nuevas asistencias, pero su historial permanece consultable. Puede reactivarla mientras la ficha siga en estado Pendiente o Activa.

#### Excepciones

- **E1 — Ficha no operativa:** si la ficha está Aplazada, Cancelada o Finalizada, el sistema muestra "No se pueden crear ni modificar materias en una ficha [estado]" y no ejecuta la operación.
- **E2 — Nombre duplicado:** si ya existe una materia con ese nombre en la ficha, el sistema muestra "Ya existe una materia con este nombre en esta ficha" y no guarda.
- **E3 — Horario fuera de la jornada:** si el horario se sale del rango de la jornada de la ficha, el sistema muestra "El horario debe estar dentro de la jornada [nombre] de la ficha" y no guarda.
- **E4 — Horario solapado:** si el horario se cruza con otro de la misma ficha en ese trimestre (propio o de otra materia), el sistema muestra "El horario se solapa con [materia]: [días] de [hora inicio] a [hora fin]" y no guarda.
- **E5 — Sesión que cruza medianoche:** si la hora de fin es anterior o igual a la de inicio, el sistema muestra "La sesión debe iniciar y terminar el mismo día" y no guarda.
- **E6 — Horario de trimestre cerrado:** el sistema muestra "No se pueden modificar los horarios: el trimestre [nombre] ya fue cerrado" y no guarda.
- **E7 — Instructor no disponible:** si el instructor asignado fue desactivado entre que se cargó el formulario y el envío, el sistema muestra "El instructor seleccionado ya no está disponible, selecciona otro" y refresca la lista de instructores activos.
- **E8 — Datos inválidos:** el sistema muestra "Datos inválidos" señalando el campo con problema y no guarda hasta corregir.

#### Postcondiciones

La materia queda creada, modificada, desactivada o eliminada según la operación. Sus horarios quedan asociados por trimestre y siempre dentro de la jornada de la ficha. El instructor asignado pasa a ver la ficha en "Mis fichas y materias" (UC017). Una materia con asistencias nunca se elimina: se desactiva y conserva su historial completo.


## Módulo: Aprendices e instructor

### UC008 — Gestionar aprendices

**Actor:** Administrador

**Descripción:** Permite vincular y desvincular aprendices de una ficha. Los aprendices se autorregistran en el sistema (UC001): el Administrador no los crea. Cada vínculo aprendiz–ficha tiene un **estado académico** —al vincular, el aprendiz queda **Matriculado**— y un aprendiz puede estar vinculado a varias fichas a la vez, sin límite.

#### Reglas del vínculo aprendiz–ficha

- Solo se pueden vincular y desvincular aprendices en fichas **Pendiente** o **Activa** (estados definidos en UC007).
- **Vincular** crea el registro del aprendiz en la ficha con estado académico Matriculado.
- **Desvincular:** el Administrador elige el motivo (**Retiro voluntario**, **Aplazado** o **Cancelado**):
  - Si el aprendiz **no tiene asistencias** en esa ficha, el registro se elimina definitivamente.
  - Si el aprendiz **tiene asistencias**, el registro no se elimina: su estado académico pasa al motivo elegido, deja de contar como aprendiz activo en la ficha y el historial de asistencia se conserva.
- **Vincular de nuevo:** un aprendiz desvinculado de una ficha **no puede volver a esa misma ficha**, sin importar el motivo de desvinculación. Solo puede vincularse a otras fichas.

#### Precondiciones

- El Administrador tiene una sesión activa (UC002).
- El aprendiz a vincular existe (se autorregistró, UC001) y su cuenta está activa.
- La ficha está en estado Pendiente o Activa.

#### Flujo básico — Vincular aprendiz

1. El Administrador abre la ficha y hace click en "Gestionar aprendices".
2. El sistema muestra la lista de aprendices de la ficha —documento, nombre y estado académico de cada uno— y el botón "Añadir aprendiz".
3. El Administrador hace click en "Añadir aprendiz" e ingresa el número de documento del aprendiz.
4. El sistema valida que el aprendiz exista, que su cuenta esté activa y que no esté ya matriculado en esta ficha.
5. El sistema agrega al aprendiz con estado Matriculado y muestra el mensaje "Aprendiz agregado".

#### Flujos alternativos

- **A1 — Desvincular aprendiz:** El Administrador selecciona un aprendiz de la ficha y hace click en "Desvincular". El sistema pide elegir el motivo (Retiro voluntario / Aplazado / Cancelado) y confirma la acción.
  - Si el aprendiz no tiene asistencias en la ficha, el sistema elimina el registro y muestra "Aprendiz desvinculado".
  - Si el aprendiz tiene asistencias, el sistema actualiza su estado académico al motivo elegido, lo deja de contar como activo en la ficha y muestra "Aprendiz desvinculado. Se conservó su historial de asistencia".
- **A2 — Consultar aprendices de la ficha:** el Administrador puede filtrar la lista por documento, nombre o estado académico; el sistema muestra los resultados paginados.

#### Excepciones

- **E1 — Aprendiz no existe o está inactivo:** el sistema muestra "Aprendiz no existe o no está activo" y no realiza la vinculación.
- **E2 — Aprendiz ya vinculado a la ficha:** si el aprendiz ya tiene un registro en la ficha (matriculado o desvinculado con cualquier motivo), el sistema muestra "Este aprendiz ya tiene un registro en esta ficha" y no lo vincula: no puede volver a la misma ficha.
- **E3 — Ficha no operativa:** si la ficha está Aplazada, Cancelada o Finalizada, el sistema muestra "No se pueden vincular ni desvincular aprendices en una ficha [estado]" y no ejecuta la operación.
- **E4 — Datos inválidos:** si el documento ingresado no tiene formato válido, el sistema muestra "Datos inválidos" en el campo correspondiente y no continúa.

#### Postcondiciones

La lista de aprendices de la ficha queda actualizada con el aprendiz vinculado o desvinculado. Ninguna desvinculación pierde el historial: los registros de asistencia existentes se conservan siempre para trazabilidad y auditoría.

### UC017 — Consultar mis fichas y materias

**Actor:** Instructor

**Descripción:** Permite al Instructor ver, a partir de las materias (class sections) donde está asignado, las fichas de las que forma parte y las materias que imparte en cada una. El instructor no se "une" a una ficha: su vinculación se establece a través de esas asignaciones. Es un caso de uso de solo lectura: no crea ni modifica información.

#### Precondiciones

- El Instructor tiene una sesión activa (UC002).
- El Instructor tiene al menos una materia asignada. Esa asignación la realiza el Administrador en otro caso de uso, fuera de este.

#### Flujo básico

1. El Instructor abre la pantalla "Mis fichas".
2. El sistema muestra las fichas de las que forma parte según sus materias asignadas, con: código de ficha, programa, estado actual, fechas y las materias que dicta en cada una.
3. El Instructor puede abrir una ficha para ver el detalle de sus materias dentro de esa ficha.

#### Flujo alternativo — Buscar ficha

1. El Instructor escribe el número de ficha en el buscador.
2. El sistema busca solo entre sus propias fichas; no muestra fichas donde el instructor no esté asignado.

#### Excepciones

- **E1 — Sin resultados:** el sistema muestra "No se encontraron fichas con ese número".
- **E2 — Ficha no operativa:** una ficha Aplazada, Cancelada o Finalizada se muestra con su estado correspondiente, sin acciones operativas (sobre ella no se registra asistencia).
- **E3 — Sin asignaciones:** si el instructor aún no tiene materias asignadas, el sistema muestra "Aún no tienes materias asignadas. Contacta al Administrador."

#### Postcondiciones

Ninguna: el caso de uso es de solo lectura y no produce cambios en el sistema.


## Módulo: Asistencia

### UC009 — Gestionar listas de asistencia

**Actor:** Instructor

**Descripción:** Permite registrar, consultar y modificar la asistencia de los aprendices de una ficha, por materia (class section) y fecha de sesión. Cada instructor toma asistencia únicamente de las materias que le fueron asignadas. La asistencia solo se registra mientras el trimestre vigente esté Activo y la ficha en estado Activa; en el momento en que el trimestre pasa a Cerrado, la edición queda bloqueada de inmediato, sin ventanas de gracia.

#### Reglas de la asistencia

- La asistencia se registra por **materia** y **fecha de sesión**, para los aprendices **Matriculados** de la ficha.
- Solo el **instructor asignado a la materia** registra y modifica su asistencia. Ningún otro instructor toca esa lista.
- Existen tres estados: **A (Asistió)**, **F (Falla)** y **J (Justificada)**. Un registro nace como A o F; F cambia a J únicamente cuando el instructor aprueba la justificación correspondiente (UC010). El instructor nunca marca J al tomar asistencia.
- La lista se presenta con todos los aprendices en **Asistió (A)** por defecto; el Instructor cambia a **Falla (F)** solo quienes no asistieron. Cada marcación se guarda al instante. Al guardar la sesión, **todos los aprendices de la lista quedan con un estado explícito**: los que quedaron sin marcar se guardan como Asistió. Solo si la sesión se guardó sin completar la marcación (o se interrumpe la conexión), los aprendices no confirmados quedan **sin registro** ese día y la sesión se muestra como **incompleta** (dato derivado, no un estado del registro).
- La fecha de sesión debe ser válida en tres sentidos: estar dentro del **trimestre vigente activo**, estar **dentro del rango de fechas de la ficha**, y no ser una fecha futura ni una fecha marcada como excepción no lectiva para esa materia.
- Mientras el trimestre esté Activo, el instructor puede modificar cualquier registro de asistencia de su materia de ese trimestre; cada cambio queda registrado en auditoría (usuario, fecha/hora, valor anterior, valor nuevo).
- El horario programado de la materia orienta qué fechas mostrar al instructor, pero no limita en qué fecha se puede registrar asistencia (permite clases de recuperación).
- **Excepción no lectiva:** el instructor marca una fecha para su materia con un motivo (calamidad, día festivo, etc.), dejando precedente. Ese día no se exige asistencia para esa materia y no cuenta en los conteos de fallas ni en las alertas (UC013). Mientras la fecha no haya pasado, la excepción se puede eliminar o modificar; si la fecha ya pasó, solo se puede modificar el motivo — el precedente no se elimina.

#### Precondiciones

- El Instructor tiene una sesión activa (UC002).
- El Instructor está asignado a la materia y su cuenta está activa.
- La ficha de la materia está en estado Activa (UC007).
- Existe un trimestre vigente (UC014).
- La ficha tiene al menos un aprendiz con estado Matriculado (UC008).

#### Flujo básico — Registrar asistencia

1. El Instructor abre una de sus materias y selecciona la fecha de la sesión.
2. El sistema muestra la lista de aprendices matriculados de la ficha, con su estado actual si ya fueron marcados para esa sesión.
3. El sistema presenta la lista con todos los aprendices marcados por defecto como **A (Asistió)**; el Instructor solo cambia a **F (Falla)** a quienes no asistieron.
4. El sistema guarda cada marcación al instante (zona horaria de Colombia, UTC-5).
5. El sistema muestra el mensaje "Asistencia guardada".

#### Flujos alternativos

- **A1 — Consultar historial de asistencia:** el Instructor filtra por materia, fecha, aprendiz o estado, dentro de sus materias asignadas. El sistema muestra el historial paginado (20 registros por página por defecto, sin límite de páginas).
- **A2 — Modificar asistencia:** mientras el trimestre esté Activo, el Instructor selecciona cualquier registro de asistencia de su materia de ese trimestre y lo edita (A o F). El sistema guarda el cambio, registra la modificación en auditoría (usuario, fecha/hora, valor anterior, valor nuevo) y muestra "Asistencia actualizada".
- **A3 — Bloqueo por cierre de trimestre:** en el momento en que el trimestre pasa a Cerrado —automáticamente al llegar su fecha de fin (UC014)— el sistema bloquea de inmediato cualquier edición de asistencia de ese trimestre, sin excepciones.
- **A4 — Registrar excepción no lectiva:** el Instructor marca una fecha para su materia con un motivo (festivo, calamidad, día no lectivo). El sistema excluye esa fecha de la exigencia de asistencia de esa materia, de los conteos de fallas y de las alertas (UC013). Mientras la fecha no haya pasado, puede eliminarla o modificarla; si ya pasó, solo puede modificar el motivo.
- **A5 — Guardar una sesión incompleta:** si el Instructor guarda la sesión sin haber completado la marcación, o la conexión se interrumpe a mitad de camino, el sistema conserva los estados ya guardados; los aprendices cuyas marcaciones no quedaron confirmadas permanecen **sin registro** para esa fecha y la sesión se muestra como incompleta. Al completar la marcación y guardar, la sesión pasa a estar completa y todos los aprendices quedan con estado explícito (los no marcados, como Asistió).

#### Excepciones

- **E1 — Intento de modificar asistencia de un trimestre cerrado:** el sistema muestra "No se puede modificar: el trimestre ya fue cerrado" y no permite el cambio, sin excepciones.
- **E2 — Fecha fuera del trimestre vigente o fuera del rango de la ficha:** el sistema muestra "La fecha seleccionada está fuera del trimestre vigente" o "La fecha está fuera del rango de fechas de la ficha", según aplique.
- **E3 — Intento de registrar asistencia en fecha futura:** el sistema muestra "No puedes registrar asistencia para fechas futuras".
- **E4 — Fecha marcada como excepción no lectiva para esa materia:** el sistema muestra "Esta fecha está marcada como no lectiva, no se puede registrar asistencia".
- **E5 — Ficha sin aprendices activos:** el sistema muestra "No hay aprendices activos en esta ficha" en lugar de la lista de asistencia.
- **E6 — Materia sin instructor asignado:** la materia no está disponible para tomar asistencia; el sistema muestra "Esta materia no tiene instructor asignado. Contacta al Administrador".
- **E7 — Pérdida de conexión durante el registro:** el sistema conserva las marcaciones ya guardadas (guardado automático) y solo se pierde la marcación en curso no confirmada.

#### Postcondiciones

Las asistencias quedan registradas o actualizadas con guardado automático, o bloqueadas de inmediato si el trimestre fue cerrado. Cada registro conserva estado (A, F o J), fecha/hora de marcación (zona horaria de Colombia), instructor que registró y materia. Toda modificación queda en auditoría. Ninguna excepción no lectiva pasada se elimina: su precedente queda en el historial.


## Módulo: Justificaciones

### UC011 — Gestionar asistencia (Aprendiz)

**Actor:** Aprendiz

**Descripción:** Permite al Aprendiz consultar su historial de asistencia, justificar inasistencias, hacer seguimiento a sus justificaciones, editarlas o cancelarlas mientras estén pendientes, corregir (subsanar) las partes rechazadas y crear una justificación nueva cuando una subsanación vence. Solo puede justificar fallas de fichas en las que está **Matriculado**.

#### Reglas de las justificaciones del aprendiz

- La justificación se presenta por **período** (rango de fechas) y **materias afectadas** donde el aprendiz tenga fallas (**F**). Por cada materia afectada se genera una parte independiente que decide su instructor (UC010).
- El formulario contiene: **tipo de justificación** (motivo del catálogo, UC016), descripción, rango de fechas (si aplica), materias afectadas y soporte (PDF o imagen, máximo 5 MB).
- **Plazo:** los días hábiles configurados (UC019), contados desde el día hábil siguiente a la **última** falla del período. Si envía dentro del plazo, la justificación queda "en tiempo"; si envía después, queda "fuera de tiempo" (la marca no bloquea el envío, pero condiciona la aprobación en UC010). Los plazos no dependen de la jornada de la ficha.
- **Cupo por tipo:** cada tipo de justificación tiene un límite de días justificables por trimestre. Para el cupo cuentan los **días con fallas** (fechas distintas, sin importar cuántas materias tengan ese día) cubiertos por justificaciones del mismo tipo en estado **pendiente o aprobado**. Una fecha ya cubierta no vuelve a contar. Las justificaciones rechazadas liberan el cupo. Si el rango supera el cupo restante, el envío se bloquea y el sistema muestra cuántos días le quedan disponibles.
- No se pueden justificar fallas de **trimestres cerrados**.
- **Edición:** mientras la justificación esté pendiente (sin decisión), el aprendiz puede editar rango, materias, tipo y soporte. Al editar se recalcula la marca de plazo y se revalida el cupo del tipo.
- **Cancelación:** puede cancelar una justificación pendiente; el cupo reservado se libera.
- **Subsanación:** si una parte (materia) es rechazada, el aprendiz dispone de **2 días hábiles desde el rechazo** para corregir **solo esa parte** (texto y/o archivo de corrección). Pasado el plazo, esa parte queda rechazada definitiva y el único camino es crear una justificación nueva para las mismas fechas.
- **Notificaciones:** recibe una notificación por cada cambio de estado de sus justificaciones, una sola vez por cambio.

#### Precondiciones

- El Aprendiz tiene una sesión activa (UC002).
- El Aprendiz está Matriculado en al menos una ficha y tiene fallas (F) justificables.

#### Flujo básico — Justificar inasistencia

1. El Aprendiz consulta su historial de asistencia (por ficha y materia) y selecciona las fallas que quiere justificar.
2. El sistema muestra el formulario con: tipo de justificación, descripción, rango de fechas (precargado con las fechas de las fallas seleccionadas), materias afectadas y soporte.
3. El Aprendiz completa los datos y adjunta el soporte.
4. El Aprendiz hace click en "Enviar".
5. El sistema valida: campos completos; formato y tamaño del soporte; que las fallas pertenezcan a fichas donde está Matriculado; que no sean de trimestres cerrados; y el cupo del tipo (días con fallas, sin duplicar fechas ya cubiertas).
6. El sistema clasifica el envío según el plazo (en tiempo o fuera de tiempo, desde la última falla del período) y crea la justificación con una parte pendiente por cada materia afectada.
7. El sistema muestra el mensaje de éxito y la justificación queda en estado "pendiente".

#### Flujos alternativos

- **A1 — Ver estado de mis justificaciones:** el Aprendiz consulta el listado con período, materias, tipo, descripción, marca de plazo y el estado de cada parte.
- **A2 — Recibir notificación:** ante cada cambio de estado (aprobada o rechazada), el sistema notifica al Aprendiz una sola vez por cambio.
- **A3 — Editar justificación pendiente:** el Aprendiz modifica rango, materias, tipo, descripción o soporte de una justificación sin decisión; el sistema recalcula plazo y revalida cupo antes de guardar.
- **A4 — Cancelar justificación pendiente:** el Aprendiz cancela una justificación sin decisión; el sistema la marca como cancelada y libera el cupo reservado.
- **A5 — Subsanar parte rechazada:** el Aprendiz abre la parte rechazada y la corrige (texto y/o nuevo archivo) dentro de los 2 días hábiles desde el rechazo. Esa parte vuelve a quedar pendiente para su instructor, conservando la marca de plazo.
- **A6 — Crear justificación nueva:** si la parte quedó rechazada definitiva (pasó el plazo de subsanación o fue rechazada de nuevo), el Aprendiz puede presentar una justificación nueva para las mismas fechas y materia; esa nueva justificación consume cupo del tipo.

#### Excepciones

- **E1 — Formato o tamaño de archivo no válido:** si el soporte no es PDF/imagen o excede 5 MB, el sistema muestra "Formato o tamaño de archivo no válido" y no envía.
- **E2 — Campos incompletos:** el sistema muestra "Completa todos los campos requeridos" y no permite el envío.
- **E3 — Justificación ya procesada:** si la justificación ya tiene decisión, el sistema bloquea edición y cancelación con "Esta justificación ya fue procesada y no puede modificarse".
- **E4 — Sin fallas para justificar:** si no hay fallas seleccionadas o la falla no existe, el sistema muestra "No hay fallas para justificar" y no crea el registro.
- **E5 — Subsanación fuera de plazo:** el sistema muestra "El plazo para subsanar esta justificación ha vencido" y no permite corregirla; solo queda crear una nueva.
- **E6 — Cupo del tipo agotado:** si los días del rango superan el cupo restante, el sistema muestra "Ya alcanzaste el límite de días para este tipo de justificación en el trimestre. Te quedan [N] días disponibles" y no envía.
- **E7 — Trimestre cerrado:** si la falla pertenece a un trimestre cerrado, el sistema muestra "No puedes justificar fallas de un trimestre cerrado".
- **E8 — Ficha no matriculada:** si la falla pertenece a una ficha donde el aprendiz no está Matriculado, el sistema muestra "Solo puedes justificar fallas de fichas en las que estás matriculado".

#### Postcondiciones

La justificación queda creada, editada, cancelada o subsanada según la operación, con una parte por materia afectada. El histórico de cada parte conserva: marca de plazo, motivo, descripción, soporte, decisiones del instructor, motivos de rechazo y correcciones, todo para trazabilidad y auditoría.

### UC010 — Gestionar justificaciones

**Actor:** Instructor

**Descripción:** Permite revisar, aprobar o rechazar las justificaciones de inasistencia que envían los aprendices (UC011). Una justificación cubre un **período** (rango de fechas) y una o varias **materias afectadas**; por cada materia se genera una decisión independiente. Cada instructor decide únicamente la parte de las materias que dicta, de modo que una misma justificación puede aprobarse en una materia y rechazarse en otra.

#### Reglas de decisión

- Una justificación se compone de un período (fecha inicio–fecha fin) y un conjunto de materias afectadas. Por cada materia el sistema mantiene una **decisión independiente**.
- Solo el **instructor asignado a la materia** decide su parte. Si el Administrador cambia el instructor de la materia, decide el asignado al momento de la decisión, aunque la falla la haya registrado otro instructor.
- **Aprobar:** todas las fallas (**F**) del aprendiz en esa materia dentro del período pasan a **J (Justificada)**; se notifica al aprendiz y, si la aprobación reduce el conteo de fallas no justificadas por debajo del umbral vigente, la alerta asociada se resuelve automáticamente (UC013).
- **Rechazar:** el sistema exige un motivo de rechazo, lo registra y notifica al aprendiz indicando el motivo.
- **Marca de plazo:** la justificación se clasifica al enviarse como **en tiempo** o **fuera de tiempo**, según los días hábiles configurados (UC019) contados desde el día hábil siguiente a la última falla del período. Esta marca no cambia con la decisión: una justificación puede estar "aprobada en una materia y fuera de tiempo". Para aprobar la parte de una justificación fuera de tiempo, el instructor debe registrar un **motivo adicional** de la excepción.
- **Plazo de respuesta del instructor:** el Instructor dispone de los días configurados (`instructorResponseDays`, UC019) para decidir una justificación. Si no decide dentro de ese plazo, el sistema le muestra un recordatorio y, cuando finalmente responda, la decisión queda marcada como **demorada** en la auditoría. No hay escalado ni decisión automática.
- **Cierre del trimestre:** el cierre impide los cambios **manuales** de asistencia, pero no la decisión de justificaciones pendientes: las que estaban pendientes al cierre pueden seguir decidiéndose y la conversión F→J se aplica con auditoría. No se pueden crear justificaciones para fallas de trimestres ya cerrados.
- **Subsanación:** si el aprendiz subsana la parte rechazada de una materia (UC011), esa parte vuelve a "pendiente" y reinicia la decisión, conservando la marca de plazo. Si no subsana dentro de los 2 días hábiles desde el rechazo, esa parte queda **Rechazada definitiva** y el aprendiz puede crear una justificación nueva para las mismas fechas y materia.
- Si el aprendiz fue desvinculado de la ficha con una justificación pendiente, la decisión se aplica igualmente sobre el historial conservado.

#### Precondiciones

- El Instructor tiene una sesión activa (UC002).
- Existe al menos una justificación pendiente en una materia asignada al Instructor.

#### Flujo básico — Decidir sobre una justificación

1. El sistema notifica al Instructor que recibió una nueva justificación para una de sus materias.
2. El Instructor accede al módulo de gestión de justificaciones.
3. El sistema muestra la lista de las partes pendientes de sus materias con los valores: **nombre del aprendiz, ficha, fecha(s) de ausencia y motivo**. La lista se ordena por fecha de solicitud (la más reciente primero); la fecha de solicitud queda visible en el detalle.
4. El Instructor selecciona una y el sistema muestra el detalle: período justificado, materias afectadas, fechas con falla de su materia, motivo, descripción y soporte adjunto.
5. El Instructor aprueba o rechaza **su materia**:
   - **Aprobar:** las fallas F de su materia dentro del período pasan a J, se notifica al aprendiz y, si corresponde, se resuelve automáticamente la alerta asociada (UC013).
   - **Rechazar:** el sistema solicita el motivo, lo registra, y notifica al aprendiz indicando el motivo.

#### Flujos alternativos

- **A1 — Consultar histórico de decisiones:** el Instructor consulta todas las decisiones de sus materias en orden cronológico, con filtros por **ficha, estado y rango de fechas (desde–hasta) sobre la fecha de solicitud**; el sistema muestra los resultados paginados.
- **A2 — Aprobar una parte fuera de tiempo:** para una justificación fuera de tiempo, el sistema exige registrar el motivo adicional de la excepción antes de habilitar el botón de aprobar. El motivo queda guardado junto con la decisión.
- **A3 — Recibir una parte subsanada:** cuando el aprendiz corrige una parte rechazada dentro de los 2 días hábiles, esa parte vuelve a "pendiente" conservando su marca de plazo y el sistema reinicia la decisión.

#### Excepciones

- **E1 — Intento de rechazar sin motivo:** el sistema exige el campo de motivo de rechazo; si está vacío, no permite confirmar la acción.
- **E2 — La parte ya no está pendiente:** si entre que el Instructor abrió la justificación y tomó la decisión, el aprendiz la editó o la canceló, el sistema muestra "Esta justificación ya no está pendiente" y no aplica la decisión.
- **E3 — Soporte adjunto no disponible o corrupto:** si el archivo no puede visualizarse, el sistema muestra "El archivo adjunto no está disponible" y permite al Instructor rechazar con el motivo "soporte no legible".
- **E4 — Instructor no autorizado:** si quien intenta decidir no es el instructor asignado a la materia, el sistema muestra "Esta justificación pertenece a la materia de otro instructor" y no aplica la decisión.

#### Postcondiciones

Cada parte (materia) queda en estado final (Aprobada, Rechazada o Cancelada) y las fallas aprobadas pasan de F a J dentro del período justificado. El aprendiz recibe una notificación por cada cambio de estado. Toda decisión queda registrada con usuario, fecha/hora y motivo (incluidos los motivos adicionales de las aprobaciones fuera de tiempo) para trazabilidad y auditoría.


## Módulo: Alertas y notificaciones

### UC013 — Gestionar alertas de inasistencia

**Actor:** Sistema (genera y resuelve) / Instructor (consulta y atiende) / Aprendiz (recibe el aviso)

**Descripción:** El sistema evalúa automáticamente la asistencia de los aprendices y genera alertas cuando superan los umbrales configurados, para apoyar el seguimiento de permanencia. Existen dos tipos de alerta: por **fallas consecutivas** (por materia) y por **fallas acumuladas** (por ficha). Los umbrales y plazos se configuran en UC019 (Configuración global). Una alerta es un hecho de negocio con estado propio; su aviso al usuario se realiza mediante notificaciones (UC018).

#### Reglas de generación

- **Alerta por fallas consecutivas (por materia):** se genera cuando el aprendiz acumula el umbral de fallas consecutivas (default 3) en las sesiones de una misma materia dentro del trimestre. La racha se mide sobre las sesiones programadas de esa materia: una asistencia (A) o una falla justificada (J) cortan la racha; los días sin clase (excepción no lectiva) no cortan ni suman.
- **Alerta por fallas acumuladas (por ficha):** se genera cuando el total de fallas (F) del aprendiz en todas las materias de la ficha dentro del trimestre alcanza el umbral (default 5). Las fallas justificadas (J) no cuentan.
- La evaluación ocurre cada vez que se registra o modifica una asistencia (UC009) y cada vez que se aprueba una justificación (UC010).
- Puede existir **una sola alerta activa** por combinación: (aprendiz + materia + trimestre + tipo Consecutivas) o (aprendiz + ficha + trimestre + tipo Acumuladas). Si la alerta se resuelve y el aprendiz vuelve a superar el umbral en el mismo trimestre, se genera una **alerta nueva**; la anterior queda como historial.
- Todo se reinicia por trimestre: al comenzar un trimestre nuevo, los conteos arrancan de cero.
- Al generarse una alerta, el sistema notifica (UC018) a los instructores correspondientes —el de la materia, en las consecutivas; los de la ficha, en las acumuladas— y también al aprendiz.
- **Resolución automática:** si una justificación aprobada baja el conteo por debajo del umbral, la alerta activa pasa a "Resuelta automáticamente" y se notifica el cambio.

#### Estados de la alerta

No leída → Leída (cuando el instructor la abre) → Atendida (cuando el instructor registra que hizo seguimiento). Una alerta activa puede pasar a Resuelta automáticamente cuando el conteo baja del umbral.

#### Precondiciones

- Existe un trimestre activo (UC014).
- Existe asistencia registrada (UC009) o justificaciones decididas (UC010).

#### Flujo — Generación y aviso (automático)

1. El sistema evalúa las fallas al guardar una asistencia o al aprobar una justificación.
2. Si se supera un umbral y no existe una alerta activa de esa combinación, el sistema genera la alerta en estado "No leída".
3. El sistema genera las notificaciones a los instructores correspondientes y al aprendiz (UC018).
4. Instructores y aprendiz ven la alerta desde sus notificaciones.

#### Flujos alternativos

- **A1 — Consultar alertas:** el Instructor consulta las alertas de sus materias y fichas; el Administrador consulta todas. Filtros por tipo, estado, ficha, aprendiz y rango de fechas; resultados paginados.
- **A2 — Marcar leída:** al abrir el detalle de la alerta, queda marcada como leída.
- **A3 — Marcar atendida:** el Instructor registra que realizó seguimiento (observación), y la alerta pasa a Atendida con fecha, usuario y observación en auditoría.
- **A4 — Resolución automática:** al aprobarse una justificación que baja el conteo del aprendiz por debajo del umbral, la alerta asociada pasa a Resuelta automáticamente y se notifica el cambio.
- **A5 — Ver historial por aprendiz:** el Instructor o el Administrador consultan todas las alertas de un aprendiz (por ficha y trimestre).

#### Excepciones

- **E1 — Sin duplicados:** si ya existe una alerta **activa** de la misma combinación, el sistema no genera otra.
- **E2 — Aprendiz desvinculado:** si el aprendiz se desvincula de la ficha, no se generan alertas nuevas para esa ficha; las existentes se conservan como historial.
- **E3 — Falla en la entrega de la notificación:** la alerta se registra igualmente; la notificación queda en estado "Reintentar" y el sistema reintenta su entrega (UC018).
- **E4 — Umbral no configurado:** si no hay valor explícito, aplican los defaults (3 consecutivas / 5 acumuladas).

#### Postcondiciones

La alerta queda registrada con su tipo, estado y trazabilidad (aprendiz, materia o ficha, trimestre, conteo al generarse). Instructores y aprendiz quedan notificados. Las atenciones y resoluciones (manuales o automáticas) quedan en auditoría.

### UC018 — Gestionar notificaciones

**Actor:** Usuario (Aprendiz, Instructor o Administrador)

**Descripción:** Permite a cualquier usuario autenticado consultar las notificaciones que el sistema le ha generado y marcar su estado de lectura. Las notificaciones son el canal de aviso de los distintos flujos (credenciales, alertas de inasistencia, decisiones de justificaciones). La bandeja es in-app.

#### Reglas

- Cada notificación pertenece a un usuario y la genera automáticamente el sistema; el usuario no las crea.
- La notificación guarda: **tipo**, **mensaje**, estado de **entrega** (interno: pendiente / enviada / reintentar) y estado de **lectura** (leída / no leída), más una **referencia al objeto de origen** para poder ir a su detalle.
- El estado de entrega y el de lectura son independientes: que una notificación se haya entregado no significa que esté leída.
- El usuario ve únicamente sus notificaciones; la bandeja las ordena de la más reciente a la más antigua y destaca las no leídas.
- Marcar como leída es individual, pero el usuario puede marcar todas como leídas.

#### Precondiciones

- El usuario tiene una sesión activa (UC002).

#### Flujo básico — Consultar notificaciones

1. El usuario abre la pantalla "Notificaciones".
2. El sistema muestra sus notificaciones con: tipo, mensaje, fecha y estado (leída / no leída).
3. El usuario abre una notificación: el sistema la marca como leída y, si tiene referencia, lo lleva al detalle del objeto (alerta, justificación, etc.).
4. El sistema muestra el estado actualizado de la notificación.

#### Flujos alternativos

- **A1 — Marcar todas como leídas:** el usuario marca todas sus notificaciones no leídas de una vez.
- **A2 — Filtrar notificaciones:** por estado (leídas / no leídas), tipo o rango de fechas; resultados paginados.
- **A3 — Recibir una nueva notificación:** al generarse, aparece en la bandeja e incrementa el indicador de no leídas.

#### Excepciones

- **E1 — Sin notificaciones:** el sistema muestra "No tienes notificaciones".
- **E2 — Notificación sin referencia:** se muestra solo el mensaje, sin enlace al detalle.
- **E3 — Error de entrega:** la notificación queda registrada con estado "Reintentar" y el sistema reintenta la entrega; el usuario la ve igualmente en su bandeja.

#### Postcondiciones

La notificación queda leída o no leída según la acción del usuario. El estado de entrega es independiente del de lectura y no condiciona la visibilidad en la bandeja.


## Módulo: Dashboard

### UC023 — Consultar dashboard

**Actor:** Usuario (cada rol ve su propio panel) — Administrador, Instructor o Aprendiz

**Descripción:** Permite a cada usuario ver un **panel de resumen** (dashboard) con la información más relevante de su rol al ingresar al sistema. Es un caso de uso de **solo lectura**: cada rol accede únicamente a sus propios datos.

#### Reglas

- Cada rol ve **su propio panel**; no puede ver el de otro rol ni datos de fichas o materias que no le correspondan.
- La información se calcula al momento de consultar; no se almacena.
- Los indicadores que dependen del trimestre usan el **trimestre activo**; si no hay ninguno, se muestran vacíos con el mensaje "No hay un trimestre activo".

##### Panel de Administrador (ya implementado — no se modifica)

- Total de usuarios, fichas activas, total de programas y total de modalidades.
- Últimas 5 fichas creadas: código, programa, instructor y estado.

##### Panel de Instructor

- **Justificaciones pendientes de decidir** (cantidad), con acceso directo a la bandeja de justificaciones de sus materias (UC010).
- **Alertas activas** de sus fichas y materias (cantidad).
- **Clases de hoy y próximas sesiones**, según los horarios de sus materias y el trimestre activo.
- **Materias asignadas** (cantidad) y **fichas en las que dicta clase** (cantidad).
- **Aprendices a mi cargo**: cantidad de aprendices matriculados en las fichas donde dicta al menos una materia.

##### Panel de Aprendiz

- **Porcentaje de asistencia del trimestre**, con los totales por estado (A, F, J). El porcentaje se calcula sobre las sesiones registradas en el trimestre.
- **Fallas no justificadas por ficha activa** y cuántas le faltan para alcanzar el umbral de alerta.
- **Justificaciones por estado** (pendientes, aprobadas y rechazadas), destacando las que están **dentro del plazo de subsanación**, con los días restantes.
- **Mis fichas y materias**.
- **Próximas clases**, según los horarios de sus materias.
- **Alertas activas** que lo afectan.

#### Precondiciones

- El usuario tiene una sesión activa (UC002).

#### Flujo básico — Consultar el panel

1. El usuario inicia sesión y es dirigido a su panel.
2. El sistema reúne los indicadores correspondientes a su rol.
3. El sistema muestra el panel con los indicadores y, cuando aplica, la lista de últimas fichas, próximas sesiones o justificaciones.
4. El usuario puede navegar desde el panel al detalle de los elementos que lo permitan (ficha, materia, alerta, justificación).

#### Flujos alternativos

- **A1 — Actualizar el panel:** el usuario refresca; el sistema recalcula los indicadores.

#### Excepciones

- **E1 — Sin datos:** si no hay información, el sistema muestra el indicador en cero o un mensaje de estado vacío (ej. "Aún no tienes materias asignadas").
- **E2 — Sin trimestre activo:** los indicadores que dependen del trimestre (asistencia, fallas, umbrales, próximas clases) se muestran vacíos con el mensaje "No hay un trimestre activo".

#### Postcondiciones

Ninguna: el caso de uso es de solo lectura y no produce cambios en el sistema.


## Tabla de dependencias entre casos de uso

Documenta únicamente **dependencias de datos y precondiciones**, no relaciones UML.

| Caso de uso | Dependencias | Actor principal |
|---|---|---|
| UC001 Registrarme | — | Aprendiz |
| UC002 Iniciar sesión | — | Usuario |
| UC003 Modificar datos | Sesión activa (UC002) | Usuario |
| UC004 Cerrar sesión | Sesión activa (UC002) | Usuario |
| UC005 Recuperar contraseña | — | Usuario |
| UC019 Configuración global | Sesión Admin (UC002) | Administrador |
| UC020 Gestionar jornadas | Sesión Admin (UC002) | Administrador |
| UC021 Gestionar modalidades | Sesión Admin (UC002) | Administrador |
| UC022 Gestionar tipos de documento | Sesión Admin (UC002) | Administrador |
| UC016 Tipos de justificación | Sesión Admin (UC002) | Administrador |
| UC006 Gestionar perfiles | Sesión Admin (UC002) | Administrador |
| UC012 Programas | Sesión Admin (UC002) | Administrador |
| UC014 Trimestres | Sesión Admin (UC002) | Administrador |
| UC007 Fichas | Sesión Admin; Programa activo (UC012); Jornada activa (UC020); Modalidad activa (UC021) | Administrador |
| UC015 Materias | Sesión Admin; ficha Pendiente/Activa (UC007); trimestre (UC014); instructor activo (UC006) | Administrador |
| UC008 Aprendices | Sesión Admin; aprendiz existente (UC001); ficha Pendiente/Activa (UC007) | Administrador |
| UC017 Consultar mis fichas y materias | Sesión Instructor; materias asignadas (UC015) | Instructor |
| UC009 Listas de asistencia | Sesión Instructor; materia asignada (UC015); ficha Activa (UC007); trimestre activo (UC014); aprendices matriculados (UC008) | Instructor |
| UC011 Asistencia (Aprendiz) | Sesión Aprendiz; matriculado en ficha (UC008); tipos de justificación (UC016); configuración (UC019); trimestre activo (UC014) | Aprendiz |
| UC010 Justificaciones | Sesión Instructor; justificación pendiente (UC011); configuración (UC019); alertas (UC013) | Instructor |
| UC013 Alertas | Trimestre activo (UC014); asistencia (UC009); justificaciones (UC010); configuración (UC019); notificaciones (UC018) | Sistema / Instructor / Aprendiz |
| UC018 Notificaciones | Sesión activa (UC002) | Usuario |
| UC023 Dashboard | Sesión activa (UC002); datos de fichas, materias, asistencia, justificaciones y alertas | Usuario (según rol) |

## Fuera de alcance de esta versión

- **Reportes** descartados para esta versión.
- **Multi-rol simultáneo:** una cuenta tiene un solo rol; se admite el cambio de rol conservando historia.
- **Invalidación inmediata de sesión al desactivar una cuenta:** la sesión expira con el token (máximo 24 h).
- **Sesiones que cruzan medianoche:** por ahora toda sesión empieza y termina el mismo día.

## Pendientes de implementación (modelo de datos)

Estos cambios acompañan las reglas nuevas y aún no están implementados:

- Entidad **`Alerta`** nueva (dos tipos, estados, trazabilidad) y **descarte** de `DesertionCounter`.
- `Notificacion`: estado de **lectura**, **referencia** al objeto de origen y nuevos **tipos** (alertas, justificaciones).
- `GlobalConfiguration`: **dos umbrales** de alerta (consecutivas y acumuladas).
- `StateJustification`: agregar **CANCELADA** y la marca de plazo separada.
- `StateGrade`: los **cinco estados** (Pendiente, Activa, Finalizada, Aplazada, Cancelada).
- `DocumentType`: agregar **estado** (activo/inactivo).
- Utilidad de **días hábiles** (lunes a viernes) para los plazos.
- Validación del **código numérico** de programa.
- Indicador **`mustChangePassword`**.
- Lógica de negocio de asistencia, justificaciones y alertas (hoy son CRUD sin reglas).
- Cambio obligatorio de contraseña y complejidad completa en todos los flujos.
