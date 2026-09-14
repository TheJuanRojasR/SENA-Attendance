# Trabajo pendiente del frontend — SENA Attendance

Este archivo es el **seguimiento vivo del frontend**: describe lo que la interfaz debe hacer y consumir para cumplir cada caso de uso, a medida que el backend avanza. La fuente de verdad del contrato HTTP es [`docs/api-contracts.md`](./api-contracts.md); las reglas de negocio y los flujos están en [`docs/use-cases.md`](./use-cases.md). Aquí no se repite el JSON de los contratos: se referencia su sección.

- **Propietario:** el desarrollador de frontend.
- **Mantenimiento:** se actualiza a medida que el backend avanza; cada UC se agrega cuando su backend está listo. El backend no cambia para acomodar al frontend: el frontend se adapta al contrato.
- **Estado actual:** UC001 tiene backend implementado y verificado; el frontend sigue en su mayor parte con los formularios stock de JHipster.

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
| Cuenta actual     | `GET /api/account` devuelve solo un `AdminUserDTO` (cuenta: `id`, `login`, `email`, `activated`, `langKey`, `authorities`, auditoría), **sin nombres, apellidos ni documento del perfil**. Queda **por confirmar** cómo obtiene el front el perfil actual.                                                   |
| Roles             | El sistema emite `ROLE_ADMIN`, `ROLE_INSTRUCTOR`, `ROLE_APPRENTICE` y `ROLE_USER` (los tres primeros siempre acompañados de `ROLE_USER`).                                                                                                                                                                    |
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

