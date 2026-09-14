# Contratos de API por caso de uso — SENA Attendance

Este documento describe, caso por caso, los contratos HTTP que el backend expone para los casos de uso UC001–UC023 definidos en [`docs/use-cases.md`](./use-cases.md). Es un **documento vivo**: cada UC se actualiza en el mismo cambio que modifica su contrato, y su estado indica el nivel de implementación. La fuente de verdad es el código en la rama `juanma`.

- **Mantenimiento:** cada sección de UC se actualiza en el mismo commit que cambia su contrato.

Fuentes verificadas:

- `src/main/java/com/mycompany/senaattendance/web/rest/*.java` — rutas, métodos, roles (`@PreAuthorize`) y códigos de estado.
- `src/main/java/com/mycompany/senaattendance/web/rest/vm/` y `service/dto/` — nombres y reglas de los campos de request/response.
- `src/main/java/com/mycompany/senaattendance/domain/` — modelo de datos.
- `src/main/java/com/mycompany/senaattendance/config/SecurityConfiguration.java` — endpoints públicos.
- `src/main/resources/config/application*.yml` — vigencia del token JWT.
- `docs/use-cases.md` — reglas de negocio y flujos de cada UC.

Cuándo este documento dice `por confirmar`, el dato no pudo determinarse con certeza a partir del código.

## Leyenda de estados

| Estado            | Significado                                                                                    |
| ----------------- | ---------------------------------------------------------------------------------------------- |
| `Implementado`    | Los endpoints del UC existen y las reglas principales del flujo están cubiertas en el backend. |
| `Parcial`         | Existen endpoints relacionados, pero faltan campos, validaciones, permisos o reglas del UC.    |
| `No implementado` | El UC no tiene endpoints en el backend actual.                                                 |

## Convenciones generales

- **Base path:** todos los endpoints viven bajo `/api`. Las rutas de este documento son absolutas, por ejemplo `/api/register`.
- **Autenticación:** JWT. El login es `POST /api/authenticate` y devuelve el token en el cuerpo (`id_token`) y en la cabecera `Authorization`. Las peticiones autenticadas envían `Authorization: Bearer <token>`.
- **Vigencia del token:** 86 400 segundos (24 horas) en los perfiles `dev` y `prod`. El flag `rememberMe` del login extiende la vigencia a 30 días (`token-validity-in-seconds-for-remember-me: 2592000`); este flag no está contemplado en los casos de uso.
- **Roles:** el sistema emite `ROLE_ADMIN`, `ROLE_INSTRUCTOR`, `ROLE_APPRENTICE` y `ROLE_USER` (los tres primeros siempre acompañados de `ROLE_USER`). Ten presente que varias anotaciones `@PreAuthorize` del código todavía aceptan `ROLE_COORDINATOR`, un rol que los casos de uso vigentes ya no contemplan.
- **Endpoints públicos:** `POST /api/authenticate`, `GET /api/authenticate`, `/api/register`, `/api/activate`, `/api/account/reset-password/init`, `/api/account/reset-password/finish` y `GET /api/document-types/**`. Los métodos de escritura de `/api/document-types/**` siguen protegidos por `@PreAuthorize` (solo `ROLE_ADMIN` o `ROLE_COORDINATOR`).
- **Paginación:** parámetros `page` (base 0), `size` y `sort=campo,asc|desc`. Spring Boot aplica el tamaño por defecto de 20 registros porque el proyecto no lo sobrescribe en `application.yml`. Los endpoints paginados devuelven un **arreglo JSON** con la página actual; el total y el enlace a la página siguiente viajan en las cabeceras `X-Total-Count` y `Link`. Los catálogos no paginados devuelven el arreglo completo.
- **Errores:** formato RFC 7807 con `Content-Type: application/problem+json`. El cuerpo incluye `type`, `title`, `status`, `detail`, además de las propiedades `message` (clave `error.<errorKey>` o `error.http.<status>`), `params` (nombre de la entidad) y `path`; los errores de validación agregan `fieldErrors` con `objectName`, `field` y `message`. La clave de error de negocio viaja en el cuerpo como `message: error.<errorKey>` y el nombre de la entidad en `params`; el backend no emite cabeceras de error propias (verificado con las pruebas de integración).
- **Códigos transversales:** `401` sin token o token inválido/expirado; `403` autenticado sin permisos; `400` datos inválidos o regla de negocio; `404` recurso inexistente; `500` error inesperado.
- **Fechas y horas:** fechas `YYYY-MM-DD` (`LocalDate`), horas `HH:mm:ss` (`LocalTime`), instantes ISO-8601 en UTC (`Instant`), por ejemplo `2026-09-11T15:04:05Z`. Toda la operación de negocio se interpreta en horario de Colombia (UTC-5).
- **Content type:** `application/json` en cuerpos y respuestas. Los `PATCH` aceptan además `application/merge-patch+json`.

## Índice

