# Trabajo pendiente del frontend — SENA Attendance

Este archivo es el **seguimiento vivo del frontend**: describe lo que la interfaz debe hacer y consumir para cumplir cada caso de uso, a medida que el backend avanza. La fuente de verdad del contrato HTTP es [`docs/api-contracts.md`](./api-contracts.md); las reglas de negocio y los flujos están en [`docs/use-cases.md`](./use-cases.md). Aquí no se repite el JSON de los contratos: se referencia su sección.

- **Propietario:** el desarrollador de frontend.
- **Mantenimiento:** se actualiza a medida que el backend avanza; cada UC se agrega cuando su backend está listo. El backend no cambia para acomodar al frontend: el frontend se adapta al contrato.
- **Estado actual:** buena parte del backend está implementado (21 UCs implementadas y 2 parciales —UC004 y UC023—; ver el índice de [`docs/api-contracts.md`](./api-contracts.md)); el frontend sigue, en su mayor parte, con los formularios stock de JHipster.

## Leyenda de estados

| Estado        | Significado                                                   |
| ------------- | ------------------------------------------------------------- |
| `Pendiente`   | No iniciado o sin verificar contra el backend.                |
| `En progreso` | Iniciado, todavía sin cerrar el flujo de extremo a extremo.   |
| `Hecho`       | Implementado y verificado contra el backend y el caso de uso. |

---

## Cómo consumir la API

Estas convenciones aplican a todo el frontend.

| Tema              | Qué debe hacer el frontend                                                                                                                                                                                                                                                                                   |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Autenticación JWT | Enviar `Authorization: Bearer <token>` en cada petición autenticada. El login es por **tipo de documento + número de documento + contraseña**. El token dura **24 horas**; `rememberMe` lo extiende a **30 días**. `rememberMe` es una decisión de UI: **no está contemplado en los UCs**.                   |
| Errores           | La clave de negocio viaja en el cuerpo como `message: error.<clave>` y el nombre de la entidad afectada en `params`. Los errores de validación agregan `fieldErrors` (`objectName`, `field`, `message`). El backend **no** emite cabeceras `X-...-error` propias: no intentar leer el error desde cabeceras. |
| Paginación        | Los endpoints paginados devuelven un **arreglo JSON** con la página actual (no un objeto `Page`) y las cabeceras `X-Total-Count` y `Link`. Parámetros `page` (base 0), `size` y `sort=campo,asc\|desc`; tamaño por defecto **20**.                                                                           |
| Cuenta actual     | `GET /api/account` devuelve solo un `AdminUserDTO` (cuenta: `id`, `login`, `email`, `activated`, `langKey`, `authorities`, auditoría), **sin nombres, apellidos ni documento del perfil**. El perfil actual (nombres, tipo y número de documento, teléfono y correo) se consulta con `GET /api/account/profile` (UC003).                                                   |
| Roles             | El sistema emite `ROLE_ADMIN`, `ROLE_INSTRUCTOR`, `ROLE_APPRENTICE` y `ROLE_USER` (los tres primeros siempre acompañados de `ROLE_USER`). El rol Coordinador ya **no existe en el backend** (se eliminó de las autoridades, de los usuarios semilla y de los `@PreAuthorize`, y la migración 013 lo borra de las bases existentes): el frontend stock todavía lo contempla en `Authority.COORDINATOR`, en las rutas y en el dashboard, y debe quitarlo al adaptar esas pantallas. |
| Content-Type      | `application/json`; los `PATCH` aceptan además `application/merge-patch+json`.                                                                                                                                                                                                                               |

---

## UC001 — Registrarme