**Estado del backend:** parcial. Ver [`docs/api-contracts.md#uc003--modificar-datos`](./api-contracts.md#uc003--modificar-datos).

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

**Estado del backend:** parcial. El enlace de recuperación es de **un solo uso** y vence a los **30 minutos** (ver [`docs/api-contracts.md#uc005--recuperar-contraseña`](./api-contracts.md#uc005--recuperar-contraseña)). Al completar el reset, el backend **limpia** `mustChangePassword` si estaba activo (el usuario eligió su propia contraseña), por lo que no debe forzarse la pantalla de cambio obligatorio después de un reset.

| #   | Ítem                                                                                                                                                                            | Estado      |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | **E3 — Enlace expirado:** si el usuario abre el enlace después de los **30 minutos**, el backend responde `400` con `error.resetlinkexpired`; mostrar "El enlace ha expirado, solicita uno nuevo" y redirigirlo al paso 1 (solicitud). | `Pendiente` |
| 2   | **E2 — Contraseña débil:** si la nueva contraseña no cumple la política completa (8–20 con mayúscula, minúscula, número y carácter especial), el backend responde `400` con `error.invalidpassword`. Mostrar "Contraseña no válida" y conservar el enlace para reintentar. | `Pendiente` |
| 3   | **E4 — Enlace ya usado:** al reutilizar un enlace consumido el backend responde `400` con `error.resetlinkused`; mostrar "Este enlace ya no es válido" y redirigir a solicitar uno nuevo. | `Pendiente` |
| 4   | **E1 — Enlace inválido:** si el enlace no corresponde a ninguna solicitud, el backend responde `400` con `error.resetlinkinvalid`; mostrar "El enlace no es válido. Solicita uno nuevo". | `Pendiente` |

---

## UC019 — Gestionar configuración global

**Estado del backend:** parcial. La configuración global es un **singleton** con `id` fijo `global-configuration` y ya expone los **cuatro** parámetros del UC: `studentJustificationDays` (default 5), `instructorResponseDays` (default 2), `consecutiveAbsenceAlertThreshold` (default 3) y `accumulatedAbsenceAlertThreshold` (default 5). Ver [`docs/api-contracts.md#uc019--gestionar-configuración-global`](./api-contracts.md#uc019--gestionar-configuración-global).

**Estado del frontend:** pendiente. La pantalla de configuración debe editar y guardar los cuatro parámetros. Los endpoints de configuración (`GET` y `PATCH /api/global-configurations`) requieren el rol `ROLE_ADMIN`.

| #   | Ítem                                                                                                                                                                                                                                              | Estado      |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | La pantalla de configuración global debe mostrar los **dos umbrales de alerta de inasistencia** (`consecutiveAbsenceAlertThreshold`, default 3; `accumulatedAbsenceAlertThreshold`, default 5), además de `studentJustificationDays` e `instructorResponseDays`. | `Pendiente` |
| 2   | Validar en el cliente que los plazos `studentJustificationDays` e `instructorResponseDays` sean enteros entre **1 y 30 días**; fuera de ese rango el backend responde `400 error.validation` con una entrada en `fieldErrors` y no persiste el cambio. | `Pendiente` |
| 3   | Validar en el cliente que cada umbral (`consecutiveAbsenceAlertThreshold`, `accumulatedAbsenceAlertThreshold`) sea un entero `>= 1` (sin máximo); si no lo es, el backend responde `400 error.validation` con una entrada en `fieldErrors` y no persiste el cambio. | `Pendiente` |
| 4   | El `PATCH /api/global-configurations` es parcial: se puede enviar solo los campos que cambian, **sin `id`** (el backend siempre apunta al singleton `global-configuration`); los campos omitidos quedan igual. Si se envía un `id` distinto, responde `400 error.idinvalid`. | `Pendiente` |

---

## UC020 — Gestionar jornadas

**Estado del backend:** parcial. Ya está implementado el **nombre único** (E1): el backend recorta el nombre y lo compara sin distinguir mayúsculas; un duplicado responde `400` con `error.timeSlotNameAlreadyUsed`, y un nombre vacío o en blanco responde `400 error.validation` con una entrada en `fieldErrors`. También está implementado el **rechazo de horas iguales** (E2): si `startTime` y `endTime` son iguales el backend responde `400` con `error.timeSlotSameTime`, mientras que un rango que cruza medianoche (`endTime` menor que `startTime`, p. ej. 22:00–06:00) se permite. También está implementado el **bloqueo de eliminación** (E3): si una o más fichas usan la jornada, `DELETE /api/time-slots/{id}` responde `400` con `error.timeSlotInUse`; en ese caso la jornada no se elimina y debe **desactivarse** con `PATCH /api/time-slots` (`isActive: false`). El listado `GET /api/time-slots` está **paginado** (`page`/`size`/`sort`, 20 por defecto) y devuelve `X-Total-Count`/`Link`; `GET /api/time-slots/active` sigue sin paginar. La **escritura** (`POST`/`PUT`/`PATCH`/`DELETE`) quedó restringida a `ROLE_ADMIN`: los demás roles autenticados reciben `403`; la **lectura** sigue disponible para cualquier usuario autenticado. Ver [`docs/api-contracts.md#uc020--gestionar-jornadas`](./api-contracts.md#uc020--gestionar-jornadas).

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

**Estado del backend:** parcial. Ya está implementado el **nombre único** (E1): el backend recorta el nombre y lo compara sin distinguir mayúsculas; un duplicado responde `400` con `error.modalityNameAlreadyUsed`, y un nombre vacío o en blanco responde `400 error.validation` con una entrada en `fieldErrors`. Al crear, la modalidad nace **Activa**: el backend fuerza `isActive = true` e ignora el valor enviado. También está implementado el **bloqueo de eliminación** (E2): si una o más fichas usan la modalidad, `DELETE /api/modalities/{id}` responde `400` con `error.modalityInUse`; en ese caso la modalidad no se elimina y debe **desactivarse** con `PATCH /api/modalities` (`isActive: false`). El listado `GET /api/modalities` está **paginado** (`page`/`size`/`sort`, 20 por defecto) y devuelve `X-Total-Count`/`Link`; `GET /api/modalities/active` sigue sin paginar. La **escritura** (`POST`/`PUT`/`PATCH`/`DELETE`) quedó restringida a `ROLE_ADMIN`: los demás roles autenticados reciben `403`; la **lectura** sigue disponible para cualquier usuario autenticado. Ver [`docs/api-contracts.md#uc021--gestionar-modalidades`](./api-contracts.md#uc021--gestionar-modalidades).

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

**Estado del backend:** implementado (salvo el **reenvío de credenciales E7**, que llega con UC018). Un solo rol por cuenta; solo se pueden asignar Administrador, Instructor o Aprendiz. El **cambio de rol** respeta las guardas de último administrador, último instructor y cuenta `admin` protegida. El **login se recalcula** al corregir el tipo o el número de documento. **Los usuarios nunca se eliminan.** Ver [`docs/api-contracts.md#uc006--gestionar-perfiles`](./api-contracts.md#uc006--gestionar-perfiles).

**Estado del frontend:** pendiente.

| #   | Ítem                                                                                                                                                                                                                                    | Estado      |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| 1   | Quitar la acción de **eliminar usuario**: el endpoint `DELETE /api/admin/users/{login}` fue retirado y responde `405`; usar desactivar/reactivar (`PATCH /api/admin/users/activated`).                                                    | `Pendiente` |
| 2   | Manejar las claves de guardas: `400 error.adminprotected` (cuenta admin protegida), `400 error.lastAdmin` (último administrador activo), `400 error.lastInstructor` (último instructor de materias de fichas operativas) y `400 error.rolenotfound`. | `Pendiente` |
| 3   | En `PATCH /api/admin/users` enviar el `id` en el body; el `rol` solo admite `ROLE_ADMIN`, `ROLE_INSTRUCTOR` o `ROLE_APPRENTICE` (sin Coordinador).                                                                                       | `Pendiente` |
| 4   | Refrescar el **login derivado** que se muestra cuando el Administrador corrige el tipo o el número de documento.                                                                                                                        | `Pendiente` |
| 5   | La búsqueda `GET /api/admin/users/search` admite también el parámetro `role` y pagina con `X-Total-Count`/`Link` (20 por defecto).                                                                                                       | `Pendiente` |
| 6   | En el alta no enviar estado: la cuenta nace `mustChangePassword = true`; el **reenvío de credenciales (E7)** depende de UC018.                                                                                                            | `Pendiente` |

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

## Próximas UCs

Las secciones de arriba se irán agregando a medida que el backend avance y cada UC quede lista. Las siguientes UCs ya tienen backend **parcial** y el frontend puede ir adelantando trabajo contra su contrato:

| UC    | Nombre                             | Contrato                                              |
| ----- | ---------------------------------- | ----------------------------------------------------- |
| UC006 | Gestionar perfiles                 | [`docs/api-contracts.md`](./api-contracts.md) — UC006 |
| UC007 | Gestionar fichas                   | [`docs/api-contracts.md`](./api-contracts.md) — UC007 |
| UC008 | Gestionar aprendices               | [`docs/api-contracts.md`](./api-contracts.md) — UC008 |
| UC009 | Gestionar listas de asistencia     | [`docs/api-contracts.md`](./api-contracts.md) — UC009 |
| UC010 | Gestionar justificaciones          | [`docs/api-contracts.md`](./api-contracts.md) — UC010 |
| UC011 | Gestionar asistencia (Aprendiz)    | [`docs/api-contracts.md`](./api-contracts.md) — UC011 |
| UC012 | Gestionar programas de aprendizaje | [`docs/api-contracts.md`](./api-contracts.md) — UC012 |
| UC014 | Gestionar trimestres académicos    | [`docs/api-contracts.md`](./api-contracts.md) — UC014 |
| UC015 | Gestionar materias                 | [`docs/api-contracts.md`](./api-contracts.md) — UC015 |
| UC016 | Gestionar tipos de justificación   | [`docs/api-contracts.md`](./api-contracts.md) — UC016 |
| UC017 | Consultar mis fichas y materias    | [`docs/api-contracts.md`](./api-contracts.md) — UC017 |
| UC019 | Gestionar configuración global     | [`docs/api-contracts.md`](./api-contracts.md) — UC019 |
| UC020 | Gestionar jornadas                 | [`docs/api-contracts.md`](./api-contracts.md) — UC020 |
| UC021 | Gestionar modalidades              | [`docs/api-contracts.md`](./api-contracts.md) — UC021 |
| UC022 | Gestionar tipos de documento       | [`docs/api-contracts.md`](./api-contracts.md) — UC022 |
| UC023 | Consultar dashboard                | [`docs/api-contracts.md`](./api-contracts.md) — UC023 |

UC013 (alertas de inasistencia) y UC018 (notificaciones) están **no implementadas** en el backend y no se listan aquí hasta que su contrato exista.

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
| `error.trimestersoutofrange`   | "La cantidad de trimestres debe estar entre 1 y 12."                                               | Alta/edición de programa (UC012-E6).                   |
| `error.codenotnumeric`         | "El código debe contener solo números."                                                            | Alta/edición de programa (UC012-E5).                   |
| `error.programInUse`           | "No es posible eliminar el programa: tiene fichas asociadas. Puedes desactivarlo."                 | Eliminación de programa (UC012-E8).                    |
| `error.trimesterInUse`         | "No es posible eliminar el trimestre: tiene horarios o asistencias registradas."                   | Eliminación de trimestre (UC014-E6).                   |
| `register.messages.success`    | "Registro exitoso. Ya puedes iniciar sesión." (quitar la mención a confirmación por correo).        | Toast de éxito del registro.                          |

Los textos de campos nuevos del formulario de registro (tipo de documento, número de documento, primer nombre, segundo nombre, primer apellido, segundo apellido, teléfono) son decisión del frontend: definir sus claves i18n junto con el formulario de UC001.