| UC                                                  | Nombre                             | Estado          |
| --------------------------------------------------- | ---------------------------------- | --------------- |
| [UC001](#uc001--registrarme)                        | Registrarme                        | Implementado    |
| [UC002](#uc002--iniciar-sesión)                     | Iniciar sesión                     | Implementado    |
| [UC003](#uc003--modificar-datos)                    | Modificar datos                    | Implementado    |
| [UC004](#uc004--cerrar-sesión)                      | Cerrar sesión                      | Parcial         |
| [UC005](#uc005--recuperar-contraseña)               | Recuperar contraseña               | Implementado    |
| [UC019](#uc019--gestionar-configuración-global)     | Gestionar configuración global     | Implementado    |
| [UC020](#uc020--gestionar-jornadas)                 | Gestionar jornadas                 | Implementado    |
| [UC021](#uc021--gestionar-modalidades)              | Gestionar modalidades              | Parcial         |
| [UC022](#uc022--gestionar-tipos-de-documento)       | Gestionar tipos de documento       | Parcial         |
| [UC016](#uc016--gestionar-tipos-de-justificación)   | Gestionar tipos de justificación   | Parcial         |
| [UC006](#uc006--gestionar-perfiles)                 | Gestionar perfiles                 | Parcial         |
| [UC012](#uc012--gestionar-programas-de-aprendizaje) | Gestionar programas de aprendizaje | Parcial         |
| [UC014](#uc014--gestionar-trimestres-académicos)    | Gestionar trimestres académicos    | Parcial         |
| [UC007](#uc007--gestionar-fichas)                   | Gestionar fichas                   | Parcial         |
| [UC015](#uc015--gestionar-materias)                 | Gestionar materias                 | Parcial         |
| [UC008](#uc008--gestionar-aprendices)               | Gestionar aprendices               | Parcial         |
| [UC017](#uc017--consultar-mis-fichas-y-materias)    | Consultar mis fichas y materias    | Parcial         |
| [UC009](#uc009--gestionar-listas-de-asistencia)     | Gestionar listas de asistencia     | Parcial         |
| [UC011](#uc011--gestionar-asistencia-aprendiz)      | Gestionar asistencia (Aprendiz)    | Parcial         |
| [UC010](#uc010--gestionar-justificaciones)          | Gestionar justificaciones          | Parcial         |
| [UC013](#uc013--gestionar-alertas-de-inasistencia)  | Gestionar alertas de inasistencia  | No implementado |
| [UC018](#uc018--gestionar-notificaciones)           | Gestionar notificaciones           | No implementado |
| [UC023](#uc023--consultar-dashboard)                | Consultar dashboard                | Parcial         |

---

## UC001 — Registrarme

**Módulo:** Cuenta y acceso | **Actor:** Aprendiz | **Estado:** Implementado

**Feature:** Autorregistro público de un aprendiz sin exigir ficha. La cuenta nace activa, con rol Aprendiz y login derivado de `<iniciales del tipo de documento>_<número de documento>`.

**Endpoints:**

| Método | Ruta            | Acceso  | Descripción                                                         |
| ------ | --------------- | ------- | ------------------------------------------------------------------- |
| POST   | `/api/register` | Público | Crea la cuenta y el perfil del aprendiz. Responde `201` sin cuerpo. |

**Request — `POST /api/register`**

```json
{
  "documentTypeId": "64f1c2a9e13b7a1f2c8d9e01",
  "documentNumber": "1029384756",
  "firstName": "Ana",
  "middleName": "María",
  "firstLastName": "Gómez",
  "secondLastName": "Ríos",
  "email": "ana.gomez@example.com",
  "phoneNumber": "3001234567",
  "password": "Clave#2026"
}
```

Campos heredados de `AdminUserDTO` como `login`, `id`, `activated` o `authorities` se aceptan en el JSON pero el servicio los ignora.

| Campo            | Tipo   | Obligatorio | Reglas                                                                                                                                              |
| ---------------- | ------ | ----------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| `documentTypeId` | string | Sí          | `@NotNull`; id de un `DocumentType` existente y **activo**. Si no existe: `400 documentTypeNotFound`; si está inactivo: `400 documentTypeInactive`. |
| `documentNumber` | string | Sí          | `@NotNull`, `@Pattern(\d+)`, máximo 30. Solo dígitos.                                                                                               |
| `firstName`      | string | Sí          | `@NotNull`, 1–30 caracteres.                                                                                                                        |
| `middleName`     | string | No          | 1–30 caracteres.                                                                                                                                    |
| `firstLastName`  | string | Sí          | `@NotNull`, 1–30 caracteres.                                                                                                                        |
| `secondLastName` | string | No          | 1–30 caracteres.                                                                                                                                    |
| `email`          | string | Sí          | `@Email`, 5–254; único en el sistema (case-insensitive).                                                                                            |
| `phoneNumber`    | string | Sí          | `@NotNull`, `@Pattern(\d{10})`: exactamente 10 dígitos.                                                                                             |
| `password`       | string | Sí          | 8–20 con mayúscula, minúscula, número y carácter especial (validado en `UserService.registerUser`).                                                 |

**Response:** `201 Created` sin cuerpo.

**Errores:**

| Código | errorKey (cuerpo `message`)                                                    | Causa                                                                                                                      |
| ------ | ------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------- |
| 400    | `error.emailexists`                                                            | El correo ya está en uso.                                                                                                  |
| 400    | `error.documentnumberexists`                                                   | El par tipo + número de documento ya está registrado en una cuenta **activa**.                                             |
| 400    | `error.documentnumberinactive`                                                 | El par tipo + número pertenece a una cuenta **desactivada**; el aprendiz debe contactar al Administrador para reactivarla. |
| 400    | `error.documentTypeNotFound`                                                   | `documentTypeId` no corresponde a un tipo de documento existente.                                                          |
| 400    | `error.documentTypeInactive`                                                   | El tipo de documento está inactivo y no puede usarse en el registro.                                                       |
| 400    | `error.emailrequired`                                                          | Correo ausente o en blanco.                                                                                                |
| 400    | `error.invalidpassword` (tipo `invalid-password`, título "Incorrect password") | La contraseña no cumple la política.                                                                                       |
| 400    | `error.validation`                                                             | Fallo de validación de campos; incluye `fieldErrors`.                                                                      |

**Notas / lo que se necesita:** la cuenta se crea con `ROLE_USER` + `ROLE_APPRENTICE` y `activated = true`; el envío de correo de activación está comentado en el código, aunque `GET /api/activate` existe. No hay `mustChangePassword`. El documento duplicado sí tiene mensajes diferenciados: `error.documentnumberexists` cuando el par tipo + número pertenece a una cuenta **activa** y `error.documentnumberinactive` cuando pertenece a una cuenta **desactivada**. Todas las validaciones (incluida la del documento duplicado) corren antes de la primera escritura, por lo que un registro rechazado por validación no deja usuario ni perfil parciales. El backend cubre el flujo de UC001; quedan a cargo del frontend el formulario en sí y la traducción de las claves de error (`emailrequired`, `documentnumberexists`, `documentnumberinactive`, `documentTypeInactive`); los tipos se obtienen con `GET /api/document-types` (ver UC022).

---

## UC002 — Iniciar sesión

**Módulo:** Cuenta y acceso | **Actor:** Usuario (Aprendiz, Instructor o Administrador) | **Estado:** Implementado

**Feature:** Autenticación por tipo y número de documento + contraseña. Emite un JWT de 24 horas y valida el token en cada petición.

**Endpoints:**

| Método | Ruta                | Acceso      | Descripción                                             |
| ------ | ------------------- | ----------- | ------------------------------------------------------- |
| POST   | `/api/authenticate` | Público     | Valida credenciales y devuelve el JWT.                  |
| GET    | `/api/authenticate` | Público     | `204` si el token vigente es válido; `401` si no.       |
| GET    | `/api/account`      | Autenticado | Devuelve la cuenta del usuario actual (`AdminUserDTO`). |

**Request — `POST /api/authenticate`**

```json
{
  "documentTypeId": "64f1c2a9e13b7a1f2c8d9e01",
  "documentNumber": "1029384756",
  "password": "Clave#2026",
  "rememberMe": false
}
```

| Campo            | Tipo    | Obligatorio | Reglas                                              |
| ---------------- | ------- | ----------- | --------------------------------------------------- |
| `documentTypeId` | string  | Sí          | `@NotNull`, 1–254.                                  |
| `documentNumber` | string  | Sí          | `@NotNull`, 1–20.                                   |
| `password`       | string  | Sí          | `@NotNull`, 4–20 a nivel de validación de bean.     |
| `rememberMe`     | boolean | No          | Si es `true`, la vigencia del token pasa a 30 días. |

**Response:** `200 OK`

```json
{
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "mustChangePassword": false
}
```

La cabecera `Authorization: Bearer <id_token>` viaja también en la respuesta. `mustChangePassword` viaja en el cuerpo del login (`true` en cuentas creadas por un Administrador que aún no cambiaron su contraseña; `false` en el resto). `POST /api/account/change-password` limpia el indicador a `false`; `GET /api/account` responde `200` con un `AdminUserDTO` (campos `id`, `login`, `email`, `activated`, `mustChangePassword`, `langKey`, `imageUrl`, `createdBy`, `createdDate`, `lastModifiedBy`, `lastModifiedDate`, `authorities`).

**Errores:** `401 Unauthorized` con cuerpo `{"message": "error.badcredentials"}` para tipo o número de documento inexistente, relación de usuario ausente o contraseña incorrecta (E1; una misma clave para los tres casos, para no revelar cuál falló). `401` con `{"message": "error.accountinactive"}` para una cuenta desactivada (E2). `400 error.validation` si falta un campo obligatorio. Un token inválido o expirado en un endpoint autenticado sigue respondiendo `401` sin clave de negocio (E3); el frontend deriva la expiración del propio token (`WWW-Authenticate` / `exp`).

**Notas / lo que se necesita:** `POST /api/authenticate` ya devuelve `mustChangePassword` en el cuerpo del login, además de exponerlo el `AdminUserDTO` (las cuentas creadas por un Administrador nacen en `true`; el auto-registro de UC001 lo deja en `false` y el reset de UC005 lo deja en `false`, limpiándolo si estaba activo). El cambio de contraseña (`POST /api/account/change-password` y `PATCH /api/account` cuando cambia la contraseña) limpia el indicador a `false`; el flujo de cambio obligatorio en el primer inicio queda a cargo del frontend. El login ahora distingue credenciales inválidas (`error.badcredentials`, E1) de cuenta inactiva (`error.accountinactive`, E2) mediante la clave de negocio en el cuerpo; la respuesta de E1 es intencionalmente genérica para no filtrar si el documento existe. No hay bloqueo por intentos fallidos ni cierre por inactividad (consistente con el UC). `GET /api/account` devuelve solo datos de la cuenta (`AdminUserDTO`), no nombres, apellidos ni documento del perfil; los datos del perfil (nombres, tipo y número de documento, teléfono y correo) se consultan con `GET /api/account/profile` (UC003).

---

## UC003 — Modificar datos

**Módulo:** Cuenta y acceso | **Actor:** Usuario | **Estado:** Implementado

**Feature:** Actualización parcial de los datos personales del usuario autenticado y cambio de contraseña. El documento no es editable.

**Endpoints:**

| Método | Ruta                           | Acceso      | Descripción                                                                                                       |
| ------ | ------------------------------ | ----------- | ----------------------------------------------------------------------------------------------------------------- |
| GET    | `/api/account/profile`         | Autenticado | Devuelve el perfil del usuario autenticado para precargar el formulario. `200` con el perfil; `404` si no existe. |
| PATCH  | `/api/account`                 | Autenticado | Actualiza datos del perfil y, opcionalmente, la contraseña. `200` sin cuerpo.                                     |
| POST   | `/api/account/change-password` | Autenticado | Cambia la contraseña validando la actual. `200` sin cuerpo.                                                       |

**Request — `PATCH /api/account`**

```json
{
  "firstName": "Ana",
  "middleName": "María",
  "firstLastName": "Gómez",
  "secondLastName": "Ríos",
  "phoneNumber": "3001234567",
  "email": "ana.gomez@example.com",
  "currentPassword": "Clave#2026",
  "newPassword": "Nueva#2026"
}
```

| Campo             | Tipo   | Obligatorio | Reglas                                                                                                                                      |
| ----------------- | ------ | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| `firstName`       | string | No          | 1–30; si se envía, actualiza.                                                                                                               |
| `middleName`      | string | No          | máximo 30; si se envía vacío o solo espacios, se limpia (queda `null`); si se omite, no cambia.                                             |
| `firstLastName`   | string | No          | 1–30; si se envía, actualiza.                                                                                                               |
| `secondLastName`  | string | No          | máximo 30; si se envía vacío o solo espacios, se limpia (queda `null`); si se omite, no cambia.                                             |
| `phoneNumber`     | string | No          | `@Pattern(\d{10})`: exactamente 10 dígitos; si se omite, no cambia.                                                                         |
| `email`           | string | No          | `@Email`, 5–254; debe ser único.                                                                                                            |
| `currentPassword` | string | Condicional | Obligatoria si se envía `newPassword`; se compara contra la vigente y un valor incorrecto responde `400 error.currentpasswordinvalid` (E4). |
| `newPassword`     | string | No          | 8–20; debe cumplir mayúscula, minúscula, número y carácter especial, y ser distinta de la actual.                                           |
| `langKey`         | string | No          | 2–10.                                                                                                                                       |

Campos no enviados quedan sin cambios. Los campos opcionales `middleName` y `secondLastName` se **limpian** enviando una cadena vacía (`""`); el servicio los persiste como `null`. El documento (tipo + número) es **inmutable**: el cliente **no debe enviar** `documentTypeId` ni `documentNumber`. Si llega cualquiera de ellos, el backend rechaza la operación con `400 error.documentimmutable` (E3) sin persistir ningún cambio. `imageUrl` ya no forma parte de este contrato: el cliente **no debe enviarlo** y, si llega, el backend lo **ignora** (no se persiste).

**Request — `POST /api/account/change-password`**

```json
{
  "currentPassword": "Clave#2026",
  "newPassword": "Nueva#2026"
}
```

| Campo             | Tipo   | Obligatorio | Reglas                                                                                            |
| ----------------- | ------ | ----------- | ------------------------------------------------------------------------------------------------- |
| `currentPassword` | string | Sí          | Debe coincidir con la contraseña vigente.                                                         |
| `newPassword`     | string | Sí          | 8–20; debe cumplir mayúscula, minúscula, número y carácter especial, y ser distinta de la actual. |

**Response:** `200 OK` sin cuerpo en ambos endpoints.

**Response — `GET /api/account/profile`**

```json
{
  "id": "6b1d5c...",
  "firstName": "Ana",
  "middleName": "María",
  "firstLastName": "Gómez",
  "secondLastName": "Ríos",
  "documentNumber": "1000000001",
  "phoneNumber": "3001234567",
  "user": {
    "id": "5f2a...",
    "login": "cc_1000000001",
    "email": "ana.gomez@example.com"
  },
  "documentType": {
    "id": "dt-cc",
    "name": "Cédula de ciudadanía"
  }
}
```

El perfil se resuelve siempre desde el contexto de seguridad (`SecurityUtils.getCurrentUserLogin()` → usuario → `UserProfileRepository.findOneByUserId`), nunca desde un parámetro de la solicitud. El bloque `user` anida `id`, `login` y `email`. Responde `404` cuando la cuenta autenticada no tiene perfil asociado.

**Errores:** `400 error.currentpasswordinvalid` si la contraseña actual no coincide o está ausente (E4); `400 error.invalidpassword` (tipo `invalid-password`, título "Incorrect password") si la nueva no cumple la política de complejidad (E5: 8–20 con mayúscula, minúscula, número y carácter especial); `400 error.samepassword` si la nueva es igual a la actual (E6); `400 error.documentimmutable` si la solicitud incluye `documentTypeId` o `documentNumber` (E3: el documento es inmutable y no debe enviarse); `400 error.emailexists` si el correo pertenece a otra cuenta; `400 error.validation` con `fieldErrors`; `401` sin sesión válida.

**Notas / lo que se necesita:** ambas rutas de cambio de contraseña (`POST /api/account/change-password` y el bloque de contraseña de `PATCH /api/account`) validan la contraseña actual (E4), la política completa (E5) y que la nueva sea distinta de la actual (E6). La validación de longitud superficial de la ruta `change-password` se mantiene antes de invocar al servicio. En `PATCH /api/account` el VM ya no valida longitud: una contraseña actual incorrecta llega al servicio y responde `error.currentpasswordinvalid` (E4), y una nueva fuera de política responde `error.invalidpassword` (E5), de modo que ambas rutas comparten las mismas claves de negocio. El cambio de contraseña no cierra la sesión (el cierre voluntario del UC depende del cliente) y limpia `mustChangePassword` a `false` tanto en `POST /api/account/change-password` como en `PATCH /api/account` cuando cambia la contraseña. `PATCH /api/account` no devuelve el perfil actualizado; el frontend precarga el formulario de edición con `GET /api/account/profile`. Cuando se envía `phoneNumber`, debe tener **exactamente 10 dígitos**: un valor de 9 u 11 dígitos, o con letras, responde `400 error.validation` con `fieldErrors` sobre `phoneNumber` (lo valida el VM antes de invocar al servicio, por lo que no se persiste ningún cambio parcial).

---

## UC004 — Cerrar sesión

**Módulo:** Cuenta y acceso | **Actor:** Usuario | **Estado:** Parcial

**Feature:** Cierre manual de la sesión del dispositivo actual. Con JWT sin estado, el cierre consiste en descartar el token en el cliente.

**Endpoints:** no hay endpoint de servidor. El token sigue siendo válido hasta su expiración (máximo 24 horas); no existe lista de revocación ni invalidación inmediata.

**Propuesta** (no implementada): `POST /api/logout` — invalida el token actual (por ejemplo, con una lista de revocación de corta vida). Su implementación es opcional para el MVP, dado que el cliente puede descartar el token.

**Errores:** si el token ya expiró, cualquier petición autenticada responde `401` (E1 del UC).

**Notas / lo que se necesita:** la desactivación de una cuenta no corta la sesión ya abierta (solo impide nuevos logins); la invalidación inmediata sigue pendiente. No hay endpoint que permita al usuario consultar o cerrar sus otras sesiones.

---

## UC005 — Recuperar contraseña

**Módulo:** Cuenta y acceso | **Actor:** Usuario | **Estado:** Implementado

**Feature:** Solicitud de restablecimiento por tipo y número de documento, con enlace de un solo uso enviado al correo registrado. El sistema responde siempre el mismo mensaje neutro, exista o no la cuenta.

**Endpoints:**

| Método | Ruta                                 | Acceso  | Descripción                                                    |
| ------ | ------------------------------------ | ------- | -------------------------------------------------------------- |
| POST   | `/api/account/reset-password/init`   | Público | Genera la clave de reset y envía el correo. `200` sin cuerpo.  |
| POST   | `/api/account/reset-password/finish` | Público | Consume la clave y fija la nueva contraseña. `200` sin cuerpo. |

**Request — `POST /api/account/reset-password/init`**

```json
{
  "documentTypeId": "64f1c2a9e13b7a1f2c8d9e01",
  "documentNumber": "1029384756"
}
```

| Campo            | Tipo   | Obligatorio | Reglas             |
| ---------------- | ------ | ----------- | ------------------ |
| `documentTypeId` | string | Sí          | `@NotNull`, 1–254. |
| `documentNumber` | string | Sí          | `@NotNull`, 1–254. |

**Request — `POST /api/account/reset-password/finish`**

```json
{
  "key": "e0f1a2b3c4d5e6f7a8b9",
  "newPassword": "Nueva#2026"
}
```

| Campo         | Tipo   | Obligatorio | Reglas                                                     |
| ------------- | ------ | ----------- | ---------------------------------------------------------- |
| `key`         | string | Sí          | Clave de reset vigente y no usada.                         |
| `newPassword` | string | Sí          | 8–20 con mayúscula, minúscula, número y carácter especial. |

**Response:** `200 OK` sin cuerpo en ambos casos. `init` responde `200` aunque el usuario no exista o el correo falle (el envío es asíncrono y los fallos se registran en logs).

**Errores:** todos los fallos de `finish` son `400 Bad Request` con una clave de negocio estable en `$.message`:

| Situación                       | `$.message`              | Causa                                                                                                           |
| ------------------------------- | ------------------------ | --------------------------------------------------------------------------------------------------------------- |
| Enlace inexistente o manipulado | `error.resetlinkinvalid` | La clave no está asociada a ningún usuario.                                                                     |
| Enlace ya usado (E4)            | `error.resetlinkused`    | La clave existe pero su `resetDate` es `null` (el enlace ya se consumió).                                       |
| Enlace expirado (E3)            | `error.resetlinkexpired` | La clave existe pero su `resetDate` es anterior a `now - 30 minutos`.                                           |
| Contraseña débil (E2)           | `error.invalidpassword`  | La nueva contraseña no cumple la política completa (8–20 con mayúscula, minúscula, número y carácter especial). |

**Notas / lo que se necesita:** la clave de reset expira a los **30 minutos** (`resetDate > now - 30 minutos` en `UserService.completePasswordReset`), como pide el UC. Es de un solo uso: al completar se limpia `resetDate` pero se **conserva** `resetKey`, de modo que un segundo intento sobre el mismo enlace resuelve como `error.resetlinkused` (E4) y no como enlace inválido. El reset no activa un indicador de cambio obligatorio y, si `mustChangePassword` estaba activo, lo **limpia** (el usuario eligió su propia contraseña). El mensaje neutro no se devuelve en el cuerpo: `init` responde `200` vacío y el cliente debe mostrar el texto del UC. El enlace expirado responde `400` con `error.resetlinkexpired` ("El enlace ha expirado, solicita uno nuevo"); el ya usado responde `400` con `error.resetlinkused` ("Este enlace ya no es válido").

---

## UC019 — Gestionar configuración global

**Módulo:** Configuración y catálogos | **Actor:** Administrador | **Estado:** Implementado

**Feature:** Lectura y actualización parcial de la configuración global (singleton): días para justificar, días de respuesta del instructor y los umbrales de alerta por fallas consecutivas y acumuladas. Aplica hacia adelante, sin recalcular datos existentes.

**Endpoints:**

| Método | Ruta                         | Acceso       | Descripción                                                                                                                                                     |
| ------ | ---------------------------- | ------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| GET    | `/api/global-configurations` | `ROLE_ADMIN` | Lectura usada por la pantalla de configuración; devuelve la vigente o la re-crea con defaults. Siempre devuelve el singleton con `id = "global-configuration"`. |
| PATCH  | `/api/global-configurations` | `ROLE_ADMIN` | Actualización parcial del singleton; el `id` es opcional. Si falta, se actualiza el singleton; si viaja, debe ser `global-configuration`.                       |

**Request — `PATCH /api/global-configurations`**

```json
{
  "studentJustificationDays": 5,
  "instructorResponseDays": 2,
  "consecutiveAbsenceAlertThreshold": 3,
  "accumulatedAbsenceAlertThreshold": 5
}
```

| Campo                              | Tipo    | Obligatorio | Reglas                                                                                                                     |
| ---------------------------------- | ------- | ----------- | -------------------------------------------------------------------------------------------------------------------------- |
| `id`                               | string  | No          | Opcional. Si falta, se actualiza el singleton con `id = "global-configuration"`; si viaja con otro valor: `400 idinvalid`. |
| `studentJustificationDays`         | integer | No          | Plazo en días hábiles: `@Min(1)` y `@Max(30)` (rango 1–30). Default 5 al sembrar la fila.                                  |
| `instructorResponseDays`           | integer | No          | Plazo en días hábiles: `@Min(1)` y `@Max(30)` (rango 1–30). Default 2 al sembrar la fila.                                  |
| `consecutiveAbsenceAlertThreshold` | integer | No          | `@Min(1)`, sin máximo. Default 3 al sembrar la fila (alerta por materia, UC013).                                           |
| `accumulatedAbsenceAlertThreshold` | integer | No          | `@Min(1)`, sin máximo. Default 5 al sembrar la fila (alerta por ficha, UC013).                                             |

**Response:** `200 OK`

```json
{
  "id": "global-configuration",
  "studentJustificationDays": 5,
  "instructorResponseDays": 2,
  "consecutiveAbsenceAlertThreshold": 3,
  "accumulatedAbsenceAlertThreshold": 5
}
```

**Errores:** `400 error.idinvalid`, `400 error.validation`, `403` si no es Administrador, `404` si el servicio no encuentra la fila. Un plazo fuera de 1–30 o un umbral menor a 1 no se guarda: responde `400 error.validation` con una entrada por campo en `fieldErrors` (campo y mensaje).

**Notas / lo que se necesita:** la configuración global es un **singleton** con `id` fijo `global-configuration`. La lectura y la escritura siempre operan sobre ese documento: `GET` lo devuelve (y lo re-crea con defaults si falta) y `PATCH` lo actualiza aunque el cuerpo no envíe `id`; un `id` distinto responde `400 error.idinvalid`. El modelo expone los cuatro parámetros del UC. Los plazos `studentJustificationDays` e `instructorResponseDays` se validan en el borde de la API con rango **1–30 días** (`@Min(1)` + `@Max(30)`), de modo que un valor fuera de rango produce un error por campo y nunca llega a la persistencia. Los umbrales `consecutiveAbsenceAlertThreshold` (default 3) y `accumulatedAbsenceAlertThreshold` (default 5) los consume UC013 y solo exigen **mínimo 1** (sin máximo, el UC no define tope). La lectura está disponible para cualquier usuario autenticado; la escritura es solo del Administrador, consistente con E2.

---

## UC020 — Gestionar jornadas

**Módulo:** Configuración y catálogos | **Actor:** Administrador | **Estado:** Implementado

**Feature:** CRUD del catálogo de jornadas (nombre y rango horario) que define la disponibilidad horaria de las fichas. En el código la jornada se llama `TimeSlot`.

**Endpoints:**

| Método | Ruta                     | Acceso       | Descripción                                                                                              |
| ------ | ------------------------ | ------------ | -------------------------------------------------------------------------------------------------------- |
| GET    | `/api/time-slots`        | Autenticado  | Lista **paginada** de jornadas (20 por página por defecto).                                              |
| GET    | `/api/time-slots/active` | Autenticado  | Lista de jornadas activas.                                                                               |
| GET    | `/api/time-slots/{id}`   | Autenticado  | Detalle de una jornada.                                                                                  |
| POST   | `/api/time-slots`        | `ROLE_ADMIN` | Crea la jornada (nace Activa); `201` con el recurso creado.                                              |
| PUT    | `/api/time-slots`        | `ROLE_ADMIN` | Reemplaza la jornada; el `id` va en el body; `200` con el recurso.                                       |
| PATCH  | `/api/time-slots`        | `ROLE_ADMIN` | Actualización parcial; el `id` va en el body; `200` con el recurso.                                      |
| DELETE | `/api/time-slots/{id}`   | `ROLE_ADMIN` | Elimina; `204`. Bloquea la eliminación si la jornada está asignada a fichas (`400 error.timeSlotInUse`). |

**Request — `POST /api/time-slots`**

```json
{
  "name": "Diurna",
  "startTime": "06:00:00",
  "endTime": "18:00:00"
}
```

| Campo       | Tipo                | Obligatorio | Reglas                                                                                                                                                                                                  |
| ----------- | ------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `name`      | string              | Sí          | `@NotBlank`, máximo 50. **Nombre único** (se compara sin distinguir mayúsculas); un duplicado responde `400 error.timeSlotNameAlreadyUsed`.                                                             |
| `startTime` | string (`HH:mm:ss`) | Sí          | `@NotNull`.                                                                                                                                                                                             |
| `endTime`   | string (`HH:mm:ss`) | Sí          | `@NotNull`. Debe ser distinto de `startTime`; si son iguales responde `400 error.timeSlotSameTime` (E2). Se permite el cruce de medianoche (`endTime` menor que `startTime`, p. ej. 22:00:00–06:00:00). |
| `isActive`  | boolean             | No          | Ignorado en la creación: el backend siempre crea la jornada como **Activa** (`isActive = true`).                                                                                                        |

**Response:** `201 Created` con el `TimeSlotDTO` creado (`id`, `name`, `startTime`, `endTime`, `isActive`). `GET /api/time-slots` es **paginado**: acepta `page` (base 0), `size` y `sort=campo,asc|desc` (20 por defecto) y devuelve un arreglo JSON con la página actual más las cabeceras `X-Total-Count` y `Link`. `GET /api/time-slots/active` sigue devolviendo el arreglo completo sin paginar (selector).

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idnotfound`, `400 error.validation`; `400 error.timeSlotNameAlreadyUsed` (nombre duplicado, E1); `400 error.timeSlotSameTime` (hora de inicio igual a la hora de fin, E2); `400 error.timeSlotInUse` (la jornada está asignada a una o más fichas y no puede eliminarse, E3); `403` sin rol permitido; `404` en detalle inexistente.

**Notas / lo que se necesita:** ya están implementados el nombre único (E1), el rechazo de horas iguales (E2) y el bloqueo de eliminación cuando la jornada está asignada a fichas (E3). `DELETE /api/time-slots/{id}` responde `400 error.timeSlotInUse` si existe al menos una ficha que referencia la jornada, y `204` cuando ninguna la usa. Una jornada en uso **no se elimina**: la alternativa es **desactivarla** con `PATCH /api/time-slots` (`isActive: false`), de modo que las fichas existentes la sigan conservando. Las jornadas que cruzan medianoche (`endTime` menor que `startTime`) se aceptan. El estado se cambia con el `PATCH` genérico (`isActive`), sin acciones dedicadas de Desactivar/Reactivar. Al crear (`POST`) el backend fuerza `isActive = true` (la jornada nace Activa) e ignora el valor enviado; el `PATCH` sí puede cambiar el estado. En `PUT` y `PATCH` el `id` de la jornada viaja **solo en el body** (no en la ruta): si falta responde `400 error.idnull` y si no existe, `400 error.idnotfound`. `GET /api/time-slots` usa el estándar de paginación del sistema (`page`/`size`/`sort`, `X-Total-Count`, `Link`); `GET /api/time-slots/active` permanece sin paginar porque alimenta selectores. La **escritura** (`POST`, `PUT`, `PATCH`, `DELETE`) está restringida a `ROLE_ADMIN`; un usuario autenticado sin ese rol recibe `403`. La **lectura** (`GET /api/time-slots`, `GET /api/time-slots/{id}` y `GET /api/time-slots/active`) queda disponible para cualquier usuario autenticado porque alimenta selectores de otros flujos.

---

## UC021 — Gestionar modalidades

**Módulo:** Configuración y catálogos | **Actor:** Administrador | **Estado:** Parcial

**Feature:** CRUD del catálogo de modalidades de formación que se seleccionan al crear fichas.

**Endpoints:**

| Método | Ruta                     | Acceso                            | Descripción                   |
| ------ | ------------------------ | --------------------------------- | ----------------------------- |
| GET    | `/api/modalities`        | Autenticado                       | Lista completa (sin paginar). |
| GET    | `/api/modalities/active` | Autenticado                       | Lista de modalidades activas. |
| GET    | `/api/modalities/{id}`   | Autenticado                       | Detalle.                      |
| POST   | `/api/modalities`        | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Crea; `201` con el recurso.   |
| PUT    | `/api/modalities/{id}`   | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Reemplaza; `200`.             |
| PATCH  | `/api/modalities/{id}`   | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Actualización parcial; `200`. |
| DELETE | `/api/modalities/{id}`   | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Elimina; `204`.               |

**Request — `POST /api/modalities`**

```json
{
  "name": "Presencial",
  "isActive": true
}
```

| Campo      | Tipo    | Obligatorio | Reglas                                                                                                                       |
| ---------- | ------- | ----------- | ---------------------------------------------------------------------------------------------------------------------------- |
| `name`     | string  | Sí          | `@NotBlank`, máximo 50. **Nombre único** (se compara sin distinguir mayúsculas); un duplicado responde `400 error.modalityNameAlreadyUsed`. |
| `isActive` | boolean | Sí          | `@NotNull`; el cliente define el estado inicial.                                                                             |

**Response:** `201 Created` con el `ModalityDTO` (`id`, `name`, `isActive`). Listas como arreglo JSON completo.

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idinvalid`, `400 error.idnotfound`, `400 error.validation`; `400 error.modalityNameAlreadyUsed` (nombre duplicado, E1); `403`; `404`.

**Notas / lo que se necesita:** está implementada la unicidad de nombre (E1): el backend recorta el nombre y lo compara sin distinguir mayúsculas; un duplicado responde `400 error.modalityNameAlreadyUsed` y un nombre vacío o en blanco responde `400 error.validation` con una entrada en `fieldErrors`. Falta el bloqueo de eliminación si la modalidad está en uso por fichas (E2). El estado se maneja con `isActive` en lugar de las acciones Desactivar/Reactivar.

---

## UC022 — Gestionar tipos de documento

**Módulo:** Configuración y catálogos | **Actor:** Administrador | **Estado:** Parcial

**Feature:** CRUD del catálogo de tipos de documento (CC, TI, CE, …). Las iniciales alimentan el login derivado `<iniciales>_<número>`.

**Endpoints:**

| Método | Ruta                       | Acceso                            | Descripción                                                     |
| ------ | -------------------------- | --------------------------------- | --------------------------------------------------------------- |
| GET    | `/api/document-types`      | Público                           | Lista completa (sin paginar); usado por el registro y el login. |
| GET    | `/api/document-types/{id}` | Público                           | Detalle.                                                        |
| POST   | `/api/document-types`      | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Crea; `201` con el recurso.                                     |
| PUT    | `/api/document-types/{id}` | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Reemplaza; `200`.                                               |
| PATCH  | `/api/document-types/{id}` | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Actualización parcial; `200`.                                   |
| DELETE | `/api/document-types/{id}` | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Elimina; `204`.                                                 |

**Request — `POST /api/document-types`**

```json
{
  "name": "Cédula de ciudadanía",
  "initials": "CC"
}
```

| Campo      | Tipo    | Obligatorio | Reglas                                                                                   |
| ---------- | ------- | ----------- | ---------------------------------------------------------------------------------------- |
| `name`     | string  | Sí          | `@NotNull`, máximo 30. No se valida unicidad.                                            |
| `initials` | string  | Sí          | `@NotNull`, máximo 10. No se normaliza a mayúsculas en el backend ni se valida unicidad. |
| `isActive` | boolean | No          | Estado del catálogo. Si se omite al crear, el tipo nace **activo** (`true`).             |

**Response:** `201 Created` con el `DocumentTypeDTO` (`id`, `name`, `initials`, `isActive`). El listado es público por configuración de seguridad, lo que permite poblar los formularios de registro e inicio de sesión sin sesión.

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idinvalid`, `400 error.idnotfound`, `400 error.validation`; `403` en escritura; `404`.

**Notas / lo que se necesita:** el `DocumentTypeDTO` ahora expone `isActive`; los tipos nuevos nacen activos (`true`) y el registro (UC001) rechaza los inactivos con `400 error.documentTypeInactive`. Al actualizar sin enviar `isActive`, el backend conserva el estado existente. Quedan pendientes las demás reglas de UC022: no se validan nombre ni iniciales duplicados (E1/E2), no se bloquea el cambio de iniciales de un tipo en uso (E3) ni su eliminación por uso (E4). Cualquier usuario autenticado puede listar tipos, y los GET son públicos.

---

## UC016 — Gestionar tipos de justificación

**Módulo:** Configuración y catálogos | **Actor:** Administrador | **Estado:** Parcial

**Feature:** CRUD del catálogo de motivos de justificación. Cada tipo define el límite de días justificables por trimestre.

**Endpoints:**

| Método | Ruta                            | Acceso                            | Descripción                   |
| ------ | ------------------------------- | --------------------------------- | ----------------------------- |
| GET    | `/api/justification-types`      | Autenticado                       | Lista completa (sin paginar). |
| GET    | `/api/justification-types/{id}` | Autenticado                       | Detalle.                      |
| POST   | `/api/justification-types`      | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Crea; `201` con el recurso.   |
| PUT    | `/api/justification-types/{id}` | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Reemplaza; `200`.             |
| PATCH  | `/api/justification-types/{id}` | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Actualización parcial; `200`. |
| DELETE | `/api/justification-types/{id}` | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Elimina; `204`.               |

**Request — `POST /api/justification-types`**

```json
{
  "name": "Calamidad doméstica",
  "limitPerTrimester": 3,
  "state": "ACTIVO"
}
```

| Campo               | Tipo    | Obligatorio | Reglas                                                                                           |
| ------------------- | ------- | ----------- | ------------------------------------------------------------------------------------------------ |
| `name`              | string  | Sí          | `@NotNull`, máximo 100. No se valida unicidad.                                                   |
| `limitPerTrimester` | integer | No          | Sin `@Min`; el UC exige entero mayor a 0.                                                        |
| `state`             | string  | Sí          | `@NotNull`; valores del enum `State`: `ACTIVO`, `INACTIVO`. El cliente define el estado inicial. |

**Response:** `201 Created` con el `JustificationTypeDTO` (`id`, `name`, `limitPerTrimester`, `state`).

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idinvalid`, `400 error.idnotfound`, `400 error.validation`; `403`; `404`.

**Notas / lo que se necesita:** no hay validación de nombre duplicado (E1), límite mayor a 0 (E2), bloqueo de eliminación si el tipo fue usado (E3) ni endpoint de solo activos para el formulario del aprendiz. Los estados del enum son `ACTIVO`/`INACTIVO` (el UC los llama Activo/Inactivo).

---

## UC006 — Gestionar perfiles

**Módulo:** Usuarios | **Actor:** Administrador | **Estado:** Parcial

**Feature:** Creación de Instructores y Administradores (y de Aprendices por excepción), modificación de datos de cualquier perfil, cambio de rol, desactivación y reactivación. Los usuarios nunca se eliminan y el login se deriva del documento.

**Endpoints:**

| Método | Ruta                                            | Acceso       | Descripción                                                                     |
| ------ | ----------------------------------------------- | ------------ | ------------------------------------------------------------------------------- |
| POST   | `/api/admin/users`                              | `ROLE_ADMIN` | Crea usuario + perfil; `201` con la entidad `User` creada.                      |
| PATCH  | `/api/admin/users` o `/api/admin/users/{login}` | `ROLE_ADMIN` | Actualización parcial; el `id` viaja en el cuerpo. `200` con `AdminUserDTO`.    |
| GET    | `/api/admin/users`                              | `ROLE_ADMIN` | Lista paginada de cuentas (`AdminUserDTO`).                                     |
| GET    | `/api/admin/users/{login}`                      | `ROLE_ADMIN` | Detalle por login; `200` o `404`.                                               |
| GET    | `/api/admin/users/search`                       | `ROLE_ADMIN` | Búsqueda por texto y estado; lista paginada de `UserManagementDTO`.             |
| PATCH  | `/api/admin/users/activated`                    | `ROLE_ADMIN` | Activa/desactiva por número de documento; `200` con `AdminUserDTO`.             |
| DELETE | `/api/admin/users/{login}`                      | `ROLE_ADMIN` | Elimina la cuenta; `204`. Contradice la regla "los usuarios nunca se eliminan". |

**Request — `POST /api/admin/users`**

```json
{
  "firstName": "Carlos",
  "middleName": null,
  "firstLastName": "Pérez",
  "secondLastName": null,
  "documentTypeId": "64f1c2a9e13b7a1f2c8d9e01",
  "documentNumber": "1029384756",
  "phoneNumber": "3001234567",
  "email": "carlos.perez@example.com",
  "password": "Temporal#2026",
  "role": "ROLE_INSTRUCTOR",
  "langKey": "es"
}
```

| Campo            | Tipo   | Obligatorio       | Reglas                                                                                                                                                     |
| ---------------- | ------ | ----------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `firstName`      | string | Sí                | `@NotNull`, 1–30.                                                                                                                                          |
| `middleName`     | string | No                | 1–30.                                                                                                                                                      |
| `firstLastName`  | string | Sí                | `@NotNull`, 1–30.                                                                                                                                          |
| `secondLastName` | string | No                | 1–30.                                                                                                                                                      |
| `documentTypeId` | string | Sí                | `@NotNull`; debe existir (`400 documentTypeNotFound`).                                                                                                     |
| `documentNumber` | string | Sí                | `@NotNull`, 1–20.                                                                                                                                          |
| `phoneNumber`    | string | Sí                | `@NotNull`, 1–20.                                                                                                                                          |
| `email`          | string | Sí                | `@Email`, 5–254; único (`400 emailexists`).                                                                                                                |
| `password`       | string | Sí                | 8–20 y política completa (`InvalidPasswordException` si no cumple).                                                                                        |
| `role`           | string | Sí en la práctica | `@Size(1,50)`; válidos `ROLE_ADMIN`, `ROLE_INSTRUCTOR`, `ROLE_APPRENTICE`. Un valor inexistente produce `400 rolenotfound`. Siempre se agrega `ROLE_USER`. |
| `langKey`        | string | No                | 2–10; default del sistema si se omite.                                                                                                                     |

**Request — `PATCH /api/admin/users`** (parcial; `AdminUpdateUserVM`)

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9e77",
  "firstName": "Carlos",
  "documentNumber": "1029384756",
  "documentTypeId": "64f1c2a9e13b7a1f2c8d9e01",
  "email": "carlos.perez@example.com",
  "phoneNumber": "3001234567",
  "role": "ROLE_INSTRUCTOR"
}
```

Todos los campos son opcionales: solo se actualizan los presentes. `id` es obligatorio (`400 idmissing`). `login`, `activated`, `authorities` y `mustChangePassword` se ignoran. Si cambia `documentNumber` (o `documentTypeId`), el login se recalcula y se validan duplicados. No acepta contraseña. Los campos de perfil tienen las mismas longitudes que en la creación (`firstName`/`firstLastName` 1–30, `documentNumber` 1–15, `phoneNumber` 1–20).

**Request — `PATCH /api/admin/users/activated`**

```json
{
  "documentNumber": "1029384756",
  "activated": false
}
```

| Campo            | Tipo    | Obligatorio | Reglas                                        |
| ---------------- | ------- | ----------- | --------------------------------------------- |
| `documentNumber` | string  | Sí          | `@NotBlank`, 1–15; identifica el perfil.      |
| `activated`      | boolean | Sí          | `@NotNull`; `true` activa, `false` desactiva. |

**Response — `POST /api/admin/users`:** `201 Created`; el cuerpo es la entidad `User` (no un DTO), con el hash de contraseña y las claves internas excluidos por `@JsonIgnore`:

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9e77",
  "login": "cc_1029384756",
  "email": "carlos.perez@example.com",
  "activated": true,
  "mustChangePassword": true,
  "langKey": "es",
  "imageUrl": null,
  "resetDate": null,
  "createdBy": "admin",
  "createdDate": "2026-09-11T15:04:05Z",
  "lastModifiedBy": "admin",
  "lastModifiedDate": "2026-09-11T15:04:05Z"
}
```

`PATCH /api/admin/users` y `PATCH /api/admin/users/activated` responden `200` con un `AdminUserDTO` (`id`, `login`, `email`, `activated`, `mustChangePassword`, `langKey`, `imageUrl`, auditoría y `authorities`). Las listas paginadas devuelven arreglos de `AdminUserDTO` / `UserManagementDTO` (`id`, `fullName`, `documentNumber`, `email`, `authorities`, `activated`) más cabeceras `X-Total-Count` y `Link`. `GET /api/admin/users` solo admite `sort` sobre `id`, `login`, `email`, `activated`, `langKey`, `createdBy`, `createdDate`, `lastModifiedBy`, `lastModifiedDate`.

**Errores:** `400 error.idexists`, `400 error.idmissing`, tipo `invalid-password`, `400 error.userexists`, `400 error.emailexists`, `400 error.documentnumberexists`, `400 error.documentTypeNotFound`, `400 error.rolenotfound`, `400 error.lastAdmin` ("Debe existir al menos un Administrador activo"; la cuenta `admin` está protegida), `400 error.lastInstructor` (lista las materias sin instructor); `403`; `404`.

**Notas / lo que se necesita:** las cuentas creadas por un Administrador nacen con `mustChangePassword = true` en la entidad `User` y el indicador se expone en el `AdminUserDTO`; falta implementar el flujo de cambio obligatorio en el primer inicio. El correo de credenciales se envía de forma síncrona al crear; si falla, se guarda una `Notificacion` pendiente, pero **no hay endpoint** para listarla o reenviarla (UC018). El borrado sigue disponible. La guarda del instructor único solo considera materias activas, no fichas Pendiente/Activa como pide el UC. Existe además el CRUD genérico `/api/user-profiles` sin `@PreAuthorize` (cualquier usuario autenticado), que puede crear o eliminar perfiles por fuera de este flujo.

---

## UC012 — Gestionar programas de aprendizaje

**Módulo:** Programas y trimestres | **Actor:** Administrador | **Estado:** Parcial

**Feature:** CRUD de programas con nombre, iniciales, código numérico y cantidad de trimestres. Solo los programas activos pueden recibir fichas nuevas.

**Endpoints:**

| Método | Ruta                      | Acceso                            | Descripción                                                                              |
| ------ | ------------------------- | --------------------------------- | ---------------------------------------------------------------------------------------- |
| GET    | `/api/programs`           | Autenticado                       | Lista paginada.                                                                          |
| GET    | `/api/programs/search`    | Autenticado                       | Filtra por `search` (código o nombre) y `status`; paginado.                              |
| GET    | `/api/programs/{id}`      | Autenticado                       | Detalle.                                                                                 |
| POST   | `/api/programs`           | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Crea; `201` con el recurso.                                                              |
| PUT    | `/api/programs/{id}`      | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Reemplaza; `200`.                                                                        |
| PATCH  | `/api/programs`           | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Actualización parcial; `id` en el cuerpo; ignora `status` y valores en blanco.           |
| PATCH  | `/api/programs/activated` | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Activa/desactiva; devuelve el programa y, si aplica, una advertencia por fichas activas. |
| DELETE | `/api/programs/{id}`      | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Elimina; `204`.                                                                          |

**Request — `POST /api/programs`**

```json
{
  "name": "Análisis y Desarrollo de Software",
  "initials": "ADSO",
  "code": "228118",
  "trimesters": 6
}
```

| Campo        | Tipo    | Obligatorio | Reglas                                                                  |
| ------------ | ------- | ----------- | ----------------------------------------------------------------------- |
| `name`       | string  | Sí          | `@NotNull`, máximo 200; único sin distinguir mayúsculas.                |
| `initials`   | string  | Sí          | `@NotNull`, máximo 10; se normaliza a mayúsculas; única.                |
| `code`       | string  | Sí          | `@NotNull`, máximo 30; único. El backend **no** exige que sea numérico. |
| `trimesters` | integer | Sí          | `@Min(1)` y `@Max(12)`.                                                 |
| `status`     | boolean | No          | Si se omite, el servicio lo crea activo (`true`).                       |

**Request — `PATCH /api/programs/activated`**

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9e30",
  "status": false
}
```

`status` se deserializa de forma estricta: solo se aceptan los booleanos JSON `true`/`false` (no `1`/`0`).

**Response — Creación:** `201 Created`

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9e30",
  "name": "Análisis y Desarrollo de Software",
  "initials": "ADSO",
  "code": "228118",
  "trimesters": 6,
  "status": true
}
```

**Response — `PATCH /api/programs/activated`:** `200 OK`

```json
{
  "program": {
    "id": "665f1c2a9e13b7a1f2c8d9e30",
    "name": "Análisis y Desarrollo de Software",
    "initials": "ADSO",
    "code": "228118",
    "trimesters": 6,
    "status": false
  },
  "activeFichasCount": 2,
  "warning": "Este programa tiene 2 fichas activas; no podrán crearse nuevas fichas bajo este programa hasta reactivarlo"
}
```

`warning` solo aparece al desactivar un programa con fichas activas; `activeFichasCount` se envía siempre que `status` sea `false`. Las listas paginadas usan las cabeceras `X-Total-Count` y `Link`.

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idinvalid`, `400 error.idnotfound`, `400 error.codeexists` ("Ya existe un programa con este código"), `400 error.initialsexists`, `400 error.nameexists`, `400 error.validation`; `403`; `404`.

**Notas / lo que se necesita:** no se valida que el código sea numérico (E5) y `DELETE` no verifica si el programa tiene fichas (E8). La advertencia de desactivación (E7) sí está implementada. El CRUD acepta `ROLE_COORDINATOR`, aunque los casos de uso solo contemplan Administrador.

---

## UC014 — Gestionar trimestres académicos

**Módulo:** Programas y trimestres | **Actor:** Administrador | **Estado:** Parcial

**Feature:** CRUD de trimestres globales. Su estado (`status`) se calcula por fechas y un proceso diario lo sincroniza; el trimestre activo enmarca asistencias, horarios y justificaciones.

**Endpoints:**

| Método | Ruta                     | Acceso                            | Descripción                                                     |
| ------ | ------------------------ | --------------------------------- | --------------------------------------------------------------- |
| GET    | `/api/trimesters`        | Autenticado                       | Lista paginada.                                                 |
| GET    | `/api/trimesters/search` | Autenticado                       | Filtra por año (4 dígitos) o nombre, y por `status`; paginado.  |
| GET    | `/api/trimesters/{id}`   | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Detalle.                                                        |
| POST   | `/api/trimesters`        | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Crea y calcula `status`; `201`.                                 |
| PUT    | `/api/trimesters/{id}`   | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Reemplaza; `200`. No aplica validaciones de estado.             |
| PATCH  | `/api/trimesters`        | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Actualización parcial con reglas por estado; `id` en el cuerpo. |
| DELETE | `/api/trimesters/{id}`   | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Elimina; `204`.                                                 |

**Request — `POST /api/trimesters`**

```json
{
  "name": "2026-2",
  "startDate": "2026-10-01",
  "endDate": "2026-12-20"
}
```

| Campo       | Tipo                  | Obligatorio | Reglas                                                                                                 |
| ----------- | --------------------- | ----------- | ------------------------------------------------------------------------------------------------------ |
| `name`      | string                | Sí          | `@NotNull`, máximo 30.                                                                                 |
| `startDate` | string (`YYYY-MM-DD`) | Sí          | `@NotNull`; debe ser anterior a `endDate`.                                                             |
| `endDate`   | string (`YYYY-MM-DD`) | Sí          | `@NotNull`; no puede solaparse con otro trimestre.                                                     |
| `status`    | boolean               | No          | Lo calcula el servidor al crear (`startDate <= hoy <= endDate`); el valor enviado se ignora en `POST`. |

**Response:** `201 Created`

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9e40",
  "name": "2026-2",
  "startDate": "2026-10-01",
  "endDate": "2026-12-20",
  "status": false
}
```

**Errores:** `400 error.datesorder` ("La fecha de inicio debe ser anterior a la fecha de fin"), `400 error.datesoverlap` ("Ya existe un trimestre que se solapa con las fechas indicadas"), `400 error.noteditable` ("No se puede editar un trimestre que ya ha finalizado"), `400 error.startdatelocked`, `400 error.enddateinpast`, `400 error.startdatemustbefuture`, `400 error.attendancestartdate`; además de `idexists`, `idnull`, `idinvalid`, `idnotfound` y de validación; `403`; `404`.

**Notas / lo que se necesita:** al crear **no** se valida que la fecha de inicio sea desde mañana ni que la fecha fin no esté vencida (solo orden y solapamiento). `PUT` no valida nada: reemplaza el documento tal como llega, incluido `status`. `PATCH` sí aplica las reglas por estado (no editable si cerró, inicio congelado si está activo, end ≥ hoy si está activo, inicio futuro si es futuro, bloqueo si ya hay asistencias). `DELETE` no verifica horarios ni asistencias asociadas. Los estados Futuro/Activo/Cerrado se derivan de `status` + fechas, no hay campo de estado explícito.

---

## UC007 — Gestionar fichas

**Módulo:** Fichas y materias | **Actor:** Administrador | **Estado:** Parcial

**Feature:** CRUD de fichas (en el código, `Grade`). Una ficha agrupa aprendices de un programa y define jornada, modalidad y rango de fechas.

**Endpoints:**

| Método | Ruta                 | Acceso                            | Descripción                                                                      |
| ------ | -------------------- | --------------------------------- | -------------------------------------------------------------------------------- |
| GET    | `/api/grades`        | `ROLE_ADMIN` o `ROLE_USER`        | Lista paginada de fichas (relaciones cargadas con `eagerload=true` por defecto). |
| GET    | `/api/grades/active` | Autenticado                       | Lista de fichas con estado `ACTIVA` (sin paginar).                               |
| GET    | `/api/grades/{id}`   | `ROLE_ADMIN` o `ROLE_USER`        | Detalle con relaciones.                                                          |
| POST   | `/api/grades`        | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Crea; `201` con el recurso.                                                      |
| PUT    | `/api/grades/{id}`   | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Reemplaza; `200`.                                                                |
| PATCH  | `/api/grades/{id}`   | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Actualización parcial; `200`.                                                    |
| DELETE | `/api/grades/{id}`   | `ROLE_ADMIN` o `ROLE_COORDINATOR` | Elimina; `204`.                                                                  |

**Request — `POST /api/grades`**

```json
{
  "code": "3412345",
  "state": "ACTIVA",
  "startDate": "2026-09-01",
  "endDate": "2027-03-31",
  "program": { "id": "665f1c2a9e13b7a1f2c8d9e30" },
  "modality": { "id": "665f1c2a9e13b7a1f2c8d9e50" },
  "timeSlot": { "id": "665f1c2a9e13b7a1f2c8d9e60" }
}
```

| Campo       | Tipo                  | Obligatorio | Reglas                                                                                             |
| ----------- | --------------------- | ----------- | -------------------------------------------------------------------------------------------------- |
| `code`      | string                | Sí          | `@NotNull`, máximo 20. No se valida que sea numérico ni único.                                     |
| `state`     | string                | Sí          | `@NotNull`; enum `StateGrade`: `ACTIVA`, `INACTIVA`, `APLAZADA`. Lo define el cliente.             |
| `startDate` | string (`YYYY-MM-DD`) | Sí          | `@NotNull`. No se valida contra `endDate` ni contra hoy.                                           |
| `endDate`   | string (`YYYY-MM-DD`) | Sí          | `@NotNull`.                                                                                        |
| `program`   | objeto                | Sí          | `@NotNull`; referencia por `id`. En creación, si el programa está inactivo: `400 programInactive`. |
| `modality`  | objeto                | Sí          | `@NotNull`; referencia por `id`.                                                                   |
| `timeSlot`  | objeto                | Sí          | `@NotNull`; referencia por `id`.                                                                   |

**Response:** `201 Created`

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9e70",
  "code": "3412345",
  "state": "ACTIVA",
  "startDate": "2026-09-01",
  "endDate": "2027-03-31",
  "program": {
    "id": "665f1c2a9e13b7a1f2c8d9e30",
    "name": "Análisis y Desarrollo de Software",
    "initials": "ADSO",
    "code": "228118",
    "trimesters": 6,
    "status": true
  },
  "modality": {
    "id": "665f1c2a9e13b7a1f2c8d9e50",
    "name": "Presencial",
    "isActive": true
  },
  "timeSlot": {
    "id": "665f1c2a9e13b7a1f2c8d9e60",
    "name": "Diurna",
    "startTime": "06:00:00",
    "endTime": "18:00:00",
    "isActive": true
  }
}
```

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idinvalid`, `400 error.idnotfound`, `400 error.programInactive` ("No se pueden crear fichas para un programa inactivo"), `400 error.validation`; `403`; `404`.

**Notas / lo que se necesita:** no existe cálculo automático de Pendiente/Activa/Finalizada por fechas, ni acciones de Aplazar, Reanudar o Cancelar, ni los cinco estados del UC (el enum solo tiene `ACTIVA`, `INACTIVA`, `APLAZADA`). No se valida el código numérico ni su unicidad (E1), ni las reglas de edición por estado (A1), ni la guarda de eliminación por aprendices o asistencias. El guardado de programa activo solo aplica en `POST`; `PUT` y `PATCH` no lo revalidan. `GET /api/grades/active` quedó sin `@PreAuthorize`: cualquier usuario autenticado puede consultarlo.

---

## UC015 — Gestionar materias

**Módulo:** Fichas y materias | **Actor:** Administrador | **Estado:** Parcial

**Feature:** CRUD de materias (`ClassSection`) por ficha, con instructor asignable y horarios por trimestre. Las materias viven dentro de una única ficha.

**Endpoints:**

| Método                | Ruta                                                  | Acceso                                               | Descripción                                      |
| --------------------- | ----------------------------------------------------- | ---------------------------------------------------- | ------------------------------------------------ |
| GET                   | `/api/class-sections`                                 | Autenticado                                          | Lista paginada de materias.                      |
| GET                   | `/api/class-sections/{id}`                            | Autenticado                                          | Detalle.                                         |
| GET                   | `/api/class-sections/mine`                            | `ROLE_INSTRUCTOR`, `ROLE_COORDINATOR` o `ROLE_ADMIN` | Materias del instructor autenticado (ver UC017). |
| POST                  | `/api/class-sections`                                 | `ROLE_ADMIN` o `ROLE_COORDINATOR`                    | Crea; `201`.                                     |
| PUT                   | `/api/class-sections/{id}`                            | `ROLE_ADMIN` o `ROLE_COORDINATOR`                    | Reemplaza; `200`.                                |
| PATCH                 | `/api/class-sections/{id}`                            | `ROLE_ADMIN` o `ROLE_COORDINATOR`                    | Actualización parcial; `200`.                    |
| DELETE                | `/api/class-sections/{id}`                            | `ROLE_ADMIN` o `ROLE_COORDINATOR`                    | Elimina; `204`.                                  |
| POST/PUT/PATCH/DELETE | `/api/class-schedules`                                | `ROLE_ADMIN` o `ROLE_COORDINATOR`                    | CRUD de horarios por trimestre.                  |
| GET                   | `/api/class-schedules`, `/api/class-schedules/{id}`   | Autenticado                                          | Consulta de horarios.                            |
| POST/PUT/PATCH/DELETE | `/api/class-exceptions`                               | `ROLE_ADMIN` o `ROLE_COORDINATOR`                    | CRUD de excepciones no lectivas.                 |
| GET                   | `/api/class-exceptions`, `/api/class-exceptions/{id}` | Autenticado                                          | Consulta de excepciones.                         |

**Request — `POST /api/class-sections`**

```json
{
  "subjectName": "Programación orientada a objetos",
  "isActive": true,
  "instructor": { "id": "665f1c2a9e13b7a1f2c8d9e80" },
  "grade": { "id": "665f1c2a9e13b7a1f2c8d9e70" }
}
```

| Campo         | Tipo    | Obligatorio | Reglas                                                                                 |
| ------------- | ------- | ----------- | -------------------------------------------------------------------------------------- |
| `subjectName` | string  | Sí          | `@NotNull`, máximo 200. No se valida unicidad dentro de la ficha.                      |
| `isActive`    | boolean | Sí          | `@NotNull`; lo define el cliente.                                                      |
| `instructor`  | objeto  | **Sí**      | `@NotNull`; el UC permite crearla sin instructor. Referencia a `UserProfile` por `id`. |
| `grade`       | objeto  | Sí          | `@NotNull`; ficha a la que pertenece.                                                  |

**Request — `POST /api/class-schedules`**

```json
{
  "dayOfWeek": "LUNES",
  "startTime": "08:00:00",
  "endTime": "10:00:00",
  "trimester": { "id": "665f1c2a9e13b7a1f2c8d9e40" },
  "classSection": { "id": "665f1c2a9e13b7a1f2c8d9e90" }
}
```

| Campo                   | Tipo                | Obligatorio | Reglas                                                                      |
| ----------------------- | ------------------- | ----------- | --------------------------------------------------------------------------- |
| `dayOfWeek`             | string              | No          | Enum `DayOfWeek`: `LUNES` … `DOMINGO`. No tiene `@NotNull`.                 |
| `startTime` / `endTime` | string (`HH:mm:ss`) | Sí          | `@NotNull`; no se validan contra la jornada, el mismo día ni solapamientos. |
| `trimester`             | objeto              | Sí          | `@NotNull`; referencia por `id`.                                            |
| `classSection`          | objeto              | Sí          | `@NotNull`; referencia por `id`.                                            |

**Request — `POST /api/class-exceptions`** (excepción no lectiva)

```json
{
  "date": "2026-09-21",
  "reason": "Festivo",
  "classSection": { "id": "665f1c2a9e13b7a1f2c8d9e90" }
}
```

| Campo          | Tipo                  | Obligatorio | Reglas                  |
| -------------- | --------------------- | ----------- | ----------------------- |
| `date`         | string (`YYYY-MM-DD`) | Sí          | `@NotNull`.             |
| `reason`       | string                | Sí          | `@NotNull`, máximo 200. |
| `classSection` | objeto                | Sí          | `@NotNull`.             |

**Response:** `201 Created` con el DTO creado (`ClassSectionDTO` con `id`, `subjectName`, `isActive`, `instructor`, `grade`; `ClassScheduleDTO` con `id`, `dayOfWeek`, `startTime`, `endTime`, `trimester`, `classSection`; `ClassExceptionDTO` con `id`, `date`, `reason`, `classSection`). Las listas paginan con `X-Total-Count` y `Link`; los CRUD genéricos devuelven los errores `idexists`, `idnull`, `idinvalid`, `idnotfound` y `error.validation`.

**Notas / lo que se necesita:** no están implementadas las reglas centrales del UC: nombre único por ficha (E2), horario dentro de la jornada (E3), no solapamiento (E4), sesión sin cruzar medianoche (E5), bloqueo de horarios de trimestre cerrado (E6) ni la restricción de crear/modificar solo en fichas Pendiente/Activa (E1). El profesor es obligatorio en la API, aunque el UC lo quiere opcional. `DELETE` de materia no verifica asistencias ni borra horarios/excepciones en cascada (el UC lo exige). Las excepciones no lectivas quedan bajo roles administrativos, no del instructor como indica UC009.

---

## UC008 — Gestionar aprendices

**Módulo:** Aprendices e instructor | **Actor:** Administrador | **Estado:** Parcial

**Feature:** Vinculación y desvinculación de aprendices a fichas mediante el registro `Apprentice`, con estado académico.

**Endpoints:**

| Método | Ruta                    | Acceso                                               | Descripción                   |
| ------ | ----------------------- | ---------------------------------------------------- | ----------------------------- |
| GET    | `/api/apprentices`      | Autenticado                                          | Lista paginada de vínculos.   |
| GET    | `/api/apprentices/{id}` | Autenticado                                          | Detalle.                      |
| POST   | `/api/apprentices`      | `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_INSTRUCTOR` | Crea el vínculo; `201`.       |
| PUT    | `/api/apprentices/{id}` | `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_INSTRUCTOR` | Reemplaza; `200`.             |
| PATCH  | `/api/apprentices/{id}` | `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_INSTRUCTOR` | Actualización parcial; `200`. |
| DELETE | `/api/apprentices/{id}` | `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_INSTRUCTOR` | Elimina el vínculo; `204`.    |

**Request — `POST /api/apprentices`**

```json
{
  "stateAcademic": "MATRICULADO",
  "student": { "id": "665f1c2a9e13b7a1f2c8d9ea0" },
  "grade": { "id": "665f1c2a9e13b7a1f2c8d9e70" }
}
```

| Campo           | Tipo   | Obligatorio | Reglas                                                                                                                                                                              |
| --------------- | ------ | ----------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `stateAcademic` | string | Sí          | `@NotNull`; enum `StateAcademic`: `MATRICULADO`, `RETIRO_VOLUNTARIO`, `APLAZADO`, `CANCELADO`, `INACTIVO`. El cliente define el estado; la vinculación debería nacer `MATRICULADO`. |
| `student`       | objeto | Sí          | `@NotNull`; referencia al `UserProfile` del aprendiz.                                                                                                                               |
| `grade`         | objeto | Sí          | `@NotNull`; referencia a la ficha.                                                                                                                                                  |

**Response:** `201 Created`

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9eb0",
  "stateAcademic": "MATRICULADO",
  "student": {
    "id": "665f1c2a9e13b7a1f2c8d9ea0",
    "firstName": "Ana",
    "firstLastName": "Gómez",
    "documentNumber": "1029384756",
    "phoneNumber": "3001234567",
    "documentType": { "id": "64f1c2a9e13b7a1f2c8d9e01", "name": "Cédula de ciudadanía", "initials": "CC" }
  },
  "grade": {
    "id": "665f1c2a9e13b7a1f2c8d9e70",
    "code": "3412345",
    "state": "ACTIVA",
    "startDate": "2026-09-01",
    "endDate": "2027-03-31"
  }
}
```

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idinvalid`, `400 error.idnotfound`, `400 error.validation`; `403`; `404`.

**Notas / lo que se necesita:** el flujo del UC no está modelado: la API no recibe el número de documento ni valida que el aprendiz exista/esté activo (E1), no impide duplicados ni reingresos a la misma ficha (E2), no verifica el estado de la ficha (E3) y no distingue si la desvinculación debe eliminar o conservar el registro según asistencias. Además, `ROLE_INSTRUCTOR` puede crear, modificar y eliminar vínculos, algo reservado al Administrador en el UC. No hay endpoint para "desvincular con motivo" ni para listar fichas de un aprendiz.

---

## UC017 — Consultar mis fichas y materias

**Módulo:** Aprendices e instructor | **Actor:** Instructor | **Estado:** Parcial

**Feature:** Consulta de solo lectura de las materias asignadas al instructor y, a través de ellas, de las fichas en las que participa.

**Endpoints:**

| Método | Ruta                       | Acceso                                               | Descripción                                                |
| ------ | -------------------------- | ---------------------------------------------------- | ---------------------------------------------------------- |
| GET    | `/api/class-sections/mine` | `ROLE_INSTRUCTOR`, `ROLE_COORDINATOR` o `ROLE_ADMIN` | Materias del instructor autenticado, con la ficha anidada. |
| GET    | `/api/class-sections/{id}` | Autenticado                                          | Detalle de una materia.                                    |
| GET    | `/api/grades`              | `ROLE_ADMIN` o `ROLE_USER`                           | Fichas visibles (paginado).                                |

**Response — `GET /api/class-sections/mine`:** `200 OK`

```json
[
  {
    "id": "665f1c2a9e13b7a1f2c8d9e90",
    "subjectName": "Programación orientada a objetos",
    "isActive": true,
    "instructor": {
      "id": "665f1c2a9e13b7a1f2c8d9e80",
      "firstName": "Carlos",
      "firstLastName": "Pérez",
      "documentNumber": "1029384756",
      "phoneNumber": "3001234567",
      "documentType": { "id": "64f1c2a9e13b7a1f2c8d9e01", "name": "Cédula de ciudadanía", "initials": "CC" }
    },
    "grade": {
      "id": "665f1c2a9e13b7a1f2c8d9e70",
      "code": "3412345",
      "state": "ACTIVA",
      "startDate": "2026-09-01",
      "endDate": "2027-03-31"
    }
  }
]
```

**Errores:** `401` sin sesión; `403` para roles sin permiso; las materias se resuelven por el perfil del usuario autenticado, por lo que un instructor sin perfil o sin asignaciones recibe `200` con arreglo vacío.

**Notas / lo que se necesita:** no hay endpoint dedicado de "mis fichas": el frontend debe derivarlas de las materias. No existe búsqueda por número de ficha (flujo alternativo del UC), no se filtran materias inactivas ni fichas finalizadas, y el listado no se pagina. La asignación se consulta por `UserProfile`, no se valida el estado de la cuenta del instructor.

---

## UC009 — Gestionar listas de asistencia

**Módulo:** Asistencia | **Actor:** Instructor | **Estado:** Parcial

**Feature:** Registro y consulta de asistencia por materia y fecha de sesión. En el código la asistencia es un CRUD plano; no existe aún la sesión con guardado masivo.

**Endpoints:**

| Método | Ruta                    | Acceso                                               | Descripción                   |
| ------ | ----------------------- | ---------------------------------------------------- | ----------------------------- |
| GET    | `/api/attendances`      | Autenticado                                          | Lista paginada de registros.  |
| GET    | `/api/attendances/{id}` | Autenticado                                          | Detalle.                      |
| POST   | `/api/attendances`      | `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_INSTRUCTOR` | Crea un registro; `201`.      |
| PUT    | `/api/attendances/{id}` | `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_INSTRUCTOR` | Reemplaza; `200`.             |
| PATCH  | `/api/attendances/{id}` | `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_INSTRUCTOR` | Actualización parcial; `200`. |
| DELETE | `/api/attendances/{id}` | `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_INSTRUCTOR` | Elimina; `204`.               |

**Request — `POST /api/attendances`**

```json
{
  "date": "2026-09-15",
  "stateAttendance": "PRESENTE",
  "classSection": { "id": "665f1c2a9e13b7a1f2c8d9e90" },
  "student": { "id": "665f1c2a9e13b7a1f2c8d9ea0" }
}
```

| Campo                     | Tipo                  | Obligatorio | Reglas                                                                                                    |
| ------------------------- | --------------------- | ----------- | --------------------------------------------------------------------------------------------------------- |
| `date`                    | string (`YYYY-MM-DD`) | Sí          | `@NotNull`; no se valida contra trimestre, ficha, futuro ni excepciones.                                  |
| `stateAttendance`         | string                | Sí          | `@NotNull`; enum `StateAttendance`: `PRESENTE`, `FALLA`, `JUSTIFICADA`, `TARDE`. El UC usa `A`, `F`, `J`. |
| `classSection`            | objeto                | Sí          | `@NotNull`; materia.                                                                                      |
| `student`                 | objeto                | Sí          | `@NotNull`; aprendiz (`UserProfile`).                                                                     |
| `modifiedByJustification` | objeto                | No          | Referencia a la justificación que cambió el estado a justificada.                                         |

**Response:** `201 Created`

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9ec0",
  "date": "2026-09-15",
  "stateAttendance": "PRESENTE",
  "classSection": {
    "id": "665f1c2a9e13b7a1f2c8d9e90",
    "subjectName": "Programación orientada a objetos",
    "isActive": true
  },
  "student": {
    "id": "665f1c2a9e13b7a1f2c8d9ea0",
    "firstName": "Ana",
    "firstLastName": "Gómez",
    "documentNumber": "1029384756",
    "phoneNumber": "3001234567",
    "documentType": { "id": "64f1c2a9e13b7a1f2c8d9e01", "name": "Cédula de ciudadanía", "initials": "CC" }
  },
  "modifiedByJustification": null
}
```

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idinvalid`, `400 error.idnotfound`, `400 error.validation`; `403`; `404`.

**Notas / lo que se necesita:** faltan las reglas del UC: guardado por sesión con todos los aprendices (E5), default `PRESENTE` (A), estado "sesión incompleta", validación de fecha dentro del trimestre activo y del rango de la ficha (E2/E3), bloqueo por trimestre cerrado (E1), verificación de excepción no lectiva (E4), restricción al instructor asignado y auditoría de cambios con valor anterior/nuevo. Cualquier usuario autenticado puede leer asistencias de cualquier ficha y `DELETE` está permitido: el UC no contempla borrar registros de asistencia. Las excepciones no lectivas se administran con el CRUD genérico de UC015.

---

## UC011 — Gestionar asistencia (Aprendiz)

**Módulo:** Justificaciones | **Actor:** Aprendiz | **Estado:** Parcial

**Feature:** El aprendiz presenta justificaciones de inasistencia con tipo, rango de fechas, descripción y soporte. El modelo separa la cabecera (`Justification`) de las decisiones por materia (`JustificationDetails`).

**Endpoints:**

| Método                | Ruta                              | Acceso                                               | Descripción                               |
| --------------------- | --------------------------------- | ---------------------------------------------------- | ----------------------------------------- |
| GET                   | `/api/justifications`             | Autenticado                                          | Lista paginada (sin filtro por aprendiz). |
| GET                   | `/api/justifications/{id}`        | Autenticado                                          | Detalle.                                  |
| POST                  | `/api/justifications`             | `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_APPRENTICE` | Crea la justificación; `201`.             |
| PUT                   | `/api/justifications/{id}`        | `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_APPRENTICE` | Reemplaza; `200`.                         |
| PATCH                 | `/api/justifications/{id}`        | `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_APPRENTICE` | Actualización parcial; `200`.             |
| DELETE                | `/api/justifications/{id}`        | `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_APPRENTICE` | Elimina; `204`.                           |
| GET                   | `/api/justification-details`      | Autenticado                                          | Lista paginada de las partes por materia. |
| GET                   | `/api/justification-details/{id}` | Autenticado                                          | Detalle de una parte.                     |
| POST/PUT/PATCH/DELETE | `/api/justification-details`      | Autenticado (sin `@PreAuthorize`)                    | CRUD genérico de partes.                  |

**Request — `POST /api/justifications`**

```json
{
  "description": "Incapacidad médica por gripe",
  "startDate": "2026-09-10",
  "endDate": "2026-09-12",
  "evidence": "JVBERi0xLjQK...",
  "evidenceContentType": "application/pdf",
  "justificationType": { "id": "665f1c2a9e13b7a1f2c8d9ed0" },
  "student": { "id": "665f1c2a9e13b7a1f2c8d9ea0" }
}
```

| Campo                   | Tipo                        | Obligatorio | Reglas                                                                              |
| ----------------------- | --------------------------- | ----------- | ----------------------------------------------------------------------------------- |
| `description`           | string                      | Sí          | `@NotNull`, máximo 300.                                                             |
| `startDate` / `endDate` | string (`YYYY-MM-DD`)       | Sí          | `@NotNull`; no se valida orden ni plazo.                                            |
| `evidence`              | string (base64 de `byte[]`) | No          | Sin validación de formato ni de tamaño (5 MB en el UC).                             |
| `evidenceContentType`   | string                      | Sí          | `@NotNull`; tipo MIME declarado por el cliente.                                     |
| `justificationType`     | objeto                      | Sí          | `@NotNull`; tipo del catálogo UC016.                                                |
| `student`               | objeto                      | Sí          | `@NotNull`; perfil del aprendiz. La API no verifica que sea el usuario autenticado. |

**Request — `POST /api/justification-details`** (parte por materia)

```json
{
  "stateJustification": "PENDIENTE",
  "rejectionReason": "",
  "correctionText": "",
  "responseDate": "2026-09-11T15:04:05Z",
  "classSection": { "id": "665f1c2a9e13b7a1f2c8d9e90" },
  "justification": { "id": "665f1c2a9e13b7a1f2c8d9ee0" }
}
```

| Campo                          | Tipo              | Obligatorio | Reglas                                                                                                    |
| ------------------------------ | ----------------- | ----------- | --------------------------------------------------------------------------------------------------------- |
| `stateJustification`           | string            | No          | Enum `StateJustification`: `PENDIENTE`, `ACEPTADA`, `RECHAZADA`. No admite `CANCELADA` ni marca de plazo. |
| `rejectionReason`              | string            | Sí          | `@NotNull`, máximo 300, aunque no haya rechazo.                                                           |
| `correctionText`               | string            | Sí          | `@NotNull`, máximo 300, aunque no haya corrección.                                                        |
| `correctionFileUrl`            | string (base64)   | No          | Archivo de corrección; sin validación de formato/tamaño.                                                  |
| `correctionFileUrlContentType` | string            | No          | MIME del archivo de corrección.                                                                           |
| `responseDate`                 | string (ISO-8601) | Sí          | `@NotNull`; fecha de decisión, aunque la parte esté pendiente.                                            |
| `classSection`                 | objeto            | Sí          | `@NotNull`; materia afectada.                                                                             |
| `justification`                | objeto            | Sí          | `@NotNull`; justificación cabecera.                                                                       |

**Response:** `201 Created`

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9ee0",
  "description": "Incapacidad médica por gripe",
  "startDate": "2026-09-10",
  "endDate": "2026-09-12",
  "evidence": "JVBERi0xLjQK...",
  "evidenceContentType": "application/pdf",
  "justificationType": { "id": "665f1c2a9e13b7a1f2c8d9ed0", "name": "Incapacidad médica", "limitPerTrimester": 3, "state": "ACTIVO" },
  "student": {
    "id": "665f1c2a9e13b7a1f2c8d9ea0",
    "firstName": "Ana",
    "firstLastName": "Gómez",
    "documentNumber": "1029384756",
    "phoneNumber": "3001234567",
    "documentType": { "id": "64f1c2a9e13b7a1f2c8d9e01", "name": "Cédula de ciudadanía", "initials": "CC" }
  }
}
```

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idinvalid`, `400 error.idnotfound`, `400 error.validation`; `403`; `404`. No existen claves de error para cupo, plazo, trimestre cerrado, ficha no matriculada ni subsanación.

**Notas / lo que se necesita:** no hay validación de cupo por tipo, marca en tiempo/fuera de tiempo, bloqueo de trimestres cerrados, verificación de matrícula, plazo de subsanación ni notificaciones. Cualquier usuario autenticado puede crear/modificar partes (`JustificationDetails` no tiene `@PreAuthorize`) y listar justificaciones ajenas; la edición no se bloquea tras una decisión y `DELETE` no implementa la cancelación con liberación de cupo. La corrección de una parte rechazada no vuelve a `PENDIENTE` de forma automática: el cliente debe hacer el `PATCH`.

---

## UC010 — Gestionar justificaciones

**Módulo:** Justificaciones | **Actor:** Instructor | **Estado:** Parcial

**Feature:** Revisión y decisión de las justificaciones recibidas, con una decisión independiente por materia. En el backend existe el modelo de decisión, pero no el flujo del instructor.

**Endpoints:** no hay endpoint de decisión para el instructor. `JustificationResource` solo permite `ROLE_ADMIN`, `ROLE_COORDINATOR` o `ROLE_APPRENTICE`, de modo que un instructor autenticado recibe `403` al intentar responder. `JustificationDetailsResource` existe como CRUD genérico sin `@PreAuthorize`, por lo que cualquier usuario autenticado puede escribir una parte, pero sin reglas de decisión.

**Propuesta** (no implementada): `PATCH /api/justification-details/{id}/decision` — registra la decisión del instructor asignado a la materia. Cuerpo sugerido:

```json
{
  "stateJustification": "RECHAZADA",
  "rejectionReason": "Soporte no legible",
  "outOfTimeReason": null
}
```

También se requiere un listado de pendientes por instructor (`GET /api/justification-details/pending`) y la conversión automática de las fallas `FALLA` a `JUSTIFICADA` dentro del período aprobado.

**Notas / lo que se necesita:** no se valida que quien decide sea el instructor de la materia (E4), no se exige motivo al rechazar (E1), no se impide decidir una parte ya no pendiente (E2), no se registra decisión demorada ni se resuelven alertas al aprobar. No hay notificaciones al aprendiz por cambio de estado.

---

## UC013 — Gestionar alertas de inasistencia

**Módulo:** Alertas y notificaciones | **Actor:** Sistema / Instructor / Aprendiz | **Estado:** No implementado

**Feature:** Generación y resolución automática de alertas por fallas consecutivas (materia) y acumuladas (ficha), con estados No leída, Leída, Atendida y Resuelta automáticamente.

**Endpoints:** no existen. El modelo de alertas está listado como pendiente en `docs/use-cases.md` y no hay entidad `Alerta`, ni endpoints de consulta, lectura o atención. Como legado existe `DesertionCounter` con CRUD en `/api/desertion-counters` (sin `@PreAuthorize`, accesible para cualquier usuario autenticado), que el UC pide descartar y no cubre la lógica de alertas.

**Propuesta** (no implementada), coherente con los roles del UC:

| Método | Ruta propuesta                                                       | Propósito                                                                                      |
| ------ | -------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| GET    | `/api/alerts?type=&state=&gradeId=&studentId=&from=&to=&page=&size=` | Lista filtrable y paginada; el instructor ve solo sus materias/fichas y el Administrador todo. |
| GET    | `/api/alerts/{id}`                                                   | Detalle; su lectura puede marcar la alerta como leída (A2).                                    |
| PATCH  | `/api/alerts/{id}/attend`                                            | Marca Atendida con observación (A3).                                                           |
| GET    | `/api/alerts/students/{studentId}`                                   | Historial de alertas de un aprendiz (A5).                                                      |

Forma sugerida de la alerta (sujeta al modelo nuevo):

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9f00",
  "type": "CONSECUTIVAS",
  "state": "NO_LEIDA",
  "studentId": "665f1c2a9e13b7a1f2c8d9ea0",
  "classSectionId": "665f1c2a9e13b7a1f2c8d9e90",
  "gradeId": null,
  "trimesterId": "665f1c2a9e13b7a1f2c8d9e40",
  "absenceCount": 3,
  "threshold": 3,
  "generatedAt": "2026-09-11T15:04:05Z",
  "resolvedAt": null
}
```

**Notas / lo que se necesita:** requieren implementación la entidad, los umbrales en `GlobalConfiguration` (hoy solo hay dos parámetros, ninguno de alertas), la evaluación al guardar asistencia o aprobar justificaciones, la regla de alerta activa única por combinación y las notificaciones asociadas. Los detalles finos (nombres de campos definitivos) quedan `por confirmar` hasta que exista el modelo.

---

## UC018 — Gestionar notificaciones

**Módulo:** Alertas y notificaciones | **Actor:** Usuario | **Estado:** No implementado

**Feature:** Bandeja in-app de notificaciones del usuario, con estado de entrega interno y estado de lectura independiente.

**Endpoints:** no existen. La entidad `Notificacion` sí existe en el modelo (`user`, `tipo`, `estado`, `mensaje`), pero no tiene controlador. Hoy solo se usa internamente: al crear un usuario, si falla el correo de credenciales se guarda una notificación con `tipo: CREDENTIALS` y `estado: PENDIENTE`. Los enums actuales son `NotificacionTipo`: `CREDENTIALS`; `NotificacionEstado`: `PENDIENTE`, `ENVIADA`, `REINTENTAR`. No hay estado de lectura ni referencia al objeto de origen.

**Propuesta** (no implementada):

| Método | Ruta propuesta                                         | Propósito                                                                |
| ------ | ------------------------------------------------------ | ------------------------------------------------------------------------ |
| GET    | `/api/notifications?read=&type=&from=&to=&page=&size=` | Bandeja del usuario autenticado, ordenada de más reciente a más antigua. |
| PATCH  | `/api/notifications/{id}/read`                         | Marca una notificación como leída.                                       |
| PATCH  | `/api/notifications/read-all`                          | Marca todas las no leídas como leídas (A1).                              |

Forma sugerida (sujeta a ampliación del modelo):

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9f10",
  "tipo": "ALERTA",
  "mensaje": "Alerta de inasistencia acumulada",
  "estado": "ENVIADA",
  "read": false,
  "referenceType": "ALERT",
  "referenceId": "665f1c2a9e13b7a1f2c8d9f00",
  "createdDate": "2026-09-11T15:04:05Z"
}
```

**Notas / lo que se necesita:** falta el estado de lectura, la referencia al objeto de origen y los tipos para alertas y justificaciones. Tampoco existe endpoint para que el Administrador reenvíe la notificación de credenciales pendiente (caso E7 de UC006).

---

## UC023 — Consultar dashboard

**Módulo:** Dashboard | **Actor:** Usuario (según rol) | **Estado:** Parcial

**Feature:** Panel de resumen por rol, calculado al momento de la consulta. Hoy solo el panel de Administrador tiene datos reales; para Instructor y Aprendiz el servicio devuelve el payload de Administrador como solución temporal.

**Endpoints:**

| Método | Ruta             | Acceso                                              | Descripción                                           |
| ------ | ---------------- | --------------------------------------------------- | ----------------------------------------------------- |
| GET    | `/api/dashboard` | `ROLE_ADMIN`, `ROLE_INSTRUCTOR` o `ROLE_APPRENTICE` | Devuelve el panel correspondiente al rol autenticado. |

**Response — `GET /api/dashboard`:** `200 OK`

```json
{
  "kpis": {
    "totalUsers": 128,
    "activeGrades": 6,
    "totalPrograms": 4,
    "totalModalities": 3
  },
  "recentGrades": [
    {
      "id": "665f1c2a9e13b7a1f2c8d9e70",
      "code": "3412345",
      "programName": "Análisis y Desarrollo de Software",
      "instructorName": "Carlos Pérez",
      "state": "ACTIVA"
    }
  ]
}
```

El panel de Administrador incluye KPIs y las últimas 5 fichas creadas (`recentGrades`). El método `role()` de la interfaz `DashboardDTO` no se serializa como campo del JSON.

**Errores:** `401` sin sesión; `403` con un rol distinto de los tres admitidos.

**Notas / lo que se necesita:** faltan los paneles de Instructor (justificaciones pendientes, alertas activas, clases de hoy, materias y aprendices a cargo) y de Aprendiz (porcentaje de asistencia, fallas y umbral, justificaciones por estado, próximas clases). No hay cálculo por trimestre activo ni manejo de "No hay un trimestre activo". La restricción de que cada rol vea solo sus datos no aplica todavía: Instructor y Aprendiz reciben los indicadores globales del Administrador.
