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
- **Endpoints públicos:** `POST /api/authenticate`, `GET /api/authenticate`, `/api/register`, `/api/activate`, `/api/account/reset-password/init`, `/api/account/reset-password/finish` y `GET /api/document-types/**`. Los métodos de escritura de `/api/document-types/**` siguen protegidos por `@PreAuthorize` (solo `ROLE_ADMIN`).
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
| [UC021](#uc021--gestionar-modalidades)              | Gestionar modalidades              | Implementado    |
| [UC022](#uc022--gestionar-tipos-de-documento)       | Gestionar tipos de documento       | Implementado    |
| [UC016](#uc016--gestionar-tipos-de-justificación)   | Gestionar tipos de justificación   | Implementado    |
| [UC006](#uc006--gestionar-perfiles)                 | Gestionar perfiles                 | Parcial         |
| [UC012](#uc012--gestionar-programas-de-aprendizaje) | Gestionar programas de aprendizaje | Implementado    |
| [UC014](#uc014--gestionar-trimestres-académicos)    | Gestionar trimestres académicos    | Implementado    |
| [UC007](#uc007--gestionar-fichas)                   | Gestionar fichas                   | Implementado    |
| [UC015](#uc015--gestionar-materias)                 | Gestionar materias                 | Implementado    |
| [UC008](#uc008--gestionar-aprendices)               | Gestionar aprendices               | Implementado    |
| [UC017](#uc017--consultar-mis-fichas-y-materias)    | Consultar mis fichas y materias    | Implementado    |
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

**Notas / lo que se necesita:** la cuenta se crea con `ROLE_USER` + `ROLE_APPRENTICE` y `activated = true`; el envío de correo de activación está comentado en el código, aunque `GET /api/activate` existe. No hay `mustChangePassword`. El documento duplicado sí tiene mensajes diferenciados: `error.documentnumberexists` cuando el par tipo + número pertenece a una cuenta **activa** y `error.documentnumberinactive` cuando pertenece a una cuenta **desactivada**. Todas las validaciones (incluida la del documento duplicado) corren antes de la primera escritura, por lo que un registro rechazado por validación no deja usuario ni perfil parciales. El backend cubre el flujo de UC001; quedan a cargo del frontend el formulario en sí y la traducción de las claves de error (`emailrequired`, `documentnumberexists`, `documentnumberinactive`, `documentTypeInactive`); los tipos activos se obtienen con `GET /api/document-types/active` (ver UC022).

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

**Notas / lo que se necesita:** la configuración global es un **singleton** con `id` fijo `global-configuration`. La lectura y la escritura siempre operan sobre ese documento: `GET` lo devuelve (y lo re-crea con defaults si falta) y `PATCH` lo actualiza aunque el cuerpo no envíe `id`; un `id` distinto responde `400 error.idinvalid`. El modelo expone los cuatro parámetros del UC. Los plazos `studentJustificationDays` e `instructorResponseDays` se validan en el borde de la API con rango **1–30 días** (`@Min(1)` + `@Max(30)`), de modo que un valor fuera de rango produce un error por campo y nunca llega a la persistencia. Los umbrales `consecutiveAbsenceAlertThreshold` (default 3) y `accumulatedAbsenceAlertThreshold` (default 5) los consume UC013 y solo exigen **mínimo 1** (sin máximo, el UC no define tope). Tanto la lectura como la escritura son solo del Administrador: `GET` y `PATCH` responden `403` a cualquier otro rol, consistente con el actor del UC y con E2.

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

**Módulo:** Configuración y catálogos | **Actor:** Administrador | **Estado:** Implementado

**Feature:** CRUD del catálogo de modalidades de formación que se seleccionan al crear fichas.

**Endpoints:**

| Método | Ruta                     | Acceso       | Descripción                   |
| ------ | ------------------------ | ------------ | ----------------------------- |
| GET    | `/api/modalities`        | Autenticado  | Lista **paginada** de modalidades (20 por página por defecto). |
| GET    | `/api/modalities/active` | Autenticado  | Lista de modalidades activas. |
| GET    | `/api/modalities/{id}`   | Autenticado  | Detalle.                      |
| POST   | `/api/modalities`        | `ROLE_ADMIN` | Crea la modalidad (nace Activa); `201` con el recurso creado. |
| PUT    | `/api/modalities`        | `ROLE_ADMIN` | Reemplaza; el `id` viaja en el cuerpo; `200`. |
| PATCH  | `/api/modalities`        | `ROLE_ADMIN` | Actualización parcial; el `id` viaja en el cuerpo; `200`. |
| DELETE | `/api/modalities/{id}`   | `ROLE_ADMIN` | Elimina; `204`. Bloquea la eliminación si la modalidad está asignada a fichas (`400 error.modalityInUse`). |

**Request — `POST /api/modalities`**

```json
{
  "name": "Presencial"
}
```

| Campo      | Tipo    | Obligatorio | Reglas                                                                                                                       |
| ---------- | ------- | ----------- | ---------------------------------------------------------------------------------------------------------------------------- |
| `name`     | string  | Sí          | `@NotBlank`, máximo 50. **Nombre único** (se compara sin distinguir mayúsculas); un duplicado responde `400 error.modalityNameAlreadyUsed`. |
| `isActive` | boolean | No          | Ignorado en la creación: el backend siempre crea la modalidad como **Activa** (`isActive = true`).                           |

**Response:** `201 Created` con el `ModalityDTO` (`id`, `name`, `isActive`). `GET /api/modalities` es **paginado**: acepta `page` (base 0), `size` y `sort=campo,asc|desc` (20 por defecto) y devuelve un arreglo JSON con la página actual más las cabeceras `X-Total-Count` y `Link`. `GET /api/modalities/active` sigue devolviendo el arreglo completo sin paginar (selector).

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idnotfound`, `400 error.validation`; `400 error.modalityNameAlreadyUsed` (nombre duplicado, E1); `400 error.modalityInUse` (la modalidad está asignada a una o más fichas y no puede eliminarse, E2); `403` sin rol permitido; `404` en detalle inexistente.

**Notas / lo que se necesita:** `PUT` y `PATCH` toman el `id` del cuerpo (la ruta es `/api/modalities`, sin `{id}`); si falta, responde `400 error.idnull`, y si no existe, `400 error.idnotfound`. Está implementada la unicidad de nombre (E1): el backend recorta el nombre y lo compara sin distinguir mayúsculas, tanto en `PUT`/`PATCH` como en `POST`; un duplicado responde `400 error.modalityNameAlreadyUsed` y un nombre vacío o en blanco responde `400 error.validation` con una entrada en `fieldErrors`. Al crear (`POST`) el backend fuerza `isActive = true` (la modalidad nace Activa) e ignora el valor enviado; el `PATCH` genérico sí puede cambiar el estado. También está implementado el bloqueo de eliminación (E2): si una o más fichas usan la modalidad, `DELETE /api/modalities/{id}` responde `400 error.modalityInUse`; en ese caso la modalidad no se elimina y debe **desactivarse** con `PATCH /api/modalities` (`isActive: false`), de modo que las fichas existentes la sigan conservando. El estado se maneja con `isActive` en lugar de las acciones Desactivar/Reactivar. `GET /api/modalities` usa el estándar de paginación del sistema (`page`/`size`/`sort`, `X-Total-Count`, `Link`); `GET /api/modalities/active` permanece sin paginar porque alimenta selectores. La **escritura** (`POST`, `PUT`, `PATCH`, `DELETE`) está restringida a `ROLE_ADMIN`; un usuario autenticado sin ese rol recibe `403`. La **lectura** (`GET /api/modalities`, `GET /api/modalities/{id}` y `GET /api/modalities/active`) queda disponible para cualquier usuario autenticado porque alimenta selectores de otros flujos.

---

## UC022 — Gestionar tipos de documento

**Módulo:** Configuración y catálogos | **Actor:** Administrador | **Estado:** Implementado

**Feature:** CRUD del catálogo de tipos de documento (CC, TI, CE, …). Las iniciales alimentan el login derivado `<iniciales>_<número>`.

**Endpoints:**

| Método | Ruta                         | Acceso       | Descripción                                                                                        |
| ------ | ---------------------------- | ------------ | -------------------------------------------------------------------------------------------------- |
| GET    | `/api/document-types`        | Público      | Lista **paginada** de tipos (20 por página por defecto).                                           |
| GET    | `/api/document-types/active` | Público      | Lista de tipos activos, sin paginar; usado por el registro y el login.                             |
| GET    | `/api/document-types/{id}`   | Público      | Detalle.                                                                                           |
| POST   | `/api/document-types`        | `ROLE_ADMIN` | Crea; `201` con el recurso.                                                                        |
| PUT    | `/api/document-types`        | `ROLE_ADMIN` | Reemplaza; el `id` viaja en el body; `200`.                                                        |
| PATCH  | `/api/document-types`        | `ROLE_ADMIN` | Actualización parcial; el `id` viaja en el body; `200`.                                            |
| DELETE | `/api/document-types/{id}`   | `ROLE_ADMIN` | Elimina; `204`. Bloquea si algún perfil usa el tipo (`400 error.documentTypeInUse`).               |

**Request — `POST /api/document-types`**

```json
{
  "name": "Cédula de ciudadanía",
  "initials": "CC"
}
```

| Campo      | Tipo    | Obligatorio | Reglas                                                                                                                                                                                                                |
| ---------- | ------- | ----------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `name`     | string  | Sí          | `@NotBlank`, máximo 30. **Nombre único** (se compara sin distinguir mayúsculas); un duplicado responde `400 error.documentTypeNameAlreadyUsed`.                                                                        |
| `initials` | string  | Sí          | `@NotBlank`, máximo 10. Se recorta y se normaliza a mayúsculas. **Iniciales únicas** (se comparan sin distinguir mayúsculas); un duplicado responde `400 error.documentTypeInitialsAlreadyUsed`.                        |
| `isActive` | boolean | No          | Estado del catálogo. Si se omite al crear, el tipo nace **activo** (`true`).                                                                                                                                          |

**Response:** `201 Created` con el `DocumentTypeDTO` (`id`, `name`, `initials`, `isActive`). `GET /api/document-types` es **paginado**: acepta `page` (base 0), `size` y `sort=campo,asc|desc` (20 por defecto) y devuelve un arreglo JSON con la página actual más las cabeceras `X-Total-Count` y `Link`. `GET /api/document-types/active` devuelve el arreglo completo sin paginar (selector del registro y del login). El listado y el detalle son públicos por configuración de seguridad; la escritura está restringida a `ROLE_ADMIN`.

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idnotfound`, `400 error.validation`; `400 error.documentTypeNameAlreadyUsed` (nombre duplicado, E1); `400 error.documentTypeInitialsAlreadyUsed` (iniciales duplicadas, E2); `400 error.documentTypeInitialsInUse` (cambio de iniciales de un tipo en uso, E3); `400 error.documentTypeInUse` (eliminación de un tipo en uso, E4); `403` en escritura sin `ROLE_ADMIN`; `404`.

**Notas / lo que se necesita:** reglas de UC022 implementadas. En `PUT` y `PATCH` el `id` viaja **solo en el body** (la ruta es `/api/document-types`, sin `{id}`): si falta responde `400 error.idnull` y si no existe, `400 error.idnotfound`. Unicidad (E1/E2): el nombre se recorta y se compara sin distinguir mayúsculas; las iniciales se recortan, se convierten a mayúsculas y se comparan sin distinguir mayúsculas; ambos se validan en `POST`, `PUT` y `PATCH` (el tipo con el mismo `id` se excluye del chequeo), y un valor vacío o en blanco responde `400 error.validation`. Iniciales en uso (E3): si al menos un `UserProfile` referencia el tipo, cambiar sus iniciales responde `400 error.documentTypeInitialsInUse`; el nombre sí puede cambiarse en cualquier momento. Eliminar en uso (E4): si algún `UserProfile` referencia el tipo, `DELETE /api/document-types/{id}` responde `400 error.documentTypeInUse`; en ese caso el tipo no se elimina y debe **desactivarse** con `PATCH /api/document-types` (`isActive: false`), de modo que los perfiles existentes lo sigan conservando. El estado se maneja con `isActive` en lugar de acciones Desactivar/Reactivar. `GET /api/document-types` usa el estándar de paginación del sistema (`page`/`size`/`sort`, `X-Total-Count`, `Link`); `GET /api/document-types/active` permanece sin paginar porque alimenta selectores. Las escrituras (`POST`, `PUT`, `PATCH`, `DELETE`) están restringidas a `ROLE_ADMIN`. El estado `isActive` ya existía: los tipos nuevos nacen activos (`true`) y el registro (UC001) rechaza los inactivos con `400 error.documentTypeInactive`.

---

## UC016 — Gestionar tipos de justificación

**Módulo:** Configuración y catálogos | **Actor:** Administrador | **Estado:** Implementado

**Feature:** CRUD del catálogo de motivos de justificación. Cada tipo define el límite de días justificables por trimestre.

**Endpoints:**

| Método | Ruta                            | Acceso                            | Descripción                   |
| ------ | ------------------------------- | --------------------------------- | ----------------------------- |
| GET    | `/api/justification-types`        | Autenticado                       | Lista completa (sin paginar). |
| GET    | `/api/justification-types/active` | Autenticado                       | Lista de tipos activos; la usa el formulario del aprendiz (UC011). |
| GET    | `/api/justification-types/{id}`   | Autenticado                       | Detalle.                      |
| POST   | `/api/justification-types`        | `ROLE_ADMIN` | Crea; `201` con el recurso.   |
| PUT    | `/api/justification-types`        | `ROLE_ADMIN` | Reemplaza; el `id` viaja en el body; `200`. |
| PATCH  | `/api/justification-types`        | `ROLE_ADMIN` | Actualización parcial; el `id` viaja en el body; `200`. |
| DELETE | `/api/justification-types/{id}`   | `ROLE_ADMIN` | Elimina; `204`. Bloquea si algún tipo fue usado (`400 error.justificationTypeInUse`). |

**Request — `POST /api/justification-types`**

```json
{
  "name": "Calamidad doméstica",
  "limitPerTrimester": 3,
  "status": "ACTIVO"
}
```

| Campo               | Tipo    | Obligatorio | Reglas                                                                                           |
| ------------------- | ------- | ----------- | ------------------------------------------------------------------------------------------------ |
| `name`              | string  | Sí          | `@NotBlank`, máximo 100. **Nombre único** (se compara sin distinguir mayúsculas); un duplicado responde `400 error.justificationTypeNameAlreadyUsed`. |
| `limitPerTrimester` | integer | Sí          | `@NotNull` + `@Min(1)`: entero mayor a 0 (E2). Un valor nulo, 0 o negativo responde `400 error.validation` con `limitPerTrimester` en `fieldErrors`. |
| `status`            | string  | No          | Valores del enum `Status`: `ACTIVO`, `INACTIVO`. Si se omite al crear, el tipo nace **Activo**; en `PUT`/`PATCH` conserva el estado existente. |

**Response:** `201 Created` con el `JustificationTypeDTO` (`id`, `name`, `limitPerTrimester`, `status`). `GET /api/justification-types` devuelve todos los tipos (sin paginar); `GET /api/justification-types/active` devuelve solo los `ACTIVO` para el formulario del aprendiz.

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idnotfound`, `400 error.validation` (E2: límite nulo, 0 o negativo); `400 error.justificationTypeNameAlreadyUsed` (nombre duplicado, E1); `400 error.justificationTypeInUse` (el tipo fue usado en justificaciones y no puede eliminarse, E3); `403`; `404`.

**Notas / lo que se necesita:** reglas de UC016 implementadas. El **nombre es único** (se recorta y se compara sin distinguir mayúsculas) y se valida en `POST`, `PUT` y `PATCH` excluyendo el propio `id`; un duplicado responde `400 error.justificationTypeNameAlreadyUsed` (E1) y un nombre vacío o en blanco responde `400 error.validation` con `name` en `fieldErrors`. El **límite** es obligatorio y mayor a 0 (`@Min(1)`); un valor nulo, 0 o negativo responde `400 error.validation` con `limitPerTrimester` en `fieldErrors` (E2). Al crear, el tipo nace **Activo** (`status = ACTIVO`) si se omite el campo —el formulario del UC solo envía nombre y límite—; en `PUT`/`PATCH` un `status` ausente conserva el estado existente, así que Desactivar/Reactivar (A2/A3) se hace con `PATCH` (`status: "INACTIVO"` / `"ACTIVO"`). **Eliminar en uso** (E3): si alguna `Justification` referencia el tipo, `DELETE` responde `400 error.justificationTypeInUse`; en ese caso el tipo no se elimina y se **desactiva**. `GET /api/justification-types/active` devuelve solo los activos para el formulario del aprendiz (UC011). En `PUT` y `PATCH` el `id` viaja **solo en el body** (ruta sin `{id}`); si falta responde `400 error.idnull` y si no existe, `400 error.idnotfound`. El campo de estado se llama **`status`** (antes `state`): entidad, DTO y documento MongoDB usan `status`; la migración Mongock (orden 008) renombra el campo en la colección `justification_type`. Las escrituras (`POST`, `PUT`, `PATCH`, `DELETE`) están restringidas a `ROLE_ADMIN`; la lectura queda para cualquier usuario autenticado.

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
| GET    | `/api/admin/users/search`                       | `ROLE_ADMIN` | Búsqueda por texto, `status` y `role`; paginada con `X-Total-Count` (`UserManagementDTO`). |
| PATCH  | `/api/admin/users/activated`                    | `ROLE_ADMIN` | Activa/desactiva por número de documento; `200` con `AdminUserDTO`.             |
| DELETE | `/api/admin/users/{login}`                      | —            | **No disponible**: los usuarios nunca se eliminan; responde `405`.              |

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

**Errores:** `400 error.idexists`, `400 error.idmissing`, tipo `invalid-password`, `400 error.userexists`, `400 error.emailexists`, `400 error.documentnumberexists`, `400 error.documentTypeNotFound`, `400 error.rolenotfound`, `400 error.adminprotected` (la cuenta `admin` está protegida), `400 error.lastAdmin` ("Debe existir al menos un Administrador activo"), `400 error.lastInstructor` (lista las materias afectadas); `403`; `404`.

**Notas / lo que se necesita:** reglas de UC006 implementadas; **lo único pendiente es el reenvío manual de credenciales (E7), que se resuelve en UC018 (REST de notificaciones)**. Un solo rol por cuenta (`ROLE_USER` + rol de dominio); solo se pueden asignar **Administrador, Instructor o Aprendiz** —`ROLE_COORDINATOR` o un rol inexistente responden `400 error.rolenotfound`. El **cambio de rol** aplica las mismas guardas que la desactivación: no se puede degradar al **último administrador activo** (`error.lastAdmin`), ni a la **cuenta `admin`** protegida (`error.adminprotected`, que además bloquea su desactivación y el cambio de su documento), ni al **último instructor** de materias de fichas operativas (`error.lastInstructor`, considerando fichas en estado `PENDIENTE` o `ACTIVA`; los estados `FINALIZADA`, `APLAZADA` y `CANCELADA` no cuentan como operativos). El **login se recalcula** cuando cambia el número **o el tipo** de documento. **Los usuarios nunca se eliminan**: el endpoint `DELETE /api/admin/users/{login}` se retiró y responde `405`; el estado se cambia con `PATCH /api/admin/users/activated`. `GET /api/admin/users/search` filtra por texto (nombre, documento o correo), `status` y `role`, y pagina con `X-Total-Count`/`Link` (20 por defecto). Las cuentas creadas por un Administrador nacen con `mustChangePassword = true`; el flujo de cambio obligatorio en el primer inicio es del frontend. El correo de credenciales se envía de forma síncrona al crear; si falla, se guarda una `Notificacion` pendiente, pero el **reenvío manual (E7) queda a cargo de UC018** (REST de notificaciones). Existe además el CRUD genérico `/api/user-profiles` sin `@PreAuthorize` (deuda transversal).

---

## UC012 — Gestionar programas de aprendizaje

**Módulo:** Programas y trimestres | **Actor:** Administrador | **Estado:** Implementado

**Feature:** CRUD de programas con nombre, iniciales, código numérico y cantidad de trimestres. Solo los programas activos pueden recibir fichas nuevas.

**Endpoints:**

| Método | Ruta                      | Acceso       | Descripción                                                                              |
| ------ | ------------------------- | ------------ | ---------------------------------------------------------------------------------------- |
| GET    | `/api/programs`           | Autenticado  | Lista paginada.                                                                          |
| GET    | `/api/programs/active`    | Autenticado  | Lista de programas activos, sin paginar; los activos alimentan la creación de fichas (UC007). |
| GET    | `/api/programs/search`    | Autenticado  | Filtra por `search` (código o nombre) y `status`; paginado.                              |
| GET    | `/api/programs/{id}`      | Autenticado  | Detalle.                                                                                 |
| POST   | `/api/programs`           | `ROLE_ADMIN` | Crea; `201` con el recurso.                                                              |
| PUT    | `/api/programs`           | `ROLE_ADMIN` | Reemplaza; el `id` viaja en el body; `200`.                                              |
| PATCH  | `/api/programs`           | `ROLE_ADMIN` | Actualización parcial; `id` en el cuerpo; ignora `status` y valores en blanco.           |
| PATCH  | `/api/programs/activated` | `ROLE_ADMIN` | Activa/desactiva; devuelve el programa y, si aplica, una advertencia por fichas activas. |
| DELETE | `/api/programs/{id}`      | `ROLE_ADMIN` | Elimina; `204`. Bloquea si el programa tiene fichas (`400 error.programInUse`).          |

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
| `code`       | string  | Sí          | `@NotNull`, máximo 30, **solo números**; único. Un valor con caracteres distintos de dígitos responde `400 error.validation` con `code` en `fieldErrors` (E5). |
| `trimesters` | integer | Sí          | `@Min(1)` y `@Max(12)`.                                                 |
| `status`     | boolean | No          | Ignorado en la creación: el programa nace **Activo** (`status = true`).  |

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

**Errores:** `400 error.idexists`, `400 error.idnull`, `400 error.idnotfound`, `400 error.validation` (código no numérico, E5, y rango de trimestres, E6); `400 error.codeexists` ("Ya existe un programa con este código"); `400 error.initialsexists` ("Ya existe un programa con estas iniciales"); `400 error.nameexists`; `400 error.trimestersoutofrange` y `400 error.codenotnumeric` (campos presentes en un `PATCH` fuera de regla); `400 error.programInUse` (el programa tiene fichas y no puede eliminarse, E8); `403`; `404`.

**Notas / lo que se necesita:** reglas de UC012 implementadas. El **código es numérico** (`@Pattern` en `POST`/`PUT`, validación en el servicio para los campos presentes en `PATCH`); la **cantidad de trimestres** se valida 1–12 en `POST`, `PUT` y en los campos presentes de `PATCH`. Al crear, el programa nace **siempre Activo** (`status = true`) y el valor enviado se ignora: el estado se cambia con `PATCH /api/programs/activated`, que implementa la advertencia E7 con `activeFichasCount`. **Eliminar en uso (E8):** si alguna ficha referencia el programa, `DELETE` responde `400 error.programInUse`; en ese caso no se elimina y se **desactiva**. `GET /api/programs/active` devuelve solo los activos para la creación de fichas (UC007). En `PUT` el `id` viaja **solo en el body** (ruta sin `{id}`); si falta, `400 error.idnull`; si no existe, `400 error.idnotfound`. La escritura (`POST`, `PUT`, `PATCH`, `PATCH /activated`, `DELETE`) está restringida a `ROLE_ADMIN`; la lectura queda para cualquier usuario autenticado. Al ser un reemplazo completo, `PUT` también puede cambiar `status` (además de `PATCH /activated`).

---

## UC014 — Gestionar trimestres académicos

**Módulo:** Programas y trimestres | **Actor:** Administrador | **Estado:** Implementado

**Feature:** CRUD de trimestres globales. Su estado (`status`, enum `FUTURO`/`ACTIVO`/`CERRADO`) se calcula por fechas y un job diario lo sincroniza; el trimestre activo enmarca asistencias, horarios y justificaciones.

**Endpoints:**

| Método | Ruta                     | Acceso       | Descripción                                                                         |
| ------ | ------------------------ | ------------ | ----------------------------------------------------------------------------------- |
| GET    | `/api/trimesters`        | Autenticado  | Lista paginada.                                                                      |
| GET    | `/api/trimesters/search` | Autenticado  | Filtra por nombre, año (4 dígitos) o `status` (`FUTURO\|ACTIVO\|CERRADO`); paginado. |
| GET    | `/api/trimesters/{id}`   | `ROLE_ADMIN` | Detalle.                                                                             |
| POST   | `/api/trimesters`        | `ROLE_ADMIN` | Crea y calcula `status`; `201`.                                                      |
| PUT    | `/api/trimesters`        | `ROLE_ADMIN` | Reemplaza; `200`. Aplica las mismas reglas de estado que `PATCH`; `id` en el cuerpo. |
| PATCH  | `/api/trimesters`        | `ROLE_ADMIN` | Actualización parcial con reglas por estado; `id` en el cuerpo.                      |
| DELETE | `/api/trimesters/{id}`   | `ROLE_ADMIN` | Elimina; `204`.                                                                      |

**Request — `POST /api/trimesters`**

```json
{
  "name": "2026-2",
  "startDate": "2026-10-01",
  "endDate": "2026-12-20"
}
```

| Campo       | Tipo                  | Obligatorio | Reglas                                                                                                                     |
| ----------- | --------------------- | ----------- | -------------------------------------------------------------------------------------------------------------------------- |
| `name`      | string                | Sí          | `@NotNull`, máximo 30.                                                                                                     |
| `startDate` | string (`YYYY-MM-DD`) | Sí          | `@NotNull`; debe ser anterior a `endDate`; en la creación, desde mañana (`startDate > hoy`).                                |
| `endDate`   | string (`YYYY-MM-DD`) | Sí          | `@NotNull`; en la creación, no anterior a hoy (`endDate >= hoy`).                                                          |
| `status`    | string                | No          | Enum `FUTURO`/`ACTIVO`/`CERRADO`; lo calcula el servidor por fechas y el valor enviado se ignora en `POST`, `PUT` y `PATCH`. |

**Response:** `201 Created`

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9e40",
  "name": "2026-2",
  "startDate": "2026-10-01",
  "endDate": "2026-12-20",
  "status": "FUTURO"
}
```

**Errores:** `400 error.datesorder` ("La fecha de inicio debe ser anterior a la fecha de fin"), `400 error.enddateinpast` ("La fecha de fin no puede ser anterior a la fecha actual"), `400 error.startdatemustbefuture` ("La nueva fecha de inicio debe ser posterior a la fecha actual"), `400 error.datesoverlap` ("Las fechas se solapan con otro trimestre"), `400 error.noteditable` ("No se puede modificar un trimestre cerrado"), `400 error.startdatelocked` ("No se puede modificar la fecha de inicio de un trimestre activo"), `400 error.attendancestartdate` ("No se puede modificar la fecha de inicio de un trimestre que ya tiene asistencia registrada"); además de `idexists`, `idnull`, `idnotfound` y de validación; `403`; `404`.

**Notas / lo que se necesita:** `PUT` y `PATCH` aplican las mismas reglas de estado (un trimestre **cerrado** no se edita; en **activo** la fecha inicio está congelada y la fecha fin no puede ser anterior a hoy; en **futuro** la fecha inicio debe seguir siendo futura; cambiar la fecha inicio se bloquea si hay asistencias), validan orden de fechas (E2) y solape (E1) y recalculan `status`. **Eliminar en uso (E6):** si el trimestre tiene horarios o asistencias, `DELETE` responde `400 error.trimesterInUse`. La escritura está restringida a `ROLE_ADMIN`; `GET /api/trimesters/{id}` también exige `ROLE_ADMIN`.

El `status` es un **enum persistido** (`StateTrimester`: `FUTURO`, `ACTIVO`, `CERRADO`) que el servidor calcula siempre por fechas e **ignora el valor enviado** en `POST`, `PUT` y `PATCH` (`CERRADO` si `endDate < hoy`; `FUTURO` si `startDate > hoy`; si no, `ACTIVO`). Al crear, las fechas se validan en este orden: `startDate < endDate` (`error.datesorder`), `endDate` no anterior a hoy (`error.enddateinpast`), `startDate` desde mañana (`error.startdatemustbefuture`) y sin solape (`error.datesoverlap`); por eso todo trimestre creado por `POST` nace **FUTURO**. Un **job diario** (01:00) mantiene el `status` sincronizado con las fechas, y la **migración Mongock orden 009** (`MigrateTrimesterStatusToState`) convierte el booleano previo al enum por fechas. **Desviación conocida (E1):** la spec pide que el mensaje de solape nombre el trimestre en conflicto, pero el contrato de errores solo entrega la clave `error.datesoverlap` (más `params: trimester`); el nombre no viaja al cliente y el frontend debe mostrar un texto genérico.

---

## UC007 — Gestionar fichas

**Módulo:** Fichas y materias | **Actor:** Administrador | **Estado:** Implementado

**Feature:** CRUD de fichas (en el código, `Grade`). Una ficha agrupa aprendices de un programa y define jornada, modalidad y rango de fechas. Su `state` es un enum persistido (`StateGrade`: `PENDIENTE`, `ACTIVA`, `FINALIZADA`, `APLAZADA`, `CANCELADA`) que el servidor calcula por fechas; `APLAZADA` y `CANCELADA` son decisiones manuales del Administrador.

**Endpoints:**

| Método | Ruta                    | Acceso                     | Descripción                                                                       |
| ------ | ----------------------- | -------------------------- | --------------------------------------------------------------------------------- |
| GET    | `/api/grades`           | `ROLE_ADMIN`               | Lista paginada de fichas (relaciones cargadas con `eagerload=true` por defecto).  |
| GET    | `/api/grades/active`    | `ROLE_ADMIN`               | Lista de fichas con estado `ACTIVA` (sin paginar).                                 |
| GET    | `/api/grades/{id}`      | `ROLE_ADMIN`               | Detalle con relaciones.                                                           |
| POST   | `/api/grades`           | `ROLE_ADMIN`               | Crea y calcula `state`; `201` con el recurso.                                      |
| PUT    | `/api/grades`           | `ROLE_ADMIN`               | Reemplaza; `200`. El `id` viaja **solo en el body**.                               |
| PATCH  | `/api/grades`           | `ROLE_ADMIN`               | Actualización parcial; `200`. El `id` viaja **solo en el body**.                   |
| PATCH  | `/api/grades/postponed` | `ROLE_ADMIN`               | Aplaza una ficha `PENDIENTE` o `ACTIVA` → `APLAZADA`; body con `id`.               |
| PATCH  | `/api/grades/resumed`   | `ROLE_ADMIN`               | Reanuda una ficha `APLAZADA` y recalcula `state` por fechas; body con `id`.        |
| PATCH  | `/api/grades/cancelled` | `ROLE_ADMIN`               | Cancela cualquier ficha que no esté `CANCELADA` → `CANCELADA`; body con `id`.     |
| DELETE | `/api/grades/{id}`      | `ROLE_ADMIN`               | Elimina; `204`.                                                                    |

**Request — `POST /api/grades`**

```json
{
  "code": "3412345",
  "startDate": "2026-10-01",
  "endDate": "2027-03-31",
  "program": { "id": "665f1c2a9e13b7a1f2c8d9e30" },
  "modality": { "id": "665f1c2a9e13b7a1f2c8d9e50" },
  "timeSlot": { "id": "665f1c2a9e13b7a1f2c8d9e60" }
}
```

| Campo       | Tipo                  | Obligatorio | Reglas                                                                                                                                                                                                                                                    |
| ----------- | --------------------- | ----------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `code`      | string                | Sí          | `@NotNull`, máximo 20 y **solo números**; único entre fichas. Un código no numérico responde `400 error.validation` con `code` en `fieldErrors` en `POST`/`PUT` (`@Pattern`) y `400 error.codenotnumeric` en `PATCH`; un duplicado responde `400 error.gradeCodeAlreadyUsed` (E1). |
| `state`     | string                | No          | Enum `StateGrade`: `PENDIENTE`, `ACTIVA`, `FINALIZADA`, `APLAZADA`, `CANCELADA`. Lo calcula el servidor por fechas y el valor enviado se **ignora** en `POST`, `PUT` y `PATCH`.                                                                            |
| `startDate` | string (`YYYY-MM-DD`) | Sí          | `@NotNull`; no posterior a `endDate` (`error.datesorder`). Al crear, y en `PUT`/`PATCH` cuando la fecha cambia, no anterior a hoy (`error.startdateinpast`).                                                                                                 |
| `endDate`   | string (`YYYY-MM-DD`) | Sí          | `@NotNull`; no anterior a `startDate` (`error.datesorder`).                                                                                                                                                                                                 |
| `program`   | objeto                | Sí          | `@NotNull`; referencia por `id`. Si la referencia existe y está inactiva: `400 error.programInactive`.                                                                                                                                                      |
| `modality`  | objeto                | Sí          | `@NotNull`; referencia por `id`. Si la referencia existe y está inactiva: `400 error.modalityInactive`.                                                                                                                                                     |
| `timeSlot`  | objeto                | Sí          | `@NotNull`; referencia por `id`. Si la referencia existe y está inactiva: `400 error.timeSlotInactive`.                                                                                                                                                     |

**Response:** `201 Created`

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9e70",
  "code": "3412345",
  "state": "PENDIENTE",
  "startDate": "2026-10-01",
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

**Errores:** `400 error.gradeCodeAlreadyUsed` ("El código de ficha ya está en uso"), `400 error.codenotnumeric` ("El código debe contener solo números", solo en `PATCH`), `400 error.datesorder` ("La fecha de fin no puede ser anterior a la fecha de inicio"), `400 error.startdateinpast` ("La fecha de inicio no puede ser anterior a hoy"), `400 error.programInactive` ("No se pueden crear fichas para un programa inactivo"), `400 error.modalityInactive` ("No se pueden crear fichas para una modalidad inactiva"), `400 error.timeSlotInactive` ("No se pueden crear fichas para una jornada inactiva"), `400 error.noteditable` ("No se puede modificar una ficha finalizada"), `400 error.fieldlocked` ("El campo no se puede modificar en el estado actual de la ficha"), `400 error.gradeCodeLocked` ("El código solo puede cambiarse mientras la ficha no tenga materias ni aprendices"), `400 error.invalidtransition` (la acción no aplica al estado actual de la ficha), `400 error.gradeInUse` ("No es posible eliminar la ficha: tiene aprendices vinculados y/o registros de asistencia. Si desea retirarla de operación, use Cancelar ficha"); además de `error.idexists`, `error.idnull`, `error.idnotfound` y de validación; `403`; `404`.

**Notas / lo que se necesita:** reglas de UC007 implementadas. Las reglas de edición por estado son las de la tabla "Reglas por estado" de UC007 en [`docs/use-cases.md`](./use-cases.md): `FINALIZADA` no admite ningún cambio (`error.noteditable`); `ACTIVA` solo permite `endDate`, `program` y `code`; `APLAZADA` solo `endDate`; `PENDIENTE` y `CANCELADA` admiten todos los campos. Un cambio de `code` sobre una ficha con materias o aprendices responde `400 error.gradeCodeLocked`. Las acciones `PATCH /api/grades/postponed`, `/resumed` y `/cancelled` reciben el `id` en el body y una transición inválida responde `400 error.invalidtransition`. **Eliminar en uso:** si la ficha tiene aprendices o asistencias, `DELETE` responde `400 error.gradeInUse`; si no, elimina la ficha y **borra en cascada** sus materias, y de cada materia sus horarios y excepciones. En `PUT` y `PATCH` el `id` viaja **solo en el body** (ruta sin `{id}`, ya no existe `error.idinvalid`): si falta, `400 error.idnull`; si no existe, `400 error.idnotfound`. Las **escrituras** (`POST`, `PUT`, `PATCH`, las tres acciones y `DELETE`) y las **lecturas** (`GET /api/grades`, `GET /api/grades/{id}` y `GET /api/grades/active`) quedan restringidas a `ROLE_ADMIN`; el instructor consulta sus propias fichas con `GET /api/class-sections/mine` (UC017).

El `state` es un **enum persistido** (`StateGrade`) con cinco valores: `PENDIENTE` (fecha de inicio futura), `ACTIVA` (hoy dentro del rango) y `FINALIZADA` (fecha de fin pasada) los calcula el servidor por fechas, mientras que `APLAZADA` y `CANCELADA` son manuales y el cálculo por fechas **nunca** las pisa. Un **job diario** (01:00) mantiene el estado sincronizado, y la **migración Mongock orden 010** (`MigrateGradeInactivaToAplazada`) convierte los documentos con el estado eliminado `INACTIVA` a `APLAZADA` (rollback no-op por pérdida de información). El `state` enviado por el cliente se ignora en `POST`, `PUT` y `PATCH`, y ya no es obligatorio en el request.

---

## UC015 — Gestionar materias

**Módulo:** Fichas y materias | **Actor:** Administrador | **Estado:** Implementado

**Feature:** CRUD de materias (`ClassSection`) por ficha, con instructor asignable y horarios por trimestre. Las materias viven dentro de una única ficha.

**Endpoints:**

| Método | Ruta                         | Acceso                           | Descripción                                                                                                              |
| ------ | ---------------------------- | -------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| GET    | `/api/class-sections`        | `ROLE_ADMIN`                     | Lista paginada de materias (paginación con `X-Total-Count` y `Link`).                                                     |
| GET    | `/api/class-sections/{id}`   | `ROLE_ADMIN`                     | Detalle.                                                                                                                  |
| GET    | `/api/class-sections/mine`   | `ROLE_INSTRUCTOR` o `ROLE_ADMIN` | Materias del instructor autenticado (ver UC017).                                                                          |
| POST   | `/api/class-sections`        | `ROLE_ADMIN`                     | Crea; `201`.                                                                                                              |
| PUT    | `/api/class-sections`        | `ROLE_ADMIN`                     | Reemplaza; `200`. El `id` viaja **solo en el body**.                                                                      |
| PATCH  | `/api/class-sections`        | `ROLE_ADMIN`                     | Actualización parcial, incluida la desactivación/reactivación (A4) con `isActive`; `200`. El `id` viaja **solo en el body**. |
| DELETE | `/api/class-sections/{id}`   | `ROLE_ADMIN`                     | Elimina; `204`. Bloqueado si la materia tiene asistencias (A3).                                                           |
| GET    | `/api/class-schedules`       | Autenticado                      | Lista paginada de horarios.                                                                                               |
| GET    | `/api/class-schedules/{id}`  | Autenticado                      | Detalle de un horario.                                                                                                    |
| POST   | `/api/class-schedules`       | `ROLE_ADMIN`                     | Crea; `201`.                                                                                                              |
| PUT    | `/api/class-schedules`       | `ROLE_ADMIN`                     | Reemplaza; `200`. El `id` viaja **solo en el body**.                                                                      |
| PATCH  | `/api/class-schedules`       | `ROLE_ADMIN`                     | Actualización parcial; `200`. El `id` viaja **solo en el body**.                                                          |
| DELETE | `/api/class-schedules/{id}`  | `ROLE_ADMIN`                     | Elimina; `204`.                                                                                                           |
| GET    | `/api/class-exceptions`      | Autenticado                      | Lista paginada de excepciones no lectivas.                                                                                |
| GET    | `/api/class-exceptions/{id}` | Autenticado                      | Detalle de una excepción.                                                                                                 |
| POST   | `/api/class-exceptions`      | `ROLE_ADMIN`                     | Crea; `201`.                                                                                                              |
| PUT    | `/api/class-exceptions`      | `ROLE_ADMIN`                     | Reemplaza; `200`. El `id` viaja **solo en el body**.                                                                      |
| PATCH  | `/api/class-exceptions`      | `ROLE_ADMIN`                     | Actualización parcial; `200`. El `id` viaja **solo en el body**.                                                          |
| DELETE | `/api/class-exceptions/{id}` | `ROLE_ADMIN`                     | Elimina; `204`.                                                                                                           |

**Request — `POST /api/class-sections`**

```json
{
  "subjectName": "Programación orientada a objetos",
  "isActive": true,
  "instructor": { "id": "665f1c2a9e13b7a1f2c8d9e80" },
  "grade": { "id": "665f1c2a9e13b7a1f2c8d9e70" }
}
```

| Campo         | Tipo    | Obligatorio | Reglas                                                                                                                                                                               |
| ------------- | ------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `subjectName` | string  | Sí          | `@NotNull`, máximo 200; se recorta y debe ser **único dentro de la ficha**, comparando sin distinguir mayúsculas (E2). Un duplicado responde `400 error.classSectionNameAlreadyUsed`. |
| `isActive`    | boolean | Sí          | `@NotNull`; lo define el cliente. La desactivación y reactivación (A4) se hacen con `PATCH` sobre este campo, incluida la reactivación de una materia.                              |
| `instructor`  | objeto  | No          | Opcional: la materia puede crearse sin instructor. Si se envía, la referencia debe existir y su cuenta estar **activa**; si no, `400 error.instructorInactive` (E7).                   |
| `grade`       | objeto  | Sí          | `@NotNull`; ficha a la que pertenece. Solo se crean o modifican materias en fichas `PENDIENTE` o `ACTIVA` (E1).                                                                      |

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

| Campo                   | Tipo                | Obligatorio | Reglas                                                                                                                                                                                                                                                                                                                                     |
| ----------------------- | ------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `dayOfWeek`             | string              | Sí          | `@NotNull`; enum `DayOfWeek`: `LUNES` … `DOMINGO`.                                                                                                                                                                                                                                                                                          |
| `startTime` / `endTime` | string (`HH:mm:ss`) | Sí          | `@NotNull`; `startTime` debe ser anterior a `endTime` (E5: `400 error.scheduleCrossesMidnight`). El rango debe caer dentro de la jornada de la ficha (E3: `400 error.scheduleOutOfTimeSlot`) y no solaparse con otro horario de la misma ficha en el mismo trimestre y día —de esta materia o de otra— (E4: `400 error.scheduleOverlap`); los rangos adyacentes no solapan. |
| `trimester`             | objeto              | Sí          | `@NotNull`; referencia por `id`. No se crean, modifican ni eliminan horarios de un trimestre `CERRADO` (E6: `400 error.trimesterClosed`).                                                                                                                                                                                                  |
| `classSection`          | objeto              | Sí          | `@NotNull`; referencia por `id`.                                                                                                                                                                                                                                                                                                           |

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

**Response:** `201 Created` con el DTO creado (`ClassSectionDTO` con `id`, `subjectName`, `isActive`, `instructor`, `grade`; `ClassScheduleDTO` con `id`, `dayOfWeek`, `startTime`, `endTime`, `trimester`, `classSection`; `ClassExceptionDTO` con `id`, `date`, `reason`, `classSection`). Las listas paginan con `X-Total-Count` y `Link`; los CRUD devuelven los errores `idexists`, `idnull`, `idnotfound` y `error.validation` (ya no existe `error.idinvalid`).

**Errores:** `400 error.gradeNotOperable` ("No se pueden crear ni modificar materias en una ficha en su estado actual", E1); `400 error.classSectionNameAlreadyUsed` ("Ya existe una materia con este nombre en esta ficha", E2); `400 error.scheduleOutOfTimeSlot` ("El horario debe estar dentro de la jornada de la ficha", E3); `400 error.scheduleOverlap` ("El horario se solapa con otro horario de la ficha en ese trimestre", E4); `400 error.scheduleCrossesMidnight` ("La sesión debe iniciar y terminar el mismo día", E5); `400 error.trimesterClosed` ("No se pueden modificar los horarios: el trimestre ya fue cerrado", E6); `400 error.instructorInactive` ("El instructor seleccionado ya no está disponible, selecciona otro", E7); `400 error.classSectionInUse` ("No es posible eliminar la materia: tiene registros de asistencia. Puede desactivarla para retirarla de operación", A3); además de `error.idexists`, `error.idnull`, `error.idnotfound` y de validación; `403`; `404`.

**Notas / lo que se necesita:** reglas de UC015 implementadas. **Crear y modificar (E1):** solo en fichas `PENDIENTE` o `ACTIVA` (incluye la reactivación A4); una ficha `APLAZADA`, `CANCELADA` o `FINALIZADA` responde `400 error.gradeNotOperable`. **Nombre único (E2):** se recorta y se compara sin distinguir mayúsculas dentro de la ficha; en `PUT`/`PATCH` la materia no colisiona consigo misma. **Instructor (E7):** es opcional; si se envía, debe existir y su cuenta estar activa. **Horarios:** `dayOfWeek` es obligatorio; cada sesión debe iniciar y terminar el mismo día (E5), caer dentro de la jornada de la ficha (E3) y no solaparse con otro horario de la misma ficha en el mismo trimestre y día —de esta materia o de otra—, y los rangos adyacentes no cuentan como solapamiento (E4). **Trimestre cerrado (E6):** no se crean, modifican ni eliminan horarios de un trimestre `CERRADO`; la clasificación se hace **por fechas** con `TrimesterService.classify`, no por el `status` persistido, para que no exista una ventana de gracia tras el cierre. **Eliminar (A3):** si la materia tiene asistencias, `DELETE` responde `400 error.classSectionInUse` y debe **desactivarse** con `PATCH /api/class-sections` (`isActive: false`); si no, se elimina y borra en cascada sus horarios y excepciones. En `PUT` y `PATCH` de `class-sections`, `class-schedules` y `class-exceptions` el `id` viaja **solo en el body** (ruta sin `{id}`); si falta, `400 error.idnull`; si no existe, `400 error.idnotfound`. Las **escrituras** de los tres resources (`POST`, `PUT`, `PATCH`, `DELETE`) quedan restringidas a `ROLE_ADMIN`; `GET /api/class-sections/mine` acepta solo `ROLE_INSTRUCTOR` o `ROLE_ADMIN` (ya no Coordinador); los `GET` genéricos de materias (`/api/class-sections` y `/api/class-sections/{id}`) también quedan solo para `ROLE_ADMIN`, y el instructor consulta las suyas por `/mine`. Los `GET` de `class-schedules` y `class-exceptions` siguen disponibles para cualquier usuario autenticado (deuda transversal en [`docs/backend-debt.md`](./backend-debt.md)). UC015 no cambió el modelo de datos: no agrega migraciones Mongock (el próximo orden libre es 011). **Desviación conocida:** las excepciones no lectivas siguen bajo roles administrativos (`ROLE_ADMIN`), no las marca el instructor en su flujo de asistencia como describe UC009.

---

## UC008 — Gestionar aprendices

**Módulo:** Aprendices e instructor | **Actor:** Administrador | **Estado:** Implementado

**Feature:** Vinculación por número de documento y desvinculación con motivo de aprendices a fichas mediante el registro `Apprentice`, con estado académico. El servidor resuelve el perfil por documento y fija el estado académico en cada operación.

**Endpoints:**

| Método | Ruta                        | Acceso                           | Descripción                                                                                                |
| ------ | --------------------------- | -------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| POST   | `/api/apprentices`          | `ROLE_ADMIN`                     | Vincula al aprendiz identificado por documento; `201` con el vínculo creado.                              |
| GET    | `/api/apprentices`          | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Lista **paginada** de vínculos; filtros opcionales `gradeId`, `documentNumber`, `name` y `stateAcademic`. |
| GET    | `/api/apprentices/{id}`     | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Detalle de un vínculo; `404` si no existe.                                                                 |
| PATCH  | `/api/apprentices/unlinked` | `ROLE_ADMIN`                     | Desvincula con motivo; `204` si elimina el registro y `200` con el DTO si lo conserva.                    |

No existen `PUT`, `PATCH` ni `DELETE /api/apprentices/{id}`: el UC solo contempla vincular, desvincular y consultar.

**Request — `POST /api/apprentices`**

```json
{
  "documentNumber": "1029384756",
  "grade": { "id": "665f1c2a9e13b7a1f2c8d9e70" }
}
```

| Campo            | Tipo   | Obligatorio | Reglas                                                                           |
| ---------------- | ------ | ----------- | -------------------------------------------------------------------------------- |
| `documentNumber` | string | Sí          | `@NotBlank`, 1–30, `@Pattern(\d+)`: solo dígitos, con el mismo formato de UC001. |
| `grade`          | objeto | Sí          | `@NotNull`; referencia a la ficha por `id`.                                      |

El estado académico **no viaja en el request**: el servidor fija `MATRICULADO` al crear el vínculo e ignora el `stateAcademic` que envíe el cliente.

**Request — `PATCH /api/apprentices/unlinked`**

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9eb0",
  "reason": "RETIRO_VOLUNTARIO"
}
```

| Campo    | Tipo   | Obligatorio | Reglas                                                                                                                   |
| -------- | ------ | ----------- | ------------------------------------------------------------------------------------------------------------------------ |
| `id`     | string | Sí          | `@NotNull`; id del vínculo (`Apprentice`), no del aprendiz.                                                              |
| `reason` | string | Sí          | `@NotNull`; corresponde al enum `StateAcademic` y solo `RETIRO_VOLUNTARIO`, `APLAZADO` o `CANCELADO` son motivos de retiro. Un valor válido del enum que no sea de retiro (`MATRICULADO`, `INACTIVO`) responde `400 error.invalidunlinkreason`; un string que no pertenece al enum no llega al servicio (falla la deserialización del body). |

**Response:** `201 Created` con el `ApprenticeDTO` creado. El DTO devuelve `id`, `stateAcademic`, `student` (resumen con `id`, `documentNumber`, `firstName` y `firstLastName`) y `grade` (resumen con `id` y `code`).

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9eb0",
  "stateAcademic": "MATRICULADO",
  "student": {
    "id": "665f1c2a9e13b7a1f2c8d9ea0",
    "documentNumber": "1029384756",
    "firstName": "Ana",
    "firstLastName": "Gómez"
  },
  "grade": {
    "id": "665f1c2a9e13b7a1f2c8d9e70",
    "code": "3412345"
  }
}
```

`PATCH /api/apprentices/unlinked` responde `204 No Content` sin cuerpo cuando el aprendiz no tiene asistencias en la ficha y el registro se elimina; si tiene asistencias, responde `200 OK` con el `ApprenticeDTO` y su `stateAcademic` pasa a ser el motivo. `GET /api/apprentices` es **paginado** (`page`/`size`/`sort`, 20 por defecto) y devuelve un arreglo JSON con las cabeceras `X-Total-Count` y `Link`; sus filtros son **opcionales** y se combinan con AND: `gradeId` (ficha), `documentNumber` (fragmento del número), `name` (fragmento del primer nombre o del primer apellido) y `stateAcademic` (valor del enum). Se devuelve una página vacía cuando ningún perfil coincide con los filtros de texto o cuando `gradeId` no es un id válido.

**Errores:** `400 error.apprenticeInactive` (el aprendiz no existe, su cuenta no está activa o su documento no identifica a un único perfil, E1); `400 error.apprenticeAlreadyEnrolled` (ya existe un registro de ese aprendiz en esa ficha en cualquier estado, E2); `400 error.gradeNotOperable` (la ficha no está `PENDIENTE` ni `ACTIVA`, E3); `400 error.invalidunlinkreason` (el motivo no es `RETIRO_VOLUNTARIO`, `APLAZADO` ni `CANCELADO`); `400 error.idnotfound` (ficha o vínculo inexistente); `400 error.validation` con `fieldErrors` (E4: documento con formato inválido, `id` o `reason` ausentes); `403`; `404`.

**Notas / lo que se necesita:** reglas de UC008 implementadas. **Vincular:** el request identifica al aprendiz por **número de documento** y el servidor resuelve el `UserProfile`; el vínculo nace con `stateAcademic = MATRICULADO` **fijado por el servidor** (el valor que envíe el cliente se ignora). El documento se valida con el mismo formato de UC001 (solo dígitos, 1–30). **E1:** el número de documento no es único por sí solo —la clave única es el par tipo + número—, así que un número compartido por más de un perfil no identifica a un aprendiz y se rechaza con `error.apprenticeInactive`, igual que una cuenta inexistente o desactivada. **E2:** `error.apprenticeAlreadyEnrolled` bloquea el reingreso a la **misma** ficha aunque el registro previo esté desvinculado. **E3:** vincular y desvincular solo operan sobre fichas `PENDIENTE` o `ACTIVA`. **A1 — Desvincular:** el id del vínculo viaja en el body de `PATCH /api/apprentices/unlinked`; sin asistencias en la ficha el registro se **elimina** (`204`) y con asistencias se **conserva** con el `stateAcademic` igual al motivo (`200`), de modo que el historial de asistencia nunca se pierde. **A2 — Consultar:** `GET /api/apprentices` acepta los cuatro filtros opcionales más la paginación estándar y devuelve del aprendiz el documento y el nombre. **Roles:** `POST` y `PATCH /unlinked` son solo `ROLE_ADMIN`; `GET` de lista y detalle quedan para `ROLE_ADMIN` o `ROLE_INSTRUCTOR`. Se retiraron los endpoints genéricos `PUT`, `PATCH` y `DELETE /api/apprentices/{id}`. UC008 no cambió el modelo de datos: no agrega migraciones Mongock (el próximo orden libre es 011). Verificación: `ApprenticeResourceIT` 34/34.

---

## UC017 — Consultar mis fichas y materias

**Módulo:** Aprendices e instructor | **Actor:** Instructor | **Estado:** Implementado

**Feature:** Consulta de solo lectura de las materias asignadas al instructor y, a través de ellas, de las fichas en las que participa. No hay endpoint dedicado de "mis fichas": la vinculación del instructor nace de sus materias, así que las fichas se **derivan** de `GET /api/class-sections/mine` (`docs/use-cases.md:910`).

**Endpoints:**

| Método | Ruta                       | Acceso                                               | Descripción                                                |
| ------ | -------------------------- | ---------------------------------------------------- | ---------------------------------------------------------- |
| GET    | `/api/class-sections/mine` | `ROLE_INSTRUCTOR` o `ROLE_ADMIN`                     | Materias del instructor autenticado, con la ficha anidada. Acepta `?gradeCode=` opcional. |
| GET    | `/api/class-sections/{id}` | `ROLE_ADMIN`                                         | Detalle de una materia. Endpoint **genérico** reservado a `ROLE_ADMIN`; el instructor consulta sus materias por `/mine`. |
| GET    | `/api/grades`              | `ROLE_ADMIN`                                         | Listado paginado de fichas. Endpoint **genérico** reservado a `ROLE_ADMIN`; el instructor deriva sus fichas de `/mine`. |

**Response — `GET /api/class-sections/mine`:** `200 OK`

```json
[
  {
    "id": "665f1c2a9e13b7a1f2c8d9e90",
    "subjectName": "Programación orientada a objetos",
    "isActive": true,
    "instructor": {
      "id": "665f1c2a9e13b7a1f2c8d9e80",
      "documentNumber": "1029384756"
    },
    "grade": {
      "id": "665f1c2a9e13b7a1f2c8d9e70",
      "code": "3412345",
      "state": "ACTIVA",
      "startDate": "2026-09-01",
      "endDate": "2027-03-31",
      "program": {
        "id": "665f1c2a9e13b7a1f2c8d9e30",
        "name": "Análisis y Desarrollo de Software"
      }
    }
  }
]
```

**Errores:** `401` sin sesión; `403` para roles sin permiso (solo `ROLE_INSTRUCTOR` o `ROLE_ADMIN`); un `gradeCode` sin coincidencias o un instructor sin perfil o sin asignaciones recibe `200` con arreglo vacío. No hay claves de error nuevas: los textos de E1 y E3 los aporta el frontend.

**Notas / lo que se necesita:** implementado y verificado (`ClassSectionResourceIT` 65/65). Las fichas se **derivan** de las materias porque la pertenencia del instructor a una ficha nace de sus asignaciones (`docs/use-cases.md:910`); el listado resuelve el perfil autenticado por `UserProfile` y sus materias por instructor, y no valida el estado de la cuenta. La **búsqueda** del flujo alternativo es el parámetro opcional `gradeCode` de `/mine`: coincidencia **parcial** y **sin distinguir mayúsculas** contra el `code` de la ficha, aplicada **solo entre las materias del instructor** (`docs/use-cases.md:916`); sin coincidencias responde `200 []`. El payload es **aditivo**: la ficha anidada expone `state`, `startDate`, `endDate` y `program { id, name }` además de `id` y `code`, y la materia mantiene `subjectName` e `isActive`. El `instructor` anidado sigue exponiendo solo `{ id, documentNumber }`: para "mis materias" el instructor es el propio usuario autenticado, así que no aporta enriquecerlo. **Decisiones:** no se pagina (la spec no lo pide y es una lista personal); no se filtran las materias inactivas ni las fichas no operativas porque E2 pide mostrarlas con su estado (`docs/use-cases.md:921`); y un instructor sin materias asignadas recibe `200 []`, con el mensaje de E3 a cargo del frontend. Los `GET` genéricos de fichas y materias (`/api/grades`, `/api/grades/{id}`, `/api/grades/active`, `/api/class-sections` y `/api/class-sections/{id}`) quedan reservados a `ROLE_ADMIN`, así que el instructor no lista fichas ni materias ajenas por esa vía. Sin migración Mongock (próximo orden libre: 011).

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