**Estado del backend:** implementado y verificado. Ver [`docs/api-contracts.md#uc001--registrarme`](./api-contracts.md#uc001--registrarme) y [`docs/use-cases.md`](./use-cases.md) (UC001).

**Estado del frontend:** el formulario sigue siendo el stock de JHipster (`username`, `email`, `firstPassword`, `secondPassword`) y **hoy el registro no funciona de extremo a extremo**.

### a. Formulario `src/main/webapp/app/modules/account/register/register.tsx`

| #   | Ítem                                                                                                                                                                                                                                                                                                                                   | Estado      |
| --- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Reemplazar los campos stock por: **tipo de documento** (select cargado de `GET /api/document-types/active`, que ya devuelve **solo** los activos), **número de documento**, **primer nombre**, **segundo nombre** (opcional), **primer apellido**, **segundo apellido** (opcional), **correo**, **teléfono** y **contraseña**. | `Pendiente` |
| 2   | Nota de UX: UC001 **no exige confirmación de contraseña**. Decidir si se mantiene el campo de confirmación por usabilidad.                                                                                                                                                                                                             | `Pendiente` |
| 3   | Quitar el bloque stock de "cuentas por defecto" (`global.messages.info.authenticated`) y el link manual de "si ya tienes perfil": no corresponden al flujo de UC001.                                                                                                                                                                   | `Pendiente` |
| 4   | El `IDocumentType` del frontend (`src/main/webapp/app/shared/model/document-type.model.ts`) **no declara `isActive`**; agregarlo para poder filtrar los tipos activos que devuelve el backend.                                                                                                                                         | `Pendiente` |

### b. `src/main/webapp/app/modules/account/register/register.reducer.ts`

| #   | Ítem                                                                                                                                                                                               | Estado      |
| --- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | El thunk hoy postea `{ login, email, password, langKey }`. Debe enviar `{ documentTypeId, documentNumber, firstName, middleName?, firstLastName, secondLastName?, email, phoneNumber, password }`. | `Pendiente` |
| 2   | El estado de rechazo hoy solo guarda `action.error.message`; debe exponer la clave de negocio (`error.<clave>`) recibida del backend para que la vista mapee el mensaje correspondiente.           | `Pendiente` |
| 3   | Actualizar `register.reducer.spec.ts`: hoy usa el payload viejo `{ login, email, password }` y el mensaje de éxito `register.messages.success`.                                                    | `Pendiente` |

### c. Validaciones de cliente

| #   | Regla                                                                                                                        | Estado      |
| --- | ---------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Número de documento: **solo dígitos**.                                                                                       | `Pendiente` |
| 2   | Teléfono: **exactamente 10 dígitos** (celular de Colombia, sin indicativo).                                                  | `Pendiente` |
| 3   | Correo: **formato válido** y obligatorio.                                                                                    | `Pendiente` |
| 4   | Contraseña: **8–20 caracteres** con al menos una **mayúscula**, una **minúscula**, un **número** y un **carácter especial**. | `Pendiente` |
| 5   | Alinear los mensajes de longitud del formulario stock (hoy mínimo 4 y máximo 50) con la política real.                       | `Pendiente` |

### d. Manejo de errores (mapear por `message`)

El backend responde `400` con `message: error.<clave>`; para validación de campos, `error.validation` más `fieldErrors`. Mapear cada clave a un mensaje de UI:

| Clave en `message`                 | Causa                                                                 | Comportamiento de UI                                                       | Estado      |
| ---------------------------------- | --------------------------------------------------------------------- | -------------------------------------------------------------------------- | ----------- |
| `error.documentnumberexists`       | El par tipo + número ya está registrado en una cuenta **activa**.     | Mostrar el error y mantener el formulario abierto (excepto la contraseña). | `Pendiente` |
| `error.documentnumberinactive`     | El par tipo + número pertenece a una cuenta **desactivada**.          | Informar que debe contactar al Administrador para reactivarla.             | `Pendiente` |
| `error.emailexists`                | El correo ya está en uso.                                             | Mostrar el error sobre el campo de correo.                                 | `Pendiente` |
| `error.emailrequired`              | Correo ausente o en blanco.                                           | Marcar el campo correo como obligatorio.                                   | `Pendiente` |
| `error.documentTypeNotFound`       | El `documentTypeId` no corresponde a un tipo existente.               | Invalidar el select de tipo de documento.                                  | `Pendiente` |
| `error.documentTypeInactive`       | El tipo de documento está inactivo y no puede usarse en el registro.  | Invalidar el select e indicar que no está disponible.                      | `Pendiente` |
| `error.validation` + `fieldErrors` | Fallo de validación de uno o más campos.                              | Resaltar el campo específico usando `field` y su `message`.                | `Pendiente` |
| Error de red (E4)                  | Sin conexión entre el envío y la creación; no se crea perfil parcial. | Mostrar "No se pudo completar el registro, intenta nuevamente".            | `Pendiente` |

Nota: la política de contraseña incumplida responde `400` con tipo `invalid-password` y `message: error.invalidpassword`; la UI debe traducirla a "Contraseña no válida" y no al genérico "Solicitud incorrecta".

### e. Éxito

| #   | Ítem                                                                                                                                                                  | Estado      |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Al `201 Created` (sin cuerpo), mostrar el mensaje de registro exitoso y **redirigir al inicio de sesión** (paso 8 de UC001).                                          | `Pendiente` |
| 2   | Hoy solo hay un link manual y el texto i18n de éxito (`register.messages.success`) menciona confirmación por correo. Corregir ese texto y automatizar la redirección. | `Pendiente` |

### f. Cancelar (flujo A1)

| #   | Ítem                                                                                         | Estado      |
| --- | -------------------------------------------------------------------------------------------- | ----------- |
| 1   | Botón "Cancelar" que descarta los datos ingresados y vuelve a la página de inicio de sesión. | `Pendiente` |

### g. i18n (`src/main/webapp/i18n/es/`)

Claves `error.*` verificadas contra los archivos actuales:

| Clave                          | Estado en i18n                                            | Verificado | Observación                                                              |
| ------------------------------ | --------------------------------------------------------- | ---------- | ------------------------------------------------------------------------ |
| `error.documentnumberexists`   | **Falta**                                                 | Sí         | Usada por el backend en UC001.                                           |
| `error.documentnumberinactive` | **Falta**                                                 | Sí         | Debe explicar que contacte al Administrador.                             |
| `error.emailrequired`          | **Falta**                                                 | Sí         | Debe marcar el correo como obligatorio.                                  |
| `error.documentTypeNotFound`   | **Falta**                                                 | Sí         | La clave viaja con esta capitalización exacta (`documentTypeNotFound`).  |
| `error.documentTypeInactive`   | **Falta**                                                 | Sí         | La clave viaja con esta capitalización exacta (`documentTypeInactive`).  |
| `error.emailexists`            | **Existe** (`global.json`, línea 155)                     | Sí         | Ya traducible; revisar redacción frente a UC001-E3.                      |
| `error.validation`             | **Existe** (`error.json`, línea 12)                       | Sí         | Solo texto genérico; el detalle real viene en `fieldErrors`.             |
| `error.http.400`               | **Existe** (`error.json`, línea 5)                        | Sí         | Hoy es "Solicitud incorrecta"; la UI debe priorizar la clave de negocio. |
| `register.messages.success`    | **Existe pero es incorrecta** (`register.json`, línea 16) | Sí         | Menciona confirmación por correo; UC001 no envía correo de activación.   |

### Checklist de aceptación (flujo básico UC001)

- [ ] 1. Desde el inicio de sesión, el aprendiz llega al formulario de registro (`Pendiente`).
- [ ] 2. El formulario muestra los 9 campos: tipo de documento, número de documento, primer nombre, segundo nombre (opcional), primer apellido, segundo apellido (opcional), correo, teléfono y contraseña (`Pendiente`).
- [ ] 3. El aprendiz completa el formulario (`Pendiente`).
- [ ] 4. El aprendiz hace click en "Registrar" (`Pendiente`).
- [ ] 5. La UI valida formato y obligatoriedad: documento solo dígitos, teléfono de 10 dígitos, correo válido y contraseña conforme a la política (`Pendiente`).
- [ ] 6. La UI envía la petición y el sistema verifica que tipo + número de documento y el correo no existan previamente (`Pendiente`).
- [ ] 7. El sistema crea el perfil con rol Aprendiz y estado Activo (`Pendiente`).
- [ ] 8. La UI muestra el mensaje de registro exitoso y redirige al inicio de sesión (`Pendiente`).

---

## UC002 — Iniciar sesión

**Estado del backend:** implementado. Ver [`docs/api-contracts.md#uc002--iniciar-sesión`](./api-contracts.md#uc002--iniciar-sesión).

| #   | Ítem                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               | Estado      |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------- |
| 1   | Cambiar el login de usuario/contraseña a **tipo de documento + número de documento + contraseña**. Hoy `login-modal.tsx` usa un campo `username` y `authentication.ts` postea `{ username, password, rememberMe }`.                                                                                                                                                                                                                                                                                                | `Pendiente` |
| 2   | Cargar el select de tipo de documento desde `GET /api/document-types/active` (endpoint público, devuelve solo los tipos activos).                                                                                                                                                                                                                                                                                                                                                                                                                          | `Pendiente` |
| 3   | Enviar `{ documentTypeId, documentNumber, password, rememberMe }` a `POST /api/authenticate` y guardar el `id_token` recibido.                                                                                                                                                                                                                                                                                                                                                                                     | `Pendiente` |
| 4   | El backend expone `mustChangePassword` en el `AdminUserDTO` (`GET /api/account` y respuestas admin) y ahora también lo devuelve `POST /api/authenticate` en el cuerpo del login; las cuentas creadas por un Administrador nacen en `true`. El cambio de contraseña (`POST /api/account/change-password` y `PATCH /api/account` cuando cambia la contraseña) limpia el indicador; el reset autoservicio (UC005) también lo limpia si estaba activo. El front puede usar el flag del login para forzar la pantalla de cambio obligatorio; el flujo en sí queda pendiente del frontend. | `Pendiente` |
| 5   | El login fallido responde `401` con la clave de negocio en `message`. Mapear `error.badcredentials` (E1: tipo/número de documento inexistente o contraseña incorrecta; el backend no revela cuál falló) a un mensaje genérico, y `error.accountinactive` (E2) a "Tu cuenta está inactiva, contacta al administrador". No mostrar el detalle de qué credencial falló.                                                                                                                                               | `Pendiente` |
| 6   | No hay bloqueo por intentos fallidos ni cierre por inactividad; la sesión expira a las 24 h (UC002-E3). El `401` por token expirado no trae clave de negocio: el frontend deriva la expiración del propio token (`exp` / `WWW-Authenticate`) y cierra la sesión.                                                                                                                                                                                                                                                   | `Pendiente` |

---

## UC003 — Modificar datos

**Estado del backend:** implementado. Ver [`docs/api-contracts.md#uc003--modificar-datos`](./api-contracts.md#uc003--modificar-datos).

| #   | Ítem                                                                                                                                                                                                                                         | Estado      |
| --- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Precargar el formulario de edición con `GET /api/account/profile`, que devuelve nombres, tipo y número de documento, teléfono y correo del usuario autenticado. `GET /api/account` no incluye esos campos del perfil.                        | `Pendiente` |
| 2   | `settings.reducer.ts` envía la actualización con `axios.post('api/account')`; el backend expone `PATCH /api/account`. Cambiar el método a `PATCH`.                                                                                           | `Pendiente` |
| 3   | Validar el teléfono en el cliente como **exactamente 10 dígitos**. El backend responde `400 error.validation` con `fieldErrors` sobre `phoneNumber` si llega con 9/11 dígitos o letras, y no persiste ningún cambio parcial.                 | `Pendiente` |
| 4   | Para **limpiar** el segundo nombre o el segundo apellido, enviar cadena vacía (`""`): el backend persiste `null`. Omitir el campo lo deja sin cambios.                                                                                       | `Pendiente` |
| 5   | El documento (tipo + número) es **inmutable**: **no enviar** `documentTypeId` ni `documentNumber` en el `PATCH`. Si el backend los recibe, responde `400 error.documentimmutable` (E3). Mapear esa clave a "Este dato no puede modificarse". | `Pendiente` |
| 6   | `imageUrl` ya **no se acepta** en `PATCH /api/account`: el backend lo **ignora** (no se persiste). No enviarlo en el formulario de autoedición.                                                                                              | `Pendiente` |

---

## UC004 — Cerrar sesión

**Estado del backend:** sin deuda. El backend es **JWT stateless** (`SessionCreationPolicy.STATELESS` + `oauth2ResourceServer.jwt`): no hay sesión de servidor, endpoint `/api/logout` ni revocación de tokens. El cierre de sesión es 100% del frontend (descartar el token); es coherente con UC004 (la invalidación inmediata de una cuenta desactivada queda como mejora futura).

| #   | Ítem                                                                                                                                                                                                                                                                        | Estado      |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | El logout ya existe (menú de cuenta → `/logout` → thunk `logout` que borra el token de `Storage.local`/`session` y resetea el estado). **Verificar** que el botón esté visible para los 3 roles y que redirija al login (hoy va a `/`, que muestra el login para anónimos). | `Pendiente` |
| 2   | El texto "Logged out successfully!" está **hardcodeado en inglés** (`modules/login/logout.tsx`); pasarlo a i18n en español.                                                                                                                                                 | `Pendiente` |
| 3   | E1 (sesión ya expirada) es el mismo caso que UC002-E3: el `401` no trae clave de negocio; el front deriva la expiración del token y redirige con el mensaje.                                                                                                                | `Pendiente` |

---

## UC005 — Recuperar contraseña

**Estado del backend:** implementado. El enlace de recuperación es de **un solo uso** y vence a los **30 minutos** (ver [`docs/api-contracts.md#uc005--recuperar-contraseña`](./api-contracts.md#uc005--recuperar-contraseña)). Al completar el reset, el backend **limpia** `mustChangePassword` si estaba activo (el usuario eligió su propia contraseña), por lo que no debe forzarse la pantalla de cambio obligatorio después de un reset.

| #   | Ítem                                                                                                                                                                            | Estado      |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | **E3 — Enlace expirado:** si el usuario abre el enlace después de los **30 minutos**, el backend responde `400` con `error.resetlinkexpired`; mostrar "El enlace ha expirado, solicita uno nuevo" y redirigirlo al paso 1 (solicitud). | `Pendiente` |
| 2   | **E2 — Contraseña débil:** si la nueva contraseña no cumple la política completa (8–20 con mayúscula, minúscula, número y carácter especial), el backend responde `400` con `error.invalidpassword`. Mostrar "Contraseña no válida" y conservar el enlace para reintentar. | `Pendiente` |
| 3   | **E4 — Enlace ya usado:** al reutilizar un enlace consumido el backend responde `400` con `error.resetlinkused`; mostrar "Este enlace ya no es válido" y redirigir a solicitar uno nuevo. | `Pendiente` |
| 4   | **E1 — Enlace inválido:** si el enlace no corresponde a ninguna solicitud, el backend responde `400` con `error.resetlinkinvalid`; mostrar "El enlace no es válido. Solicita uno nuevo". | `Pendiente` |

---

## UC019 — Gestionar configuración global

**Estado del backend:** implementado. La configuración global es un **singleton** con `id` fijo `global-configuration` y ya expone los **cuatro** parámetros del UC: `studentJustificationDays` (default 5), `instructorResponseDays` (default 2), `consecutiveAbsenceAlertThreshold` (default 3) y `accumulatedAbsenceAlertThreshold` (default 5). Ver [`docs/api-contracts.md#uc019--gestionar-configuración-global`](./api-contracts.md#uc019--gestionar-configuración-global).

**Estado del frontend:** pendiente. La pantalla de configuración debe editar y guardar los cuatro parámetros. Los endpoints de configuración (`GET` y `PATCH /api/global-configurations`) requieren el rol `ROLE_ADMIN`.

| #   | Ítem                                                                                                                                                                                                                                              | Estado      |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | La pantalla de configuración global debe mostrar los **dos umbrales de alerta de inasistencia** (`consecutiveAbsenceAlertThreshold`, default 3; `accumulatedAbsenceAlertThreshold`, default 5), además de `studentJustificationDays` e `instructorResponseDays`. | `Pendiente` |
| 2   | Validar en el cliente que los plazos `studentJustificationDays` e `instructorResponseDays` sean enteros entre **1 y 30 días**; fuera de ese rango el backend responde `400 error.validation` con una entrada en `fieldErrors` y no persiste el cambio. | `Pendiente` |
| 3   | Validar en el cliente que cada umbral (`consecutiveAbsenceAlertThreshold`, `accumulatedAbsenceAlertThreshold`) sea un entero `>= 1` (sin máximo); si no lo es, el backend responde `400 error.validation` con una entrada en `fieldErrors` y no persiste el cambio. | `Pendiente` |
| 4   | El `PATCH /api/global-configurations` es parcial: se puede enviar solo los campos que cambian, **sin `id`** (el backend siempre apunta al singleton `global-configuration`); los campos omitidos quedan igual. Si se envía un `id` distinto, responde `400 error.idinvalid`. | `Pendiente` |

---

## UC020 — Gestionar jornadas

**Estado del backend:** implementado. Ya está implementado el **nombre único** (E1): el backend recorta el nombre y lo compara sin distinguir mayúsculas; un duplicado responde `400` con `error.timeSlotNameAlreadyUsed`, y un nombre vacío o en blanco responde `400 error.validation` con una entrada en `fieldErrors`. También está implementado el **rechazo de horas iguales** (E2): si `startTime` y `endTime` son iguales el backend responde `400` con `error.timeSlotSameTime`, mientras que un rango que cruza medianoche (`endTime` menor que `startTime`, p. ej. 22:00–06:00) se permite. También está implementado el **bloqueo de eliminación** (E3): si una o más fichas usan la jornada, `DELETE /api/time-slots/{id}` responde `400` con `error.timeSlotInUse`; en ese caso la jornada no se elimina y debe **desactivarse** con `PATCH /api/time-slots` (`isActive: false`). El listado `GET /api/time-slots` está **paginado** (`page`/`size`/`sort`, 20 por defecto) y devuelve `X-Total-Count`/`Link`; `GET /api/time-slots/active` sigue sin paginar. La **escritura** (`POST`/`PUT`/`PATCH`/`DELETE`) quedó restringida a `ROLE_ADMIN`: los demás roles autenticados reciben `403`; la **lectura** sigue disponible para cualquier usuario autenticado. Ver [`docs/api-contracts.md#uc020--gestionar-jornadas`](./api-contracts.md#uc020--gestionar-jornadas).

**Estado del frontend:** pendiente. La pantalla de jornadas debe manejar los errores de nombre duplicado y de horas iguales, y evitar enviar nombres en blanco. El formulario de creación solo envía `name`, `startTime` y `endTime`: el backend crea la jornada como Activa e ignora `isActive` en el `POST`; el estado se cambia luego con el `PATCH`. En `PUT` y `PATCH` el `id` de la jornada debe enviarse **en el body**, no en la ruta. El listado debe consumir `GET /api/time-slots` como **paginado** (enviar `page`/`size` y leer `X-Total-Count` para el total de registros), usando `GET /api/time-slots/active` solo para selectores.

| #   | Ítem                                                                                                                                                                                                                       | Estado      |
| --- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Validar en el cliente que el nombre no esté vacío ni compuesto solo por espacios antes de enviar; el backend responde `400 error.validation` con `name` en `fieldErrors`.                                                  | `Pendiente` |
| 2   | Al crear o editar una jornada, manejar `400 error.timeSlotNameAlreadyUsed` mostrando "Ya existe una jornada con este nombre" (E1) y conservar los datos del formulario.                                                    | `Pendiente` |
| 3   | Validar en el cliente el máximo de 50 caracteres del nombre (el backend responde `400 error.validation` con `name` en `fieldErrors`).                                                                                     | `Pendiente` |
| 4   | Al crear o editar una jornada, manejar `400 error.timeSlotSameTime` mostrando "La hora de inicio y la hora de fin no pueden ser iguales" (E2); permitir enviar rangos que cruzan medianoche (p. ej. 22:00–06:00).            | `Pendiente` |
| 5   | Al eliminar una jornada en uso, manejar `400 error.timeSlotInUse` mostrando "No es posible eliminar la jornada: está asignada a fichas. Puedes desactivarla" (E3) y ofrecer **desactivarla** con `PATCH /api/time-slots` (`isActive: false`) en lugar de reintentar la eliminación. | `Pendiente` |
| 6   | En el formulario de creación, enviar solo `name`, `startTime` y `endTime` y no ofrecer un control de estado en el alta: el backend crea la jornada como Activa e ignora `isActive` en el `POST`. El estado se cambia después con el `PATCH`. | `Pendiente` |
| 7   | Enviar el `id` de la jornada **solo en el body** para `PUT /api/time-slots` y `PATCH /api/time-slots` (ya no va en la ruta); si falta el backend responde `400 error.idnull` y si no existe, `400 error.idnotfound`. | `Pendiente` |
| 8   | Consumir `GET /api/time-slots` como listado **paginado** (`page`/`size`/`sort`, 20 por defecto) y usar `X-Total-Count` para el total de registros; reservar `GET /api/time-slots/active` para selectores. | `Pendiente` |
| 9   | Mostrar la pantalla de **gestión de jornadas** (crear, editar, desactivar, eliminar) **solo a `ROLE_ADMIN`**: el backend restringe la escritura a ese rol y responde `403` a los demás. La lectura sigue disponible para cualquier usuario autenticado y solo debe usarse en selectores. | `Pendiente` |

---

## UC021 — Gestionar modalidades

**Estado del backend:** implementado. Ya está implementado el **nombre único** (E1): el backend recorta el nombre y lo compara sin distinguir mayúsculas; un duplicado responde `400` con `error.modalityNameAlreadyUsed`, y un nombre vacío o en blanco responde `400 error.validation` con una entrada en `fieldErrors`. Al crear, la modalidad nace **Activa**: el backend fuerza `isActive = true` e ignora el valor enviado. También está implementado el **bloqueo de eliminación** (E2): si una o más fichas usan la modalidad, `DELETE /api/modalities/{id}` responde `400` con `error.modalityInUse`; en ese caso la modalidad no se elimina y debe **desactivarse** con `PATCH /api/modalities` (`isActive: false`). El listado `GET /api/modalities` está **paginado** (`page`/`size`/`sort`, 20 por defecto) y devuelve `X-Total-Count`/`Link`; `GET /api/modalities/active` sigue sin paginar. La **escritura** (`POST`/`PUT`/`PATCH`/`DELETE`) quedó restringida a `ROLE_ADMIN`: los demás roles autenticados reciben `403`; la **lectura** sigue disponible para cualquier usuario autenticado. Ver [`docs/api-contracts.md#uc021--gestionar-modalidades`](./api-contracts.md#uc021--gestionar-modalidades).

**Estado del frontend:** pendiente. La pantalla de modalidades debe manejar el error de nombre duplicado y evitar enviar nombres en blanco. El listado debe consumir `GET /api/modalities` como **paginado** (enviar `page`/`size` y leer `X-Total-Count` para el total de registros), usando `GET /api/modalities/active` solo para selectores.

| #   | Ítem                                                                                                                                                                        | Estado      |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Validar en el cliente que el nombre no esté vacío ni compuesto solo por espacios antes de enviar; el backend responde `400 error.validation` con `name` en `fieldErrors`.   | `Pendiente` |
| 2   | Al crear o editar una modalidad, manejar `400 error.modalityNameAlreadyUsed` mostrando "Ya existe una modalidad con este nombre" (E1) y conservar los datos del formulario. | `Pendiente` |
| 3   | Validar en el cliente el máximo de 50 caracteres del nombre (el backend responde `400 error.validation` con `name` en `fieldErrors`).                                      | `Pendiente` |
| 4   | En el formulario de creación no enviar `isActive`: el backend siempre crea la modalidad **Activa** (`isActive = true`) e ignora el valor enviado.                           | `Pendiente` |
| 5   | Al eliminar una modalidad en uso, manejar `400 error.modalityInUse` mostrando "No es posible eliminar la modalidad: está asignada a fichas. Puedes desactivarla" (E2) y ofrecer **desactivarla** con `PATCH /api/modalities` (`isActive: false`) en lugar de reintentar la eliminación. | `Pendiente` |
| 6   | Al editar, enviar el `id` de la modalidad en el cuerpo del `PUT`/`PATCH` (la ruta es `/api/modalities`, sin `{id}`): el backend lee el `id` del cuerpo y responde `400 error.idnull` si falta. | `Pendiente` |
| 7   | Consumir `GET /api/modalities` como listado **paginado** (`page`/`size`/`sort`, 20 por defecto) y usar `X-Total-Count` para el total de registros; reservar `GET /api/modalities/active` para selectores. | `Pendiente` |
| 8   | Mostrar la pantalla de **gestión de modalidades** (crear, editar, desactivar, eliminar) **solo a `ROLE_ADMIN`**: el backend restringe la escritura a ese rol y responde `403` a los demás. La lectura sigue disponible para cualquier usuario autenticado y solo debe usarse en selectores. | `Pendiente` |

---

## UC022 — Gestionar tipos de documento

**Estado del backend:** implementado. El **nombre** y las **iniciales** son únicos: el backend recorta el nombre y compara ambos sin distinguir mayúsculas, y normaliza las iniciales a mayúsculas; un duplicado responde `400` con `error.documentTypeNameAlreadyUsed` (E1) o `error.documentTypeInitialsAlreadyUsed` (E2), y un valor vacío o en blanco responde `400 error.validation` con una entrada en `fieldErrors`. Las **iniciales de un tipo en uso** no se pueden cambiar: si algún perfil referencia el tipo, el `PUT`/`PATCH` responde `400` con `error.documentTypeInitialsInUse` (E3), mientras que el nombre sí puede cambiarse. Al eliminar, si algún perfil usa el tipo, `DELETE /api/document-types/{id}` responde `400` con `error.documentTypeInUse` (E4) y el tipo debe **desactivarse** con `PATCH /api/document-types` (`isActive: false`). El listado `GET /api/document-types` está **paginado** (`page`/`size`/`sort`, 20 por defecto) y devuelve `X-Total-Count`/`Link`; `GET /api/document-types/active` sigue sin paginar y es el que deben usar el registro (UC001) y el login. En `PUT`/`PATCH` el `id` viaja **solo en el body** (ruta `/api/document-types`, sin `{id}`). La **escritura** (`POST`/`PUT`/`PATCH`/`DELETE`) quedó restringida a `ROLE_ADMIN`; el listado y el detalle siguen siendo públicos. Ver [`docs/api-contracts.md#uc022--gestionar-tipos-de-documento`](./api-contracts.md#uc022--gestionar-tipos-de-documento).

**Estado del frontend:** pendiente. La pantalla de tipos de documento debe manejar los errores de nombre/iniciales duplicados y de tipo en uso, enviar el `id` en el body en `PUT`/`PATCH`, consumir el listado paginado y reservar `GET /api/document-types/active` para los selectores de registro y login.

| #   | Ítem                                                                                                                                                                                                        | Estado      |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Validar en el cliente que el nombre y las iniciales no estén vacíos ni compuestos solo por espacios antes de enviar; el backend responde `400 error.validation` con el campo en `fieldErrors`.              | `Pendiente` |
| 2   | Al crear o editar un tipo, manejar `400 error.documentTypeNameAlreadyUsed` mostrando "Ya existe un tipo de documento con este nombre" (E1) y conservar los datos del formulario.                            | `Pendiente` |
| 3   | Al crear o editar un tipo, manejar `400 error.documentTypeInitialsAlreadyUsed` mostrando "Ya existe un tipo de documento con estas iniciales" (E2) y conservar los datos del formulario.                    | `Pendiente` |
| 4   | Validar en el cliente los máximos (nombre 30, iniciales 10) y mostrar las iniciales en mayúsculas (el backend las normaliza igualmente); el backend responde `400 error.validation` con el campo en `fieldErrors`. | `Pendiente` |
| 5   | Al editar un tipo **en uso**, manejar `400 error.documentTypeInitialsInUse` mostrando "No se pueden modificar las iniciales de un tipo de documento en uso" (E3); permitir editar el nombre y no reenviar iniciales distintas. | `Pendiente` |
| 6   | Al eliminar un tipo en uso, manejar `400 error.documentTypeInUse` mostrando "No es posible eliminar el tipo de documento: está en uso por usuarios. Puedes desactivarlo" (E4) y ofrecer **desactivarlo** con `PATCH /api/document-types` (`isActive: false`) en lugar de reintentar la eliminación. | `Pendiente` |
| 7   | Enviar el `id` del tipo **solo en el body** para `PUT /api/document-types` y `PATCH /api/document-types` (ya no va en la ruta); si falta el backend responde `400 error.idnull` y si no existe, `400 error.idnotfound`. | `Pendiente` |
| 8   | Consumir `GET /api/document-types` como listado **paginado** (`page`/`size`/`sort`, 20 por defecto) y usar `X-Total-Count`; en el **registro (UC001) y el login** usar `GET /api/document-types/active`, que devuelve el arreglo completo de activos. | `Pendiente` |
| 9   | Mostrar la pantalla de **gestión de tipos de documento** (crear, editar, desactivar, eliminar) **solo a `ROLE_ADMIN`**: el backend restringe la escritura a ese rol y responde `403` a los demás. La lectura sigue siendo pública. | `Pendiente` |

---

## UC016 — Gestionar tipos de justificación

**Estado del backend:** implementado. El **nombre es único** (se compara sin distinguir mayúsculas): un duplicado responde `400` con `error.justificationTypeNameAlreadyUsed` (E1) y un nombre vacío o en blanco responde `400 error.validation` con `name` en `fieldErrors`. El **límite de días por trimestre** es obligatorio y mayor a 0 (`@Min(1)`): un valor nulo, 0 o negativo responde `400 error.validation` con `limitPerTrimester` en `fieldErrors` (E2). Al crear, el tipo nace **Activo**: el formulario del UC solo envía nombre y límite, y `status` es opcional (si se omite queda `ACTIVO`); Desactivar/Reactivar (A2/A3) se hace con `PATCH` (`status: "INACTIVO"` / `"ACTIVO"`). Al eliminar, si el tipo ya fue usado en alguna justificación, `DELETE /api/justification-types/{id}` responde `400` con `error.justificationTypeInUse` (E3) y el tipo debe **desactivarse**. `GET /api/justification-types/active` devuelve solo los activos y es el que debe consumir el formulario del aprendiz (UC011); el listado `GET /api/justification-types` **no** está paginado. En `PUT`/`PATCH` el `id` viaja **solo en el body** (ruta `/api/justification-types`, sin `{id}`). La **escritura** quedó restringida a `ROLE_ADMIN`; la lectura sigue para cualquier usuario autenticado. **Cambio breaking:** el campo de estado se renombró de `state` a **`status`** en el JSON del API. Ver [`docs/api-contracts.md#uc016--gestionar-tipos-de-justificación`](./api-contracts.md#uc016--gestionar-tipos-de-justificación).

**Estado del frontend:** pendiente. La pantalla debe manejar E1/E2/E3, enviar el `id` en el body en `PUT`/`PATCH`, usar `GET /api/justification-types/active` en el formulario del aprendiz (UC011) y adaptar el campo `state` → `status`.

| #   | Ítem                                                                                                                                                                                                                                    | Estado      |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Renombrar el campo `state` → `status` en el modelo (`justification-type.model.ts`), la tabla, el formulario y el detalle; el JSON del backend ahora usa `status` (**cambio breaking**).                                                  | `Pendiente` |
| 2   | Validar en el cliente que el nombre no esté vacío ni compuesto solo por espacios; el backend responde `400 error.validation` con `name` en `fieldErrors`.                                                                               | `Pendiente` |
| 3   | Al crear o editar un tipo, manejar `400 error.justificationTypeNameAlreadyUsed` mostrando "Ya existe un tipo de justificación con este nombre" (E1) y conservar los datos del formulario.                                                | `Pendiente` |
| 4   | Validar el límite como entero mayor a 0 y manejar `400 error.validation` con `limitPerTrimester` en `fieldErrors` (E2).                                                                                                                 | `Pendiente` |
| 5   | En el alta no enviar `status`: el backend crea el tipo **Activo** e ignora un estado enviado. El estado se cambia después con `PATCH` (`status: "INACTIVO"`/`"ACTIVO"`).                                                                   | `Pendiente` |
| 6   | Al eliminar un tipo en uso, manejar `400 error.justificationTypeInUse` mostrando "No es posible eliminar el tipo: ya fue usado en justificaciones. Puedes desactivarlo" (E3) y ofrecer **desactivarlo** en lugar de reintentar la eliminación. | `Pendiente` |
| 7   | Enviar el `id` del tipo **solo en el body** para `PUT /api/justification-types` y `PATCH /api/justification-types` (ya no va en la ruta); si falta el backend responde `400 error.idnull` y si no existe, `400 error.idnotfound`.       | `Pendiente` |
| 8   | Usar `GET /api/justification-types/active` en el **formulario del aprendiz (UC011)** para ofrecer solo los tipos disponibles; el listado de gestión `GET /api/justification-types` no está paginado.                                      | `Pendiente` |
| 9   | Mostrar la pantalla de **gestión de tipos de justificación** (crear, editar, desactivar, eliminar) **solo a `ROLE_ADMIN`**: el backend restringe la escritura a ese rol y responde `403` a los demás.                                   | `Pendiente` |

---

## UC006 — Gestionar perfiles

**Estado del backend:** implementado, incluido el **reenvío de credenciales (E7)** por `PATCH /api/admin/users/resend-credentials`. Un solo rol por cuenta; solo se pueden asignar Administrador, Instructor o Aprendiz. El **cambio de rol** respeta las guardas de último administrador, último instructor y cuenta `admin` protegida. El **login se recalcula** al corregir el tipo o el número de documento. **Los usuarios nunca se eliminan.** Ver [`docs/api-contracts.md#uc006--gestionar-perfiles`](./api-contracts.md#uc006--gestionar-perfiles).

**Estado del frontend:** pendiente.

| #   | Ítem                                                                                                                                                                                                                                    | Estado      |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Quitar la acción de **eliminar usuario**: el endpoint `DELETE /api/admin/users/{login}` fue retirado y responde `405`; usar desactivar/reactivar (`PATCH /api/admin/users/activated`).                                                    | `Pendiente` |
| 2   | Manejar las claves de guardas: `400 error.adminprotected` (cuenta admin protegida), `400 error.lastAdmin` (último administrador activo), `400 error.lastInstructor` (último instructor de materias de fichas en estado Pendiente o Activa) y `400 error.rolenotfound`. | `Pendiente` |
| 3   | En `PATCH /api/admin/users` enviar el `id` en el body; el `rol` solo admite `ROLE_ADMIN`, `ROLE_INSTRUCTOR` o `ROLE_APPRENTICE` (sin Coordinador).                                                                                       | `Pendiente` |
| 4   | Refrescar el **login derivado** que se muestra cuando el Administrador corrige el tipo o el número de documento.                                                                                                                        | `Pendiente` |
| 5   | La búsqueda `GET /api/admin/users/search` admite también el parámetro `role` y pagina con `X-Total-Count`/`Link` (20 por defecto).                                                                                                       | `Pendiente` |
| 6   | En el alta no enviar estado: la cuenta nace `mustChangePassword = true`; el **reenvío de credenciales (E7)** ya tiene endpoint (`PATCH /api/admin/users/resend-credentials`, ver UC018).                                                                                                            | `Pendiente` |

---

## UC012 — Gestionar programas de aprendizaje

**Estado del backend:** implementado. El **código es solo numérico** (E5) y la **cantidad de trimestres** va de 1 a 12 (E6); el programa **nace Activo**; al eliminar, si tiene fichas, responde `400 error.programInUse` (E8) y debe **desactivarse**; `GET /api/programs/active` alimenta la creación de fichas (UC007). En `PUT` el `id` viaja **solo en el body**. La escritura quedó restringida a `ROLE_ADMIN`. Ver [`docs/api-contracts.md#uc012--gestionar-programas-de-aprendizaje`](./api-contracts.md#uc012--gestionar-programas-de-aprendizaje).

**Estado del frontend:** pendiente. El formulario debe validar el código numérico y el rango de trimestres, no enviar estado en el alta, y adaptar el `PUT` al id en el body.

| #   | Ítem                                                                                                                                                                                                                                    | Estado      |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Validar el **código solo numérico** (E5) antes de enviar; el backend responde `400 error.validation` con `code` en `fieldErrors` (POST/PUT) o `400 error.codenotnumeric` (PATCH).                                                          | `Pendiente` |
| 2   | Validar la **cantidad de trimestres 1–12** (E6); un PATCH fuera de rango responde `400 error.trimestersoutofrange`.                                                                                                                       | `Pendiente` |
| 3   | En el alta no ofrecer estado: el programa nace **Activo** e ignora el `status` enviado. El estado se cambia con `PATCH /api/programs/activated`.                                                                                          | `Pendiente` |
| 4   | Enviar el `id` del programa **solo en el body** para `PUT /api/programs` (cambio breaking: la ruta ya no lleva `{id}`); si falta, `400 error.idnull`, y si no existe, `400 error.idnotfound`.                                              | `Pendiente` |
| 5   | Al eliminar un programa con fichas, manejar `400 error.programInUse` y ofrecer **desactivarlo** en lugar de reintentar.                                                                                                                   | `Pendiente` |
| 6   | Mostrar la advertencia de desactivación (E7) con `warning` y `activeFichasCount` devueltos por `PATCH /api/programs/activated`.                                                                                                          | `Pendiente` |
| 7   | Usar `GET /api/programs/active` en el selector de creación de fichas (UC007); el listado de gestión `GET /api/programs` es paginado (`X-Total-Count`).                                                                                    | `Pendiente` |
| 8   | Mostrar la gestión de programas solo a `ROLE_ADMIN`: el backend restringe la escritura a ese rol y responde `403` a los demás.                                                                                                           | `Pendiente` |

---

## UC014 — Gestionar trimestres académicos

**Estado del backend:** implementado. El `status` del trimestre es un **enum persistido** (`StateTrimester`: `FUTURO`, `ACTIVO`, `CERRADO`) que el servidor calcula por fechas e **ignora el valor enviado**; un job diario lo sincroniza y la migración Mongock orden 009 convierte el booleano previo. Al crear se exige fecha inicio desde mañana y fecha fin no anterior a hoy. Ver [`docs/api-contracts.md#uc014--gestionar-trimestres-académicos`](./api-contracts.md#uc014--gestionar-trimestres-académicos).

**Estado del frontend:** pendiente. **Cambio incompatible:** el campo `status` pasó de booleano (`false`) a string (`"FUTURO"`/`"ACTIVO"`/`"CERRADO"`), y el parámetro `status` de la búsqueda usa los mismos tres valores. El frontend stock de JHipster sigue enviando y esperando el booleano.

| #   | Ítem                                                                                                                                                                                                                                              | Estado      |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Cambiar el campo `status` de booleano a string en el modelo (`trimester.model.ts`), la tabla, el formulario y el detalle; el backend devuelve `"status": "FUTURO"\|"ACTIVO"\|"CERRADO"` (**cambio incompatible**).                                  | `Pendiente` |
| 2   | No enviar `status` en `POST`/`PUT`/`PATCH`: el servidor lo calcula por fechas e ignora el valor enviado. En el alta, recordar que todo trimestre nace `FUTURO` (fecha inicio desde mañana y fecha fin no anterior a hoy).                          | `Pendiente` |
| 3   | En la búsqueda `GET /api/trimesters/search`, enviar `?status=FUTURO\|ACTIVO\|CERRADO` (ya no un booleano) además de nombre o año; el filtro de fechas sigue siendo el año.                                                                          | `Pendiente` |
| 4   | En `PUT` enviar el `id` **solo en el body** (`PUT /api/trimesters`, ruta sin `/{id}`); si falta, `400 error.idnull`, y si no existe, `400 error.idnotfound`. Ya no se emite `error.idinvalid` por desajuste entre ruta y body.                      | `Pendiente` |
| 5   | Restringir la pantalla de gestión a `ROLE_ADMIN`: todas las escrituras y ahora también `GET /api/trimesters/{id}` exigen ese rol; los demás roles autenticados reciben `403`. El listado y la búsqueda siguen disponibles para cualquier autenticado. | `Pendiente` |
| 6   | Al crear, mapear los errores de fecha: `400 error.datesorder`, `400 error.enddateinpast`, `400 error.startdatemustbefuture` y `400 error.datesoverlap`, mostrando el mensaje sobre el campo de fechas.                                              | `Pendiente` |
| 7   | Al eliminar, manejar `400 error.trimesterInUse` ("No es posible eliminar el trimestre: tiene horarios o asistencias registradas") y ofrecer conservarlo en lugar de reintentar.                                                                      | `Pendiente` |
| 8   | Consumir `GET /api/trimesters` como listado **paginado** (`page`/`size`/`sort`, 20 por defecto) usando `X-Total-Count`; el backend no expone `/api/trimesters/active`.                                                                              | `Pendiente` |

---

## UC007 — Gestionar fichas

**Estado del backend:** implementado. El `state` de la ficha es un **enum persistido** (`StateGrade`: `PENDIENTE`, `ACTIVA`, `FINALIZADA`, `APLAZADA`, `CANCELADA`): los tres primeros los calcula el servidor por fechas y un job diario los sincroniza, mientras que `APLAZADA` y `CANCELADA` son manuales y el cálculo por fechas nunca las pisa. La migración Mongock orden 010 (`MigrateGradeInactivaToAplazada`) convierte el valor legado `INACTIVA` a `APLAZADA`. Ver [`docs/api-contracts.md#uc007--gestionar-fichas`](./api-contracts.md#uc007--gestionar-fichas).

**Estado del frontend:** pendiente. **Cambios incompatibles:** `state` pasó de tres valores (`ACTIVA`, `INACTIVA`, `APLAZADA`) a cinco (`PENDIENTE`, `ACTIVA`, `FINALIZADA`, `APLAZADA`, `CANCELADA`) y **ya no se envía** en `POST`/`PUT`/`PATCH` (el servidor lo calcula por fechas e ignora el valor recibido); `PUT` y `PATCH` ya no llevan `/{id}` (el `id` va **solo en el body**) y aparecen tres acciones nuevas. El frontend stock sigue enviando `state` (con `ACTIVA` por defecto), lista `INACTIVA` en su modelo de estados y arma `PUT`/`PATCH` con el `id` en la ruta. Además, `GET /api/grades`, `GET /api/grades/{id}` y `GET /api/grades/active` quedan restringidos a `ROLE_ADMIN` (**cambio incompatible**: la pantalla stock de fichas responde `403` a los roles no admin; el Instructor consulta sus fichas por `GET /api/class-sections/mine`, UC017).

| #   | Ítem                                                                                                                                                                                                                                          | Estado      |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Actualizar el enum `StateGrade` en `src/main/webapp/app/shared/model/enumerations/state-grade.model.ts`: quitar `INACTIVA` y agregar `PENDIENTE`, `FINALIZADA` y `CANCELADA`; actualizar también las etiquetas de `src/main/webapp/i18n/es/stateGrade.json` (**cambio incompatible**). | `Pendiente` |
| 2   | No enviar `state` en `POST`/`PUT`/`PATCH`: el servidor lo calcula por fechas e ignora el valor enviado. Quitar el select de estado del formulario; en el alta, la ficha nace `PENDIENTE` si la fecha de inicio es futura y `ACTIVA` si ya arrancó. | `Pendiente` |
| 3   | Enviar el `id` **solo en el body** para `PUT /api/grades` y `PATCH /api/grades` (la ruta ya no lleva `/{id}`); el reducer stock usa `api/grades/${entity.id}`. Si falta, `400 error.idnull`, y si no existe, `400 error.idnotfound`; ya no se emite `error.idinvalid`. | `Pendiente` |
| 4   | Agregar las acciones **Aplazar**, **Reanudar** y **Cancelar** con `PATCH /api/grades/postponed`, `/resumed` y `/cancelled`, enviando `{ "id": "<ficha>" }` en el body. Pedir confirmación (la cancelación es definitiva) y manejar `400 error.invalidtransition` cuando la acción no aplique al estado actual. | `Pendiente` |
| 5   | Aplicar las **reglas de edición por estado** en el formulario: `FINALIZADA` no permite ningún cambio (`400 error.noteditable`); `ACTIVA` solo fecha fin, programa y código; `APLAZADA` solo fecha fin; `PENDIENTE` y `CANCELADA` permiten todo. Ocultar o deshabilitar los campos bloqueados y mapear `400 error.fieldlocked`. | `Pendiente` |
| 6   | Validar el **código solo numérico y único**: un duplicado responde `400 error.gradeCodeAlreadyUsed` (E1); un código no numérico responde `400 error.validation` con `code` en `fieldErrors` en `POST`/`PUT` y `400 error.codenotnumeric` en `PATCH`. | `Pendiente` |
| 7   | Manejar `400 error.gradeCodeLocked` al cambiar el código de una ficha que ya tiene materias o aprendices (el candado aplica aunque el estado permita editar el código). | `Pendiente` |
| 8   | Validar las **fechas** en el cliente: `endDate` no anterior a `startDate` (`400 error.datesorder`) y `startDate` no anterior a hoy al crear o al cambiarla (`400 error.startdateinpast`). | `Pendiente` |
| 9   | Refrescar los selectores con los **catálogos activos** (`GET /api/programs/active`, `GET /api/modalities/active` y `GET /api/time-slots/active`) y manejar la desactivación al guardar: `400 error.programInactive`, `400 error.modalityInactive` y `400 error.timeSlotInactive` (E3). | `Pendiente` |
| 10  | Al eliminar, manejar `400 error.gradeInUse` mostrando el mensaje del UC y ofreciendo **Cancelar ficha** en lugar de reintentar la eliminación (una ficha con aprendices o asistencias nunca se elimina). | `Pendiente` |
| 11  | Mostrar la **gestión de fichas solo a `ROLE_ADMIN`**: el backend restringe las escrituras y también las lecturas genéricas (`GET /api/grades`, `GET /api/grades/{id}` y `GET /api/grades/active`) a ese rol, y responde `403` a los demás (**cambio incompatible**: la pantalla stock de fichas deja de funcionar para roles no admin). El Instructor consulta sus fichas por `GET /api/class-sections/mine` (UC017). | `Pendiente` |
| 12  | Mostrar los **cinco estados** en el listado y el detalle con las etiquetas i18n de `stateGrade.json` (hoy solo existen `ACTIVA`, `INACTIVA` y `APLAZADA`). | `Pendiente` |

---

## UC015 — Gestionar materias

**Estado del backend:** implementado. El CRUD de materias (`ClassSection`) aplica nombre único por ficha (E2), reglas de horarios contra la jornada de la ficha (E3), no solapamiento (E4) y mismo día (E5), bloqueo de fichas no operables (E1) y de trimestres cerrados por fechas (E6), instructor opcional con cuenta activa (E7) y borrado bloqueado con asistencias más cascada de horarios y excepciones (A3). Ver [`docs/api-contracts.md#uc015--gestionar-materias`](./api-contracts.md#uc015--gestionar-materias).

**Estado del frontend:** pendiente. **Cambios incompatibles:** `PUT` y `PATCH` de `/api/class-sections`, `/api/class-schedules` y `/api/class-exceptions` ya no llevan `/{id}` (el `id` va **solo en el body**); el `instructor` pasó a ser **opcional**; `dayOfWeek` es **obligatorio** en los horarios; las escrituras de materias y horarios quedan restringidas a `ROLE_ADMIN` y las de excepciones aceptan `ROLE_ADMIN` o al **instructor asignado a la materia** (A4 de UC009); y `GET /api/class-sections/mine` ya no acepta Coordinador (solo `ROLE_INSTRUCTOR` o `ROLE_ADMIN`). El frontend stock de JHipster sigue armando `PUT`/`PATCH` con el `id` en la ruta, envía `dayOfWeek` como opcional y muestra la pantalla a cualquier rol. Además, `GET /api/class-sections` y `GET /api/class-sections/{id}` quedan restringidos a `ROLE_ADMIN` (**cambio incompatible**: la pantalla stock de materias responde `403` a los roles no admin; el Instructor consulta las suyas por `GET /api/class-sections/mine`, UC017).

| #   | Ítem                                                                                                                                                                                                                                                                             | Estado      |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Enviar el `id` **solo en el body** para `PUT /api/class-sections` y `PATCH /api/class-sections` (materias); si falta, `400 error.idnull`, y si no existe, `400 error.idnotfound`. Ya no se emite `error.idinvalid`.                                                               | `Pendiente` |
| 2   | Enviar el `id` **solo en el body** también en `PUT`/`PATCH` de `/api/class-schedules` y `/api/class-exceptions` (la ruta no lleva `{id}`).                                                                                                                                        | `Pendiente` |
| 3   | Formulario de materia: el `instructor` es **opcional** (se puede crear sin instructor y asignarlo después); si se envía, debe existir con cuenta activa o el backend responde `400 error.instructorInactive` (E7) y conviene refrescar la lista de instructores.                   | `Pendiente` |
| 4   | Validar en el cliente el nombre recortado y sin duplicados dentro de la ficha, y manejar `400 error.classSectionNameAlreadyUsed` mostrando "Ya existe una materia con este nombre en esta ficha" (E2) sin cerrar el formulario.                                                    | `Pendiente` |
| 5   | Formulario de horarios: `dayOfWeek` es **obligatorio** (select `LUNES` … `DOMINGO`); si falta, el backend responde `400 error.validation` con `dayOfWeek` en `fieldErrors`.                                                                                                       | `Pendiente` |
| 6   | Manejar los errores de horario sin cerrar el formulario: `400 error.scheduleCrossesMidnight` (E5), `400 error.scheduleOutOfTimeSlot` (E3) y `400 error.scheduleOverlap` (E4), con los mensajes del UC.                                                                             | `Pendiente` |
| 7   | No ofrecer crear ni editar materias en fichas fuera de `PENDIENTE`/`ACTIVA` (incluida la reactivación A4): el backend responde `400 error.gradeNotOperable` (E1).                                                                                                                 | `Pendiente` |
| 8   | No permitir crear, editar ni eliminar horarios de un trimestre **cerrado** (el cliente puede calcularlo por fechas); el backend responde `400 error.trimesterClosed` (E6) con la clasificación por fechas, sin ventana de gracia.                                                 | `Pendiente` |
| 9   | Al eliminar una materia con asistencias, manejar `400 error.classSectionInUse` con el mensaje del UC y ofrecer **desactivarla** con `PATCH /api/class-sections` (`isActive: false`, `id` en el body) en lugar de reintentar. Si no tiene asistencias, el backend borra en cascada sus horarios y excepciones. | `Pendiente` |
| 10  | Mostrar la **gestión de materias y horarios solo a `ROLE_ADMIN`**: el backend restringe esas escrituras a ese rol y responde `403` a los demás. Las **excepciones no lectivas** también las gestiona el **instructor asignado a la materia** (A4 de UC009), no solo el Admin. Las lecturas genéricas de materias (`GET /api/class-sections` y `GET /api/class-sections/{id}`) también quedan solo para `ROLE_ADMIN` (**cambio incompatible**: la pantalla stock de materias deja de funcionar para roles no admin; el Instructor usa `/api/class-sections/mine`). Los `GET` de horarios siguen abiertos a cualquier autenticado (deuda en [`docs/backend-debt.md`](./backend-debt.md)) y los de excepciones quedan acotados al instructor. | `Pendiente` |

---

## UC008 — Gestionar aprendices

**Estado del backend:** implementado. La vinculación identifica al aprendiz por **número de documento** (no por id de perfil) y el servidor fija el estado académico `MATRICULADO`; la desvinculación lleva motivo y conserva el historial cuando existen asistencias. Ver [`docs/api-contracts.md#uc008--gestionar-aprendices`](./api-contracts.md#uc008--gestionar-aprendices).

**Estado del frontend:** pendiente. **Cambios incompatibles:** el alta pasó a `POST /api/apprentices` con `documentNumber` + `grade` y **sin `stateAcademic`** (el servidor siempre matricula); `PUT`, `PATCH` y `DELETE /api/apprentices/{id}` fueron **retirados** (el UC solo contempla vincular, desvincular y consultar); la desvinculación es `PATCH /api/apprentices/unlinked` con el `id` del vínculo **en el body**; y las lecturas quedaron restringidas a `ROLE_ADMIN` o `ROLE_INSTRUCTOR`, con la escritura solo `ROLE_ADMIN`. El frontend stock de JHipster sigue posteando `{ stateAcademic, student, grade }`, armando `PUT`/`PATCH`/`DELETE` con el `id` en la ruta y mostrando la pantalla a cualquier rol.

| #   | Ítem                                                                                                                                                                                                                                                                                                                    | Estado      |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Alta: enviar `{ documentNumber, grade: { id } }` y **no enviar `stateAcademic`**. El backend resuelve el aprendiz por documento y siempre crea el vínculo como `MATRICULADO`; el documento debe tener el formato de UC001 (solo dígitos, 1–30).                                                                          | `Pendiente` |
| 2   | Manejar `400 error.apprenticeInactive` (E1: aprendiz inexistente, cuenta inactiva o documento que identifica a más de un perfil): mostrar "Aprendiz no existe o no está activo" y no continuar.                                                                                                                          | `Pendiente` |
| 3   | Manejar `400 error.apprenticeAlreadyEnrolled` (E2: ya existe un registro de ese aprendiz en la ficha, en cualquier estado): mostrar "Este aprendiz ya tiene un registro en esta ficha" y no ofrecer reintentar la vinculación.                                                                                           | `Pendiente` |
| 4   | Manejar `400 error.gradeNotOperable` (E3: ficha fuera de `PENDIENTE`/`ACTIVA`) al vincular y al desvincular. La clave es **compartida con UC015**: el texto debe indicar que la ficha no permite la operación, sin limitarse a materias.                                                                                 | `Pendiente` |
| 5   | Validar el documento en el cliente (solo dígitos, 1–30) y manejar `400 error.validation` con `documentNumber` en `fieldErrors` (E4).                                                                                                                                                                                    | `Pendiente` |
| 6   | Desvincular con `PATCH /api/apprentices/unlinked` enviando `{ id, reason }` en el body (el `id` es del vínculo, no del aprendiz): select de motivo Retiro voluntario (`RETIRO_VOLUNTARIO`), Aplazado (`APLAZADO`) o Cancelado (`CANCELADO`) con confirmación previa; manejar `400 error.invalidunlinkreason` si el valor no es válido. | `Pendiente` |
| 7   | Interpretar ambos finales de la desvinculación: `204` sin cuerpo cuando el registro se elimina (sin asistencias) y `200` con el DTO cuando se conserva (con asistencias); mostrar "Aprendiz desvinculado" o "Aprendiz desvinculado. Se conservó su historial de asistencia" según el caso.                              | `Pendiente` |
| 8   | Consultar (A2) con `GET /api/apprentices` paginado (`page`/`size`/`sort`, `X-Total-Count`/`Link`) y filtros opcionales `gradeId`, `documentNumber`, `name` y `stateAcademic`; la respuesta trae del aprendiz `documentNumber`, `firstName` y `firstLastName`, más `stateAcademic` y la ficha (`id`, `code`).              | `Pendiente` |
| 9   | Mostrar la gestión de aprendices **solo a `ROLE_ADMIN`**; las lecturas (lista y detalle) aceptan `ROLE_ADMIN` o `ROLE_INSTRUCTOR` y responden `403` a los demás.                                                                                                                                                        | `Pendiente` |
| 10  | Adaptar el modelo y el reducer stock (`apprentice.model.ts`, `apprentice.reducer.ts`): quitar `stateAcademic` y `student` del alta, eliminar las mutaciones a los endpoints retirados y actualizar las pruebas que usen el payload viejo.                                                                                | `Pendiente` |

---

## UC017 — Consultar mis fichas y materias

**Estado del backend:** implementado. No hay endpoint dedicado de "mis fichas": la vinculación del instructor con una ficha nace de sus materias asignadas, así que las fichas se **derivan** de `GET /api/class-sections/mine`. Ese endpoint exige `ROLE_INSTRUCTOR` o `ROLE_ADMIN` y acepta el filtro opcional `gradeCode` (búsqueda parcial por número de ficha, sin distinguir mayúsculas y solo entre las materias del instructor). Ver [`docs/api-contracts.md#uc017--consultar-mis-fichas-y-materias`](./api-contracts.md#uc017--consultar-mis-fichas-y-materias) y [`docs/use-cases.md`](./use-cases.md) (UC017).

**Estado del frontend:** pendiente. La pantalla "Mis fichas" debe agrupar por ficha las materias que devuelve `/mine`: cada materia trae `id`, `subjectName`, `isActive` y la ficha anidada con `id`, `code`, `state`, `startDate`, `endDate` y `program { id, name }`; el `instructor` anidado solo trae `id` y `documentNumber` (es el propio instructor autenticado). El listado llega completo (**no es paginado**) y sin filtrar: las materias inactivas y las fichas no operativas vienen incluidas con su estado.

| #   | Ítem                                                                                                                                                                                                                                                                                                                                                                                            | Estado      |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Consumir `GET /api/class-sections/mine` y **derivar las fichas** agrupando sus materias por `grade.id`; no existe un endpoint de "mis fichas". No paginar: el arreglo llega completo.                                                                                                                                                                                                             | `Pendiente` |
| 2   | Buscador por número de ficha (flujo alternativo): enviar `?gradeCode=<texto>` y renderizar la respuesta del backend tal cual. La coincidencia es **parcial** y sin distinguir mayúsculas, y busca **solo entre las materias del instructor**; si no hay coincidencias el backend responde `200 []` y la UI muestra "No se encontraron fichas con ese número" (E1).                                  | `Pendiente` |
| 3   | Si el arreglo llega vacío **sin filtro**, mostrar "Aún no tienes materias asignadas. Contacta al Administrador." (E3); el backend responde `200 []` y no emite ninguna clave de error.                                                                                                                                                                                                           | `Pendiente` |
| 4   | Mostrar las fichas no operativas (`APLAZADA`, `CANCELADA` o `FINALIZADA`) con su estado correspondiente y sin acciones operativas (E2), y las materias con `isActive: false` tal cual: el backend no las filtra.                                                                                                                                                                                 | `Pendiente` |
| 5   | Mostrar la pantalla "Mis fichas" solo a `ROLE_INSTRUCTOR` o `ROLE_ADMIN`: `/mine` responde `403` al resto de los roles.                                                                                                                                                                                                                                                                          | `Pendiente` |
| 6   | Los textos de E1 y E3 los aporta el frontend: no viajan en `message` del backend, así que no deben mapearse como `error.<clave>` (no hay claves de error nuevas en el backend).                                                                                                                                                                                                                  | `Pendiente` |

---

## UC009 — Gestionar listas de asistencia

**Estado del backend:** implementado. El registro pasó de un CRUD plano a una **sesión por materia y fecha** (`PUT /api/attendances/session`) con guardado masivo e idempotente y sesión incompleta derivada (A5); la edición A2 es `PATCH /api/attendances/{id}`, el historial se consulta con filtros y los endpoints genéricos de alta/borrado responden `405`. El instructor solo marca `PRESENTE` o `FALLA` (`JUSTIFICADA` llega por UC010) y el backend bloquea fechas futuras, trimestres cerrados o fuera de vigencia, fechas fuera del rango de la ficha, fechas no lectivas, fichas sin matriculados y materias ajenas. Ver [`docs/api-contracts.md#uc009--gestionar-listas-de-asistencia`](./api-contracts.md#uc009--gestionar-listas-de-asistencia).

**Estado del frontend:** pendiente. **Cambios incompatibles:** `POST /api/attendances`, `PUT /api/attendances/{id}` y `DELETE /api/attendances/{id}` fueron **retirados** y responden `405`; el estado `TARDE` se eliminó del enum y fue migrado a `PRESENTE` (el formulario stock todavía lo ofrece); la sesión **no se rellena sola** con `PRESENTE` (los aprendices no enviados quedan sin registro y la sesión se muestra incompleta); y los `GET` de asistencia quedaron restringidos a `ROLE_ADMIN`, al instructor (que solo ve los registros de sus materias) o al aprendiz (que solo ve los suyos, UC011).

| #   | Ítem                                                                                                                                                                                                                                                                                                     | Estado      |
| --- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Reemplazar el CRUD genérico por la sesión: enviar `{ classSection: { id }, date, attendances: [{ studentId, stateAttendance }] }` a `PUT /api/attendances/session`. El `studentId` es el **id del `UserProfile`** (no el número de documento) y la respuesta trae `records`, `complete`, `enrolledCount` y `recordedCount`. | `Pendiente` |
| 2   | Presentar la lista con todos en `PRESENTE` por defecto y enviar al guardar **solo las marcaciones confirmadas**: el backend no rellena los ausentes. Un guardado parcial deja la sesión **incompleta** (A5); usar `complete`/`enrolledCount`/`recordedCount` para avisar y permitir reenviar los faltantes. Guardar de nuevo la misma sesión es idempotente. | `Pendiente` |
| 3   | Quitar `TARDE` de la UI (enum, etiquetas y filtros): el instructor solo ofrece `PRESENTE` y `FALLA`; `JUSTIFICADA` es de solo lectura en este flujo (llega por UC010). | `Pendiente` |
| 4   | Editar un registro con `PATCH /api/attendances/{id}` enviando `{ id, stateAttendance }` con `PRESENTE` o `FALLA`; solo aplica a materias propias y con el trimestre activo. | `Pendiente` |
| 5   | Historial (A1): `GET /api/attendances` paginado (`page`/`size`, `X-Total-Count`/`Link`, 20 por defecto) con filtros opcionales `classSectionId`, `date`, `studentId` y `stateAttendance`; el detalle fuera del alcance del instructor responde `404`. | `Pendiente` |
| 6   | Ocultar las acciones de crear y eliminar registro: los endpoints retirados responden `405`; la asistencia solo se registra por sesión y no se elimina. | `Pendiente` |
| 7   | Gestionar las **fechas no lectivas (A4)** con `/api/class-exceptions` (contrato en UC015): el instructor opera solo sobre sus materias y una fecha pasada es un precedente que no se puede crear, mover ni eliminar (solo cambiar el motivo). | `Pendiente` |
| 8   | Mapear las guardas de la sesión a mensajes de UI (ver claves nuevas abajo) y no ofrecer fechas futuras, no lectivas, de trimestres cerrados o fuera de la ficha/trimestre. | `Pendiente` |

### Claves i18n (`src/main/webapp/i18n/es/`)

| Clave                                | Texto esperado (sugerido)                                                                                     | Dónde se usa                                                              | Estado     |
| ------------------------------------ | ------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- | ---------- |
| `error.futureSessionDate`            | "No puedes registrar asistencia para fechas futuras."                                                          | Registro de sesión (UC009-E3).                                            | **Falta**  |
| `error.dateOutOfTrimester`           | "La fecha seleccionada está fuera del trimestre vigente."                                                      | Registro de sesión (UC009-E2).                                            | **Falta**  |
| `error.dateOutOfGradeRange`          | "La fecha está fuera del rango de fechas de la ficha."                                                         | Registro de sesión (UC009-E2).                                            | **Falta**  |
| `error.nonTeachingDate`              | "Esta fecha está marcada como no lectiva, no se puede registrar asistencia."                                   | Registro de sesión (UC009-E4).                                            | **Falta**  |
| `error.noActiveApprentices`          | "No hay aprendices activos en esta ficha."                                                                     | Registro de sesión (UC009-E5).                                            | **Falta**  |
| `error.invalidAttendanceState`       | "La asistencia solo se puede registrar o editar como Presente o Falla."                                        | Registro de sesión y edición A2 (UC009).                                  | **Falta**  |
| `error.studentNotEnrolled`           | "El aprendiz no está matriculado en esta ficha."                                                               | Registro de sesión (UC009).                                               | **Falta**  |
| `error.classSectionWithoutInstructor` | "Esta materia no tiene instructor asignado. Contacta al Administrador."                                        | Registro de sesión (UC009-E6).                                            | **Falta**  |
| `error.pastExceptionLocked`          | "Una fecha no lectiva pasada solo puede modificar su motivo: el precedente no se elimina."                     | Fechas no lectivas (UC009-A4).                                            | **Falta**  |
| `error.notYourClassSection`          | "Solo el instructor asignado a la materia puede gestionar este registro."                                      | Sesión, edición y fechas no lectivas (UC009); **compartida con UC015**.   | **Falta**  |
| `error.trimesterClosed`              | "No se puede modificar: el trimestre ya fue cerrado." (neutralizar el texto actual, que solo habla de horarios). | Asistencia (UC009-E1) y horarios (UC015-E6); **clave compartida**.        | **Falta**  |

---

## UC011 — Gestionar asistencia (Aprendiz)

**Estado del backend:** implementado. El aprendiz ya puede **leer sus propias asistencias** (`GET /api/attendances` acepta `ROLE_APPRENTICE` y acota la respuesta a sus registros; con `stateAttendance=FALLA` obtiene las fallas a justificar) y gestionar sus justificaciones de extremo a extremo: alta con una parte `PENDIENTE` por materia, marca de plazo calculada por el servidor (`onTime`), cupo por tipo, edición y cancelación mientras siga pendiente, y subsanación de las partes rechazadas. Ver [`docs/api-contracts.md#uc011--gestionar-asistencia-aprendiz`](./api-contracts.md#uc011--gestionar-asistencia-aprendiz).

**Estado del frontend:** pendiente. **Cambios incompatibles:** `DELETE /api/justifications/{id}` fue **retirado** (responde `405`; la cancelación es `PATCH /api/justifications/cancelled` con el `id` en el body); el request de `POST`/`PUT`/`PATCH` ahora lleva `detailses: [{ classSection: { id } }]`; la respuesta incorpora el mark `onTime` calculado por el servidor; `StateJustification` suma `CANCELADA`; y las lecturas y escrituras quedan **acotadas al aprendiz autenticado** (una justificación o parte ajena responde `404` en lectura y `400 error.notYourJustification` en escritura). El frontend stock sigue usando el CRUD genérico (incluido el `DELETE` retirado), no envía `detailses` y no muestra `onTime` ni `CANCELADA`.

| #   | Ítem                                                                                                                                                                                                                                                                                                                                          | Estado      |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Historial de asistencia (paso 1): consumir `GET /api/attendances` como aprendiz (paginado, 20 por defecto) y filtrar por `classSectionId`, `date` y `stateAttendance=FALLA` para que el aprendiz seleccione las fallas a justificar. El backend acota la lectura a sus propios registros; `studentId` es el id del `UserProfile`.               | `Pendiente` |
| 2   | Alta: enviar `{ description, startDate, endDate, evidence?, evidenceContentType, detailses: [{ classSection: { id } }], justificationType: { id }, student: { id } }`. El servidor crea una parte `PENDIENTE` por materia; no enviar `onTime`.                                                                                                    | `Pendiente` |
| 3   | Mostrar la marca `onTime` de la respuesta como "en tiempo"/"fuera de tiempo" **sin bloquear** el envío: la fecha límite son los días hábiles configurados contados desde el día hábil siguiente a la última falla cubierta (lunes a viernes, sin calendario de festivos).                                                                      | `Pendiente` |
| 4   | Manejar el cupo agotado: `400 error.quotaExceeded` (E6), mostrando los días restantes que viajan en `title`/`detail`; el consumo se calcula por **días con falla (fechas distintas)** del mismo tipo y aprendiz en `PENDIENTE`/`ACEPTADA`, y las `RECHAZADA`/`CANCELADA` liberan cupo.                                                          | `Pendiente` |
| 5   | Manejar las guardas de creación: `400 error.justificationTypeInactive` (tipo inactivo), `400 error.noFailuresFound` (E4), `400 error.notMatriculado` (E8), `400 error.invalidEvidence` (E1: PDF/imagen ≤5 MB) y `400 error.datesorder` (inicio posterior al fin, clave compartida).                                                            | `Pendiente` |
| 6   | Manejar `400 error.trimesterClosed` (E7, clave compartida): no ofrecer fallas de trimestres cerrados.                                                                                                                                                                                                                                        | `Pendiente` |
| 7   | Edición: `PUT`/`PATCH /api/justifications/{id}` solo mientras todas las partes estén `PENDIENTE`; si alguna ya tiene decisión, el backend responde `400 error.alreadyProcessed` (E3) y recalcula plazo y cupo al guardar. No ofrecer edición sobre justificaciones decididas.                                                                  | `Pendiente` |
| 8   | Cancelación (A4): `PATCH /api/justifications/cancelled` con `{ id }` en el body (no usar el `DELETE` retirado); las partes pasan a `CANCELADA` y el cupo se libera. Pedir confirmación por ser definitiva.                                                                                                                                     | `Pendiente` |
| 9   | Subsanación (A5): `PATCH /api/justification-details/{id}` enviando **solo** `correctionText` y/o `correctionFileUrl` + `correctionFileUrlContentType`; la parte `RECHAZADA` vuelve a `PENDIENTE` dentro de los **2 días hábiles** desde el rechazo. Fuera de plazo responde `400 error.correctionExpired` (E5) y solo queda crear una justificación nueva. | `Pendiente` |
| 10  | Mostrar `CANCELADA` en el enum `StateJustification` y sus etiquetas; el backend ya la devuelve en las partes canceladas.                                                                                                                                                                                                                       | `Pendiente` |
| 11  | No ofrecer al instructor los recursos genéricos de justificaciones (`/api/justifications` y `/api/justification-details` le responden `403`): su bandeja y su decisión son las de UC010 (ver la sección UC010).                                                                                                                       | `Pendiente` |

### Claves i18n (`src/main/webapp/i18n/es/`)

| Clave                             | Texto esperado (sugerido)                                                                                          | Dónde se usa                                                                             | Estado    |
| --------------------------------- | ------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------- | --------- |
| `error.notYourJustification`      | "Solo puedes gestionar tus propias justificaciones."                                                               | Lectura/escritura de justificaciones y partes ajenas (UC011).                            | **Falta** |
| `error.alreadyProcessed`          | "Esta justificación ya fue procesada y no puede modificarse."                                                      | Edición/cancelación con decisión y subsanación de parte procesada (UC011-E3).            | **Falta** |
| `error.correctionExpired`         | "El plazo para subsanar esta justificación ha vencido."                                                            | Subsanación fuera de plazo (UC011-E5).                                                   | **Falta** |
| `error.quotaExceeded`             | "Ya alcanzaste el límite de días para este tipo de justificación en el trimestre. Te quedan [N] días disponibles." | Cupo agotado (UC011-E6); el detalle con los días viaja en la respuesta del backend.       | **Falta** |
| `error.justificationTypeInactive` | "El tipo de justificación no está activo."                                                                         | Alta de justificación con tipo inactivo o inexistente (UC011/UC016).                     | **Falta** |
| `error.noFailuresFound`           | "No hay fallas para justificar."                                                                                   | El rango no cubre fallas reales (UC011-E4).                                              | **Falta** |
| `error.notMatriculado`            | "Solo puedes justificar fallas de fichas en las que estás matriculado."                                            | Ficha no matriculada (UC011-E8).                                                         | **Falta** |
| `error.invalidEvidence`           | "Formato o tamaño de archivo no válido."                                                                           | Soporte que no es PDF/imagen o supera 5 MB (UC011-E1).                                   | **Falta** |
| `error.datesorder`                | "La fecha de fin no puede ser anterior a la fecha de inicio."                                                      | Rango invertido en justificaciones (UC011); **compartida** con UC007/UC014.              | **Falta** |
| `error.trimesterClosed`           | "No se puede modificar: el trimestre ya fue cerrado." (neutralizar el texto actual, que solo habla de horarios).   | Justificaciones (UC011-E7), asistencia (UC009-E1) y horarios (UC015-E6); **compartida**. | **Falta** |

---

## UC010 — Gestionar justificaciones

**Estado del backend:** implementado. El instructor consulta y decide las partes pendientes de sus materias: `GET /api/justification-details/pending` devuelve solo las materias asignadas (el Administrador ve todas), `GET /api/justification-details/{id}` le permite leer el detalle de una parte de sus materias con el soporte adjunto (`404` en las ajenas) y `PATCH /api/justification-details/{id}/decision` aplica la decisión (`ACEPTADA` o `RECHAZADA`). Ver [`docs/api-contracts.md#uc010--gestionar-justificaciones`](./api-contracts.md#uc010--gestionar-justificaciones).

**Estado del frontend:** pendiente. **Cambios incompatibles:** los endpoints de decisión son nuevos; la parte suma `requestDate`, `outOfTimeReason` y `lateDecision`; la cabecera recortada suma `justificationType` y el detalle agrega `justification.evidence`/`justification.evidenceContentType`; y el instructor **no** debe usar `/api/justifications` ni los endpoints genéricos de `/api/justification-details` (responden `403`), salvo `GET /api/justification-details/{id}` para las partes de sus materias.

| #   | Ítem                                                                                                                                                                                                                                                                                                                                                                                                | Estado      |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Bandeja (A1): consumir `GET /api/justification-details/pending` paginado (20 por defecto, `X-Total-Count`/`Link`, `sort=id,desc`) con los filtros `stateJustification` (por defecto `PENDIENTE`), `classSectionId` y `createdFrom`/`createdTo` sobre la fecha de solicitud; mostrar aprendiz (`justification.student`), período (`startDate`–`endDate`), materia (`classSection.subjectName`), `onTime`, `justificationType.name` y `requestDate`. | `Pendiente` |
| 2   | Decisión (paso 5): enviar `PATCH /api/justification-details/{id}/decision` con `{ stateJustification, rejectionReason?, outOfTimeReason? }`. El rechazo exige motivo (E1) y la aprobación de una justificación fuera de tiempo exige el motivo adicional (A2); no enviar `responseDate` ni `lateDecision`, que son del servidor.                                                                           | `Pendiente` |
| 3   | Mostrar `lateDecision` como marca de decisión demorada en el histórico; la marca no bloquea nada.                                                                                                                                                                                                                                                                                                    | `Pendiente` |
| 4   | Habilitar la bandeja y la decisión solo a `ROLE_INSTRUCTOR` (o `ROLE_ADMIN`); el resto de roles recibe `403`.                                                                                                                                                                                                                                                                                        | `Pendiente` |
| 5   | Mapear las claves de error de la decisión (ver abajo) y refrescar la bandeja tras decidir.                                                                                                                                                                                                                                                                                                          | `Pendiente` |
| 6   | Revisar el soporte antes de decidir (paso 4): al abrir la parte, pedir `GET /api/justification-details/{id}`; la respuesta trae `justification.evidence` (archivo en base64), `justification.evidenceContentType` y `justificationType`. Solo responde `200` para las partes de sus materias (`404` en las ajenas). **La bandeja `/pending` no trae el archivo**: hay que pedir el detalle.                  | `Pendiente` |

### Claves i18n (`src/main/webapp/i18n/es/`)

| Clave                           | Texto esperado (sugerido)                                      | Dónde se usa                                  | Estado    |
| ------------------------------- | -------------------------------------------------------------- | --------------------------------------------- | --------- |
| `error.rejectionReasonRequired` | "El motivo de rechazo es obligatorio."                         | Rechazo sin motivo (UC010-E1).                | **Falta** |
| `error.outOfTimeReasonRequired` | "Debes registrar el motivo de la aprobación fuera de tiempo."  | Aprobación fuera de tiempo (UC010-A2).        | **Falta** |
| `error.invalidDecisionState`    | "La decisión solo puede ser Aceptada o Rechazada."             | Estado de decisión inválido (UC010).          | **Falta** |

---

## UC018 — Gestionar notificaciones

**Estado del backend:** implementado. Bandeja in-app del usuario autenticado con filtros, paginación e indicador de no leídas, marcado de lectura individual y masivo, entrega real de las notificaciones de justificaciones y reenvío manual de credenciales del Administrador (E7 de UC006). Ver [`docs/api-contracts.md#uc018--gestionar-notificaciones`](./api-contracts.md#uc018--gestionar-notificaciones).

**Estado del frontend:** pendiente. **Cambios incompatibles:** los endpoints de la bandeja y del reenvío son nuevos; cada notificación pasa a tener dos estados (`read` y `estado`), la bandeja agrega `referenceType`/`referenceId` y el indicador de no leídas viaja en la cabecera `X-Unread-Count`.

| #   | Ítem                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | Estado      |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Bandeja (pasos 1–2): consumir `GET /api/notifications` paginado (20 por defecto, `X-Total-Count`/`Link`) con `sort=createdDate,desc` y los filtros `read`, `type`, `from` y `to`; mostrar tipo, mensaje, fecha y estado leída/no leída, destacando las no leídas; sin datos → "No tienes notificaciones" (E1).                                                                                                                                                                                  | `Pendiente` |
| 2   | Abrir una notificación (paso 3): enviar `PATCH /api/notifications/{id}/read`, refrescar el estado con la respuesta y, si `referenceType`/`referenceId` están presentes, navegar al detalle del objeto; sin referencia, mostrar solo el mensaje (E2). El `404` de una notificación ajena no debe confundirse con "no encontrada".                                                                                                                                                                  | `Pendiente` |
| 3   | Marcar todas (A1): `PATCH /api/notifications/read-all` y refrescar la bandeja.                                                                                                                                                                                                                                                                                                                                                                                                                 | `Pendiente` |
| 4   | Indicador de no leídas (A3): leer la cabecera `X-Unread-Count` de la bandeja y refrescarlo tras leer o marcar todas; no hay endpoint de conteo aparte.                                                                                                                                                                                                                                                                                                                                          | `Pendiente` |
| 5   | Reenvío del Administrador (E7 de UC006): en la gestión de usuarios, `PATCH /api/admin/users/resend-credentials` con `{ documentNumber }`; `200` con `AdminUserDTO` y `400 error.documentNumberNotFound` si el documento no existe. El backend genera el enlace de restablecimiento y cierra la notificación abierta como `ENVIADA` o `REINTENTAR`; el estado de entrega es interno y la respuesta no lo expone, así que la UI no debe prometer la entrega.                                          | `Pendiente` |

---

## UC013 — Gestionar alertas de inasistencia

**Estado del backend:** implementado. El sistema genera y resuelve alertas por fallas consecutivas (materia) y acumuladas (ficha) al guardar o modificar asistencia y al aprobar una justificación; el aprendiz y los instructores reciben el aviso por la bandeja de UC018. Los umbrales (`consecutiveAbsenceAlertThreshold`, default 3; `accumulatedAbsenceAlertThreshold`, default 5) ya se consumen desde la configuración global (UC019). Ver [`docs/api-contracts.md#uc013--gestionar-alertas-de-inasistencia`](./api-contracts.md#uc013--gestionar-alertas-de-inasistencia).

**Estado del frontend:** pendiente. **Cambios incompatibles:** los endpoints de alertas son nuevos y el módulo stock `desertion-counter` sigue en el proyecto aunque ya no tiene backend; el aprendiz **no** consume `/api/alerts` (responde `403`): sus avisos llegan por la bandeja de UC018.

| #   | Ítem                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | Estado      |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Bandeja (A1): consumir `GET /api/alerts` paginado (20 por defecto, `sort=generatedAt,desc`, `X-Total-Count`/`Link`) con los filtros `type`, `state`, `gradeId`, `studentId`, `from` y `to`; mostrar aprendiz, tipo, estado, conteo y umbral, materia (o ficha cuando sea `ACUMULADAS`) y fecha, destacando las `NO_LEIDA`.                                                                                                                                                                                                      | `Pendiente` |
| 2   | Detalle y lectura (A2): abrir con `GET /api/alerts/{id}` y marcar leída con `PATCH /api/alerts/{id}/read` (idempotente); una alerta ajena responde `404`, que no debe confundirse con "no encontrada".                                                                                                                                                                                                                                                                                                                          | `Pendiente` |
| 3   | Atender (A3): `PATCH /api/alerts/{id}/attend` con `{ observation }` obligatoria (máximo 300 caracteres). Una alerta resuelta automáticamente responde `400 error.alertAlreadyResolved`: no ofrecer la acción sobre las `RESUELTA_AUTOMATICAMENTE`.                                                                                                                                                                                                                                                                              | `Pendiente` |
| 4   | Historial (A5): `GET /api/alerts/students/{studentId}` paginado.                                                                                                                                                                                                                                                                                                                                                                                                                                                                | `Pendiente` |
| 5   | Mostrar los estados `NO_LEIDA`, `LEIDA`, `ATENDIDA` y `RESUELTA_AUTOMATICAMENTE`, y los tipos `CONSECUTIVAS`/`ACUMULADAS`; en las alertas acumuladas `classSection` llega en `null` y `grade` siempre viaja.                                                                                                                                                                                                                                                                                                                    | `Pendiente` |
| 6   | Alcance por rol: la bandeja y las acciones son de `ROLE_ADMIN` o `ROLE_INSTRUCTOR`; el instructor solo ve las `CONSECUTIVAS` de sus materias y las `ACUMULADAS` de sus fichas, y el Administrador ve todo. El aprendiz recibe `403`: ocultarle la pantalla y mostrarle sus avisos en la bandeja de UC018.                                                                                                                                                                                                                       | `Pendiente` |
| 7   | **Eliminar el módulo stock de desertion-counter** (lo reemplazan las alertas): el backend se eliminó en el commit `e2760e8` y la colección se dropea con la migración 015. Quitar `src/main/webapp/app/entities/desertion-counter/*` (listado, alta, detalle, borrado, reducer y spec), `src/main/webapp/app/shared/model/desertion-counter.model.ts`, su registro en `app/entities/menu.tsx`, `app/entities/routes.tsx` y `app/entities/reducers.ts`, y las claves de `i18n/es/desertionCounter.json` y `i18n/es/global.json`. | `Pendiente` |

### Claves i18n (`src/main/webapp/i18n/es/`)

| Clave                        | Texto esperado (sugerido)                                       | Dónde se usa                                | Estado    |
| ---------------------------- | --------------------------------------------------------------- | ------------------------------------------- | --------- |
| `error.alertAlreadyResolved` | "La alerta fue resuelta automáticamente y no se puede atender." | Atención de una alerta resuelta (UC013-A3). | **Falta** |

---

## Próximas UCs

Las secciones de arriba se irán agregando a medida que el backend avance y cada UC quede lista. La siguiente UC tiene backend **parcial** y el frontend puede ir adelantando trabajo contra su contrato:

| UC    | Nombre                          | Contrato                                              |
| ----- | ------------------------------- | ----------------------------------------------------- |
| UC023 | Consultar dashboard             | [`docs/api-contracts.md`](./api-contracts.md) — UC023 |

La única UC con backend parcial es UC023: sus paneles de Instructor y Aprendiz todavía devuelven los indicadores del Administrador. Con UC013 ya implementada (alertas de inasistencia, ver arriba), el tipo `ALERTA` y el job de reintentos de las notificaciones también están listos (UC018, ver arriba).

---

## Claves i18n pendientes

Tabla consolidada de textos a crear o corregir en `src/main/webapp/i18n/es/`. Los textos sugeridos son neutrales y deben validarse con el copy final del producto.

| Clave                          | Texto esperado (sugerido)                                                                           | Dónde se usa                                          |
| ------------------------------ | --------------------------------------------------------------------------------------------------- | ----------------------------------------------------- |
| `error.documentnumberexists`   | "Este documento ya está registrado en el sistema."                                                  | Registro (UC001-E1).                                  |
| `error.documentnumberinactive` | "La cuenta asociada a este documento está desactivada. Contacta al Administrador para reactivarla." | Registro (UC001-E1).                                  |
| `error.emailexists`            | "Este correo ya está en uso."                                                                       | Registro (UC001-E3). Revisar texto.                   |
| `error.emailrequired`          | "El correo electrónico es obligatorio."                                                             | Registro (UC001-E2).                                  |
| `error.documentTypeNotFound`   | "El tipo de documento no existe."                                                                   | Registro (UC001-E2).                                  |
| `error.documentTypeInactive`   | "El tipo de documento está inactivo y no puede usarse en el registro."                              | Registro (UC001-E2).                                  |
| `error.validation`             | "Error de validación en el servidor." (mantener; el detalle va por `fieldErrors`).                  | Registro y demás formularios.                         |
| `error.badcredentials`         | "Tipo o número de documento o contraseña incorrectos."                                              | Inicio de sesión (UC002-E1).                          |
| `error.accountinactive`        | "Tu cuenta está inactiva. Contacta al administrador."                                               | Inicio de sesión (UC002-E2).                          |
| `error.currentpasswordinvalid` | "La contraseña actual es incorrecta."                                                               | Cambio de contraseña (UC002/UC003-E4).                |
| `error.samepassword`           | "La nueva contraseña debe ser diferente a la actual."                                               | Cambio de contraseña (UC003-E6).                      |
| `error.invalidpassword`        | "Contraseña no válida."                                                                             | Cambio de contraseña y registro (UC003-E5, UC001-E1) y reset (UC005-E2). |
| `error.resetlinkinvalid`       | "El enlace no es válido. Solicita uno nuevo."                                                       | Recuperación de contraseña (UC005-E1).               |
| `error.resetlinkexpired`       | "El enlace ha expirado, solicita uno nuevo."                                                        | Recuperación de contraseña (UC005-E3).               |
| `error.resetlinkused`          | "Este enlace ya no es válido."                                                                      | Recuperación de contraseña (UC005-E4).               |
| `error.documentimmutable`      | "Este dato no puede modificarse."                                                                   | Edición de perfil (UC003-E3).                         |
| `error.timeSlotNameAlreadyUsed` | "Ya existe una jornada con este nombre."                                                           | Alta/edición de jornada (UC020-E1).                   |
| `error.timeSlotSameTime`       | "La hora de inicio y la hora de fin no pueden ser iguales."                                        | Alta/edición de jornada (UC020-E2).                   |
| `error.timeSlotInUse`          | "No es posible eliminar la jornada: está asignada a fichas. Puedes desactivarla."                  | Eliminación de jornada (UC020-E3).                    |
| `error.modalityNameAlreadyUsed` | "Ya existe una modalidad con este nombre."                                                          | Alta/edición de modalidad (UC021-E1).                 |
| `error.modalityInUse`          | "No es posible eliminar la modalidad: está asignada a fichas. Puedes desactivarla."                | Eliminación de modalidad (UC021-E2).                  |
| `error.documentTypeNameAlreadyUsed`     | "Ya existe un tipo de documento con este nombre."                                                  | Alta/edición de tipo de documento (UC022-E1).          |
| `error.documentTypeInitialsAlreadyUsed` | "Ya existe un tipo de documento con estas iniciales."                                              | Alta/edición de tipo de documento (UC022-E2).          |
| `error.documentTypeInitialsInUse`       | "No se pueden modificar las iniciales de un tipo de documento en uso."                             | Edición de tipo de documento (UC022-E3).               |
| `error.documentTypeInUse`               | "No es posible eliminar el tipo de documento: está en uso por usuarios. Puedes desactivarlo."      | Eliminación de tipo de documento (UC022-E4).           |
| `error.justificationTypeNameAlreadyUsed` | "Ya existe un tipo de justificación con este nombre."                                              | Alta/edición de tipo de justificación (UC016-E1).      |
| `error.justificationTypeInUse`          | "No es posible eliminar el tipo: ya fue usado en justificaciones. Puedes desactivarlo."            | Eliminación de tipo de justificación (UC016-E3).       |
| `error.adminprotected`         | "La cuenta admin está protegida y no puede desactivarse."                                          | Desactivar/degradar al super admin (UC006-E6).         |
| `error.rolenotfound`           | "Rol no válido."                                                                                   | Crear/editar usuario con un rol no asignable (UC006).  |
| `error.documentNumberNotFound` | "No existe un usuario con ese número de documento."                                                | Activación y reenvío de credenciales del Administrador (UC006-E7). |
| `error.trimestersoutofrange`   | "La cantidad de trimestres debe estar entre 1 y 12."                                               | Alta/edición de programa (UC012-E6).                   |
| `error.codenotnumeric`         | "El código debe contener solo números."                                                            | Alta/edición de programa (UC012-E5) y de ficha (UC007-E4). |
| `error.programInUse`           | "No es posible eliminar el programa: tiene fichas asociadas. Puedes desactivarlo."                 | Eliminación de programa (UC012-E8).                    |
| `error.trimesterInUse`         | "No es posible eliminar el trimestre: tiene horarios o asistencias registradas."                   | Eliminación de trimestre (UC014-E6).                   |
| `trimesterStateFuture`         | "Futuro"                                                                                            | Etiqueta del estado del trimestre (UC014).             |
| `trimesterStateActive`         | "Activo"                                                                                            | Etiqueta del estado del trimestre (UC014).             |
| `trimesterStateClosed`         | "Cerrado"                                                                                           | Etiqueta del estado del trimestre (UC014).             |
| `error.gradeCodeAlreadyUsed`   | "El código de ficha ya está en uso."                                                                 | Alta/edición de ficha (UC007-E1).                      |
| `error.datesorder`             | "La fecha de fin no puede ser anterior a la fecha de inicio."                                        | Fichas (UC007), trimestres (UC014) y justificaciones (UC011); clave compartida, el texto debe servir para los tres formularios. |
| `error.startdateinpast`        | "La fecha de inicio no puede ser anterior a hoy."                                                    | Alta/edición de ficha (UC007).                         |
| `error.programInactive`        | "No se pueden crear fichas para un programa inactivo."                                              | Alta/edición de ficha (UC007-E3).                      |
| `error.modalityInactive`       | "No se pueden crear fichas para una modalidad inactiva."                                            | Alta/edición de ficha (UC007-E3).                      |
| `error.timeSlotInactive`       | "No se pueden crear fichas para una jornada inactiva."                                              | Alta/edición de ficha (UC007-E3).                      |
| `error.noteditable`            | "No se puede modificar: el registro está cerrado o finalizado."                                     | Trimestres cerrados (UC014) y fichas finalizadas (UC007); clave compartida. |
| `error.fieldlocked`            | "El campo no se puede modificar en el estado actual de la ficha."                                   | Edición de ficha (UC007-A1).                           |
| `error.gradeCodeLocked`        | "El código solo puede cambiarse mientras la ficha no tenga materias ni aprendices."                 | Edición de ficha (UC007-A1).                           |
| `error.invalidtransition`      | "La ficha no se puede aplazar, reanudar o cancelar en su estado actual."                            | Acciones de ficha (UC007-A2/A3).                       |
| `error.gradeInUse`             | "No es posible eliminar la ficha: tiene aprendices vinculados y/o registros de asistencia. Si desea retirarla de operación, use Cancelar ficha." | Eliminación de ficha (UC007).                          |
| `error.instructorInactive`     | "El instructor seleccionado ya no está disponible, selecciona otro."                                | Asignación de instructor (UC015-E7).                   |
| `error.classSectionNameAlreadyUsed` | "Ya existe una materia con este nombre en esta ficha."                                        | Alta/edición de materia (UC015-E2).                    |
| `error.scheduleCrossesMidnight` | "La sesión debe iniciar y terminar el mismo día."                                                  | Alta/edición de horario (UC015-E5).                    |
| `error.scheduleOutOfTimeSlot`  | "El horario debe estar dentro de la jornada de la ficha."                                          | Alta/edición de horario (UC015-E3).                    |
| `error.scheduleOverlap`        | "El horario se solapa con otro horario de la ficha en ese trimestre."                              | Alta/edición de horario (UC015-E4).                    |
| `error.gradeNotOperable`       | "La ficha no permite esta operación en su estado actual."                                            | Vinculación/desvinculación de aprendiz (UC008-E3) y alta/edición de materia (UC015-E1); **clave compartida**, el texto debe servir para ambos formularios. |
| `error.trimesterClosed`        | "No se puede modificar: el trimestre ya fue cerrado."                                              | Justificaciones (UC011-E7), asistencia (UC009-E1) y horarios (UC015-E6); **clave compartida**, el texto debe servir para los tres contextos. |
| `error.classSectionInUse`      | "No es posible eliminar la materia: tiene registros de asistencia. Puedes desactivarla para retirarla de operación." | Eliminación de materia (UC015-A3).                     |
| `error.apprenticeInactive`     | "Aprendiz no existe o no está activo."                                                              | Vinculación de aprendiz (UC008-E1).                    |
| `error.apprenticeAlreadyEnrolled` | "Este aprendiz ya tiene un registro en esta ficha."                                              | Vinculación de aprendiz (UC008-E2).                    |
| `error.invalidunlinkreason`    | "El motivo de desvinculación no es válido."                                                         | Desvinculación de aprendiz (UC008-A1).                 |
| `error.notYourClassSection`    | "Solo el instructor asignado a la materia puede gestionar este registro."                           | Sesión, edición y fechas no lectivas (UC009); **compartida con UC015**. |
| `error.classSectionWithoutInstructor` | "Esta materia no tiene instructor asignado. Contacta al Administrador."                      | Registro de sesión (UC009-E6).                         |
| `error.futureSessionDate`      | "No puedes registrar asistencia para fechas futuras."                                               | Registro de sesión (UC009-E3).                         |
| `error.dateOutOfTrimester`     | "La fecha seleccionada está fuera del trimestre vigente."                                           | Registro de sesión (UC009-E2).                         |
| `error.dateOutOfGradeRange`    | "La fecha está fuera del rango de fechas de la ficha."                                              | Registro de sesión (UC009-E2).                         |
| `error.nonTeachingDate`        | "Esta fecha está marcada como no lectiva, no se puede registrar asistencia."                        | Registro de sesión (UC009-E4).                         |
| `error.noActiveApprentices`    | "No hay aprendices activos en esta ficha."                                                          | Registro de sesión (UC009-E5).                         |
| `error.invalidAttendanceState` | "La asistencia solo se puede registrar o editar como Presente o Falla."                             | Registro de sesión y edición A2 (UC009).               |
| `error.studentNotEnrolled`     | "El aprendiz no está matriculado en esta ficha."                                                    | Registro de sesión (UC009).                            |
| `error.pastExceptionLocked`    | "Una fecha no lectiva pasada solo puede modificar su motivo: el precedente no se elimina."          | Fechas no lectivas (UC009-A4).                         |
| `error.notYourJustification`   | "Solo puedes gestionar tus propias justificaciones."                                                | Justificaciones y partes ajenas (UC011).               |
| `error.alreadyProcessed`       | "Esta justificación ya fue procesada y no puede modificarse."                                       | Edición/cancelación con decisión y subsanación de parte procesada (UC011-E3). |
| `error.correctionExpired`      | "El plazo para subsanar esta justificación ha vencido."                                             | Subsanación fuera de plazo (UC011-E5).                 |
| `error.quotaExceeded`          | "Ya alcanzaste el límite de días para este tipo de justificación en el trimestre. Te quedan [N] días disponibles." | Cupo agotado (UC011-E6).                               |
| `error.justificationTypeInactive` | "El tipo de justificación no está activo."                                                      | Alta con tipo inactivo o inexistente (UC011/UC016).    |
| `error.noFailuresFound`        | "No hay fallas para justificar."                                                                    | Rango sin fallas cubiertas (UC011-E4).                 |
| `error.notMatriculado`         | "Solo puedes justificar fallas de fichas en las que estás matriculado."                             | Ficha no matriculada (UC011-E8).                       |
| `error.invalidEvidence`        | "Formato o tamaño de archivo no válido."                                                            | Soporte inválido (UC011-E1).                           |
| `error.rejectionReasonRequired` | "El motivo de rechazo es obligatorio."                                                             | Rechazo sin motivo (UC010-E1).                         |
| `error.outOfTimeReasonRequired` | "Debes registrar el motivo de la aprobación fuera de tiempo."                                      | Aprobación de una justificación fuera de tiempo (UC010-A2). |
| `error.invalidDecisionState`   | "La decisión solo puede ser Aceptada o Rechazada."                                                  | Estado de decisión inválido (UC010).                   |
| `register.messages.success`    | "Registro exitoso. Ya puedes iniciar sesión." (quitar la mención a confirmación por correo).        | Toast de éxito del registro.                          |

Los textos de campos nuevos del formulario de registro (tipo de documento, número de documento, primer nombre, segundo nombre, primer apellido, segundo apellido, teléfono) son decisión del frontend: definir sus claves i18n junto con el formulario de UC001.
