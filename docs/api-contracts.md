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
- **Vigencia del token:** 86 400 segundos (24 horas) por defecto en los perfiles `dev` y `prod`. El flag `rememberMe` del login —opcional y `false` por defecto— extiende la vigencia a 30 días (`token-validity-in-seconds-for-remember-me: 2592000`); es la opción "mantener sesión" documentada en UC002.
- **Roles:** el sistema emite `ROLE_ADMIN`, `ROLE_INSTRUCTOR`, `ROLE_APPRENTICE` y `ROLE_USER` (los tres primeros siempre acompañados de `ROLE_USER`).
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
| [UC006](#uc006--gestionar-perfiles)                 | Gestionar perfiles                 | Implementado    |
| [UC012](#uc012--gestionar-programas-de-aprendizaje) | Gestionar programas de aprendizaje | Implementado    |
| [UC014](#uc014--gestionar-trimestres-académicos)    | Gestionar trimestres académicos    | Implementado    |
| [UC007](#uc007--gestionar-fichas)                   | Gestionar fichas                   | Implementado    |
| [UC015](#uc015--gestionar-materias)                 | Gestionar materias                 | Implementado    |
| [UC008](#uc008--gestionar-aprendices)               | Gestionar aprendices               | Implementado    |
| [UC017](#uc017--consultar-mis-fichas-y-materias)    | Consultar mis fichas y materias    | Implementado    |
| [UC009](#uc009--gestionar-listas-de-asistencia)     | Gestionar listas de asistencia     | Implementado    |
| [UC011](#uc011--gestionar-asistencia-aprendiz)      | Gestionar asistencia (Aprendiz)    | Implementado    |
| [UC010](#uc010--gestionar-justificaciones)          | Gestionar justificaciones          | Implementado    |
| [UC013](#uc013--gestionar-alertas-de-inasistencia)  | Gestionar alertas de inasistencia  | Implementado    |
| [UC018](#uc018--gestionar-notificaciones)           | Gestionar notificaciones           | Implementado    |
| [UC023](#uc023--consultar-dashboard)                | Consultar dashboard                | Implementado    |

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

**Feature:** Autenticación por tipo y número de documento + contraseña. Emite un JWT de 24 horas, o de 30 días con la opción "mantener sesión" (`rememberMe`), y valida el token en cada petición.

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
| `rememberMe`     | boolean | No          | Opcional, `false` por defecto. Si es `true`, la vigencia del token pasa a 30 días ("mantener sesión", UC002). |

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

**Endpoints:** no hay endpoint de servidor. El token sigue siendo válido hasta su expiración (24 horas, o hasta 30 días si la sesión se abrió con `rememberMe`); no existe lista de revocación ni invalidación inmediata.

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

**Módulo:** Usuarios | **Actor:** Administrador | **Estado:** Implementado

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
| PATCH  | `/api/admin/users/resend-credentials`           | `ROLE_ADMIN` | Reenvía el acceso (E7) por número de documento: enlace de restablecimiento; `200` con `AdminUserDTO`. |
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

**Request — `PATCH /api/admin/users/resend-credentials`** (E7)

```json
{
  "documentNumber": "1029384756"
}
```

| Campo            | Tipo   | Obligatorio | Reglas                                   |
| ---------------- | ------ | ----------- | ---------------------------------------- |
| `documentNumber` | string | Sí          | `@NotBlank`, 1–15; identifica el perfil. |

El reenvío **no manda la contraseña temporal**: genera un `resetKey` nuevo, marca `mustChangePassword = true` y envía el correo de restablecimiento, de modo que el usuario elige su propia contraseña. Si el usuario tiene una `Notificacion` de `CREDENTIALS` abierta (`PENDIENTE` o `REINTENTAR`), queda `ENVIADA` cuando el correo sale o `REINTENTAR` cuando falla (UC018, E3); sin notificación abierta el reenvío igual manda el correo y no es un error. La entrega es la de UC018 y el estado de entrega es interno: la respuesta no lo expone.

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
  "resetDate": "2026-09-11T15:04:05Z",
  "createdBy": "admin",
  "createdDate": "2026-09-11T15:04:05Z",
  "lastModifiedBy": "admin",
  "lastModifiedDate": "2026-09-11T15:04:05Z"
}
```

`PATCH /api/admin/users`, `PATCH /api/admin/users/activated` y `PATCH /api/admin/users/resend-credentials` responden `200` con un `AdminUserDTO` (`id`, `login`, `email`, `activated`, `mustChangePassword`, `langKey`, `imageUrl`, auditoría y `authorities`). Las listas paginadas devuelven arreglos de `AdminUserDTO` / `UserManagementDTO` (`id`, `fullName`, `documentNumber`, `email`, `authorities`, `activated`) más cabeceras `X-Total-Count` y `Link`. `GET /api/admin/users` solo admite `sort` sobre `id`, `login`, `email`, `activated`, `langKey`, `createdBy`, `createdDate`, `lastModifiedBy`, `lastModifiedDate`.

**Errores:** `400 error.idexists`, `400 error.idmissing`, tipo `invalid-password`, `400 error.userexists`, `400 error.emailexists`, `400 error.documentnumberexists`, `400 error.documentTypeNotFound`, `400 error.rolenotfound`, `400 error.documentNumberNotFound` (activación/reenvío con documento inexistente), `400 error.adminprotected` (la cuenta `admin` está protegida), `400 error.lastAdmin` ("Debe existir al menos un Administrador activo"), `400 error.lastInstructor` (lista las materias afectadas); `403`; `404`.

**Notas / lo que se necesita:** reglas de UC006 implementadas, incluido el reenvío manual de credenciales (E7). Un solo rol por cuenta (`ROLE_USER` + rol de dominio); solo se pueden asignar **Administrador, Instructor o Aprendiz**; un rol no asignable o inexistente responde `400 error.rolenotfound`. El **cambio de rol** aplica las mismas guardas que la desactivación: no se puede degradar al **último administrador activo** (`error.lastAdmin`), ni a la **cuenta `admin`** protegida (`error.adminprotected`, que además bloquea su desactivación y el cambio de su documento), ni al **último instructor** de materias de fichas operativas (`error.lastInstructor`, considerando fichas en estado `PENDIENTE` o `ACTIVA`; los estados `FINALIZADA`, `APLAZADA` y `CANCELADA` no cuentan como operativos). El **login se recalcula** cuando cambia el número **o el tipo** de documento. **Los usuarios nunca se eliminan**: el endpoint `DELETE /api/admin/users/{login}` se retiró y responde `405`; el estado se cambia con `PATCH /api/admin/users/activated`. `GET /api/admin/users/search` filtra por texto (nombre, documento o correo), `status` y `role`, y pagina con `X-Total-Count`/`Link` (20 por defecto). Las cuentas creadas por un Administrador nacen con `mustChangePassword = true`; el flujo de cambio obligatorio en el primer inicio es del frontend. El correo de credenciales se envía de forma síncrona al crear y usa el `resetKey` generado en el alta, así que su enlace de restablecimiento nunca va vacío; si el envío falla, se guarda una `Notificacion` pendiente que el Administrador cierra con `PATCH /api/admin/users/resend-credentials` (E7, ver UC018). Existe además el CRUD genérico `/api/user-profiles` sin `@PreAuthorize` (deuda transversal).

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

**Errores:** `400 error.gradeCodeAlreadyUsed` ("El código de ficha ya está en uso"), `400 error.codenotnumeric` ("El código debe contener solo números", solo en `PATCH`), `400 error.datesorder` ("La fecha de fin no puede ser anterior a la fecha de inicio"), `400 error.startdateinpast` ("La fecha de inicio no puede ser anterior a hoy"), `400 error.programInactive` ("El programa seleccionado ya no está disponible, selecciona otro"), `400 error.modalityInactive` ("La modalidad seleccionada ya no está disponible, selecciona otro"), `400 error.timeSlotInactive` ("La jornada seleccionada ya no está disponible, selecciona otro"), `400 error.noteditable` ("No se puede modificar una ficha finalizada"), `400 error.fieldlocked` ("El campo no se puede modificar en el estado actual de la ficha"), `400 error.gradeCodeLocked` ("El código solo puede cambiarse mientras la ficha no tenga materias ni aprendices"), `400 error.invalidtransition` (la acción no aplica al estado actual de la ficha), `400 error.gradeInUse` ("No es posible eliminar la ficha: tiene aprendices vinculados y/o registros de asistencia. Si desea retirarla de operación, use Cancelar ficha"); además de `error.idexists`, `error.idnull`, `error.idnotfound` y de validación; `403`; `404`.

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
| GET    | `/api/class-exceptions`      | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Lista paginada de excepciones no lectivas; el instructor ve solo las de sus materias (A4 de UC009).                       |
| GET    | `/api/class-exceptions/{id}` | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Detalle; fuera de las materias del instructor responde `404`.                                                             |
| POST   | `/api/class-exceptions`      | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Crea; `201`. El instructor solo en sus materias y con fecha no pasada.                                                    |
| PUT    | `/api/class-exceptions`      | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Reemplaza; `200`. El `id` viaja **solo en el body**.                                                                      |
| PATCH  | `/api/class-exceptions`      | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Actualización parcial; `200`. El `id` viaja **solo en el body**.                                                          |
| DELETE | `/api/class-exceptions/{id}` | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Elimina; `204`. Una fecha pasada es un precedente y no se elimina.                                                        |

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
| `date`         | string (`YYYY-MM-DD`) | Sí          | `@NotNull`; una fecha pasada no se puede crear, ni mover, ni eliminar (`400 error.pastExceptionLocked`, A4 de UC009). |
| `reason`       | string                | Sí          | `@NotNull`, máximo 200. |
| `classSection` | objeto                | Sí          | `@NotNull`.             |

**Response:** `201 Created` con el DTO creado (`ClassSectionDTO` con `id`, `subjectName`, `isActive`, `instructor`, `grade`; `ClassScheduleDTO` con `id`, `dayOfWeek`, `startTime`, `endTime`, `trimester`, `classSection`; `ClassExceptionDTO` con `id`, `date`, `reason`, `classSection`). Las listas paginan con `X-Total-Count` y `Link`; los CRUD devuelven los errores `idexists`, `idnull`, `idnotfound` y `error.validation` (ya no existe `error.idinvalid`).

**Errores:** `400 error.gradeNotOperable` ("No se pueden crear ni modificar materias en una ficha en su estado actual", E1); `400 error.classSectionNameAlreadyUsed` ("Ya existe una materia con este nombre en esta ficha", E2); `400 error.scheduleOutOfTimeSlot` ("El horario debe estar dentro de la jornada de la ficha", E3); `400 error.scheduleOverlap` ("El horario se solapa con otro horario de la ficha en ese trimestre", E4); `400 error.scheduleCrossesMidnight` ("La sesión debe iniciar y terminar el mismo día", E5); `400 error.trimesterClosed` ("No se pueden modificar los horarios: el trimestre ya fue cerrado", E6); `400 error.instructorInactive` ("El instructor seleccionado ya no está disponible, selecciona otro", E7); `400 error.classSectionInUse` ("No es posible eliminar la materia: tiene registros de asistencia. Puede desactivarla para retirarla de operación", A3); `400 error.classSectionInUseWithJustifications` ("No es posible eliminar la materia: tiene justificaciones asociadas. Puede desactivarla para retirarla de operación", A3, defensa en profundidad); `400 error.notYourClassSection` ("Solo el instructor asignado a la materia puede gestionar sus fechas no lectivas", A4 de UC009); `400 error.pastExceptionLocked` ("Una fecha no lectiva pasada solo puede modificar su motivo: el precedente no se elimina", A4 de UC009); además de `error.idexists`, `error.idnull`, `error.idnotfound` y de validación; `403`; `404`.

**Notas / lo que se necesita:** reglas de UC015 implementadas. **Crear y modificar (E1):** solo en fichas `PENDIENTE` o `ACTIVA` (incluye la reactivación A4); una ficha `APLAZADA`, `CANCELADA` o `FINALIZADA` responde `400 error.gradeNotOperable`. **Nombre único (E2):** se recorta y se compara sin distinguir mayúsculas dentro de la ficha; en `PUT`/`PATCH` la materia no colisiona consigo misma. **Instructor (E7):** es opcional; si se envía, debe existir y su cuenta estar activa. **Horarios:** `dayOfWeek` es obligatorio; cada sesión debe iniciar y terminar el mismo día (E5), caer dentro de la jornada de la ficha (E3) y no solaparse con otro horario de la misma ficha en el mismo trimestre y día —de esta materia o de otra—, y los rangos adyacentes no cuentan como solapamiento (E4). **Trimestre cerrado (E6):** no se crean, modifican ni eliminan horarios de un trimestre `CERRADO`; la clasificación se hace **por fechas** con `TrimesterService.classify`, no por el `status` persistido, para que no exista una ventana de gracia tras el cierre. **Eliminar (A3):** si la materia tiene asistencias, `DELETE` responde `400 error.classSectionInUse` y debe **desactivarse** con `PATCH /api/class-sections` (`isActive: false`); si no, se elimina y borra en cascada sus horarios y excepciones. Una segunda guarda de defensa en profundidad bloquea el borrado con `400 error.classSectionInUseWithJustifications` cuando existe al menos un `JustificationDetails` de la materia: en el flujo real es casi inalcanzable (crear una justificación exige fallas reales, y las fallas implican asistencias, que ya bloquean), pero evita dejar partes huérfanas en datos legacy. En `PUT` y `PATCH` de `class-sections`, `class-schedules` y `class-exceptions` el `id` viaja **solo en el body** (ruta sin `{id}`); si falta, `400 error.idnull`; si no existe, `400 error.idnotfound`. Las **escrituras** de `class-sections` y `class-schedules` (`POST`, `PUT`, `PATCH`, `DELETE`) quedan restringidas a `ROLE_ADMIN`; las de `class-exceptions` aceptan `ROLE_ADMIN` o al **instructor asignado a la materia** (A4 de UC009); `GET /api/class-sections/mine` acepta solo `ROLE_INSTRUCTOR` o `ROLE_ADMIN` (ya no Coordinador); los `GET` genéricos de materias (`/api/class-sections` y `/api/class-sections/{id}`) también quedan solo para `ROLE_ADMIN`, y el instructor consulta las suyas por `/mine`. Los `GET` de `class-schedules` siguen disponibles para cualquier usuario autenticado (deuda transversal en [`docs/backend-debt.md`](./backend-debt.md)); los `GET` de `class-exceptions` quedan acotados al instructor (solo las excepciones de sus materias). UC015 no cambió el modelo de datos: no agrega migraciones Mongock (el próximo orden libre es 016).

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

**Notas / lo que se necesita:** reglas de UC008 implementadas. **Vincular:** el request identifica al aprendiz por **número de documento** y el servidor resuelve el `UserProfile`; el vínculo nace con `stateAcademic = MATRICULADO` **fijado por el servidor** (el valor que envíe el cliente se ignora). El documento se valida con el mismo formato de UC001 (solo dígitos, 1–30). **E1:** el número de documento no es único por sí solo —la clave única es el par tipo + número—, así que un número compartido por más de un perfil no identifica a un aprendiz y se rechaza con `error.apprenticeInactive`, igual que una cuenta inexistente o desactivada. **E2:** `error.apprenticeAlreadyEnrolled` bloquea el reingreso a la **misma** ficha aunque el registro previo esté desvinculado. **E3:** vincular y desvincular solo operan sobre fichas `PENDIENTE` o `ACTIVA`. **A1 — Desvincular:** el id del vínculo viaja en el body de `PATCH /api/apprentices/unlinked`; sin asistencias en la ficha el registro se **elimina** (`204`) y con asistencias se **conserva** con el `stateAcademic` igual al motivo (`200`), de modo que el historial de asistencia nunca se pierde. **A2 — Consultar:** `GET /api/apprentices` acepta los cuatro filtros opcionales más la paginación estándar y devuelve del aprendiz el documento y el nombre. **Roles:** `POST` y `PATCH /unlinked` son solo `ROLE_ADMIN`; `GET` de lista y detalle quedan para `ROLE_ADMIN` o `ROLE_INSTRUCTOR`. Se retiraron los endpoints genéricos `PUT`, `PATCH` y `DELETE /api/apprentices/{id}`. UC008 no cambió el modelo de datos: no agrega migraciones Mongock (el próximo orden libre es 016). Verificación: `ApprenticeResourceIT` 34/34.

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

**Notas / lo que se necesita:** implementado y verificado (`ClassSectionResourceIT` 65/65). Las fichas se **derivan** de las materias porque la pertenencia del instructor a una ficha nace de sus asignaciones (`docs/use-cases.md:910`); el listado resuelve el perfil autenticado por `UserProfile` y sus materias por instructor, y no valida el estado de la cuenta. La **búsqueda** del flujo alternativo es el parámetro opcional `gradeCode` de `/mine`: coincidencia **parcial** y **sin distinguir mayúsculas** contra el `code` de la ficha, aplicada **solo entre las materias del instructor** (`docs/use-cases.md:916`); sin coincidencias responde `200 []`. El payload es **aditivo**: la ficha anidada expone `state`, `startDate`, `endDate` y `program { id, name }` además de `id` y `code`, y la materia mantiene `subjectName` e `isActive`. El `instructor` anidado sigue exponiendo solo `{ id, documentNumber }`: para "mis materias" el instructor es el propio usuario autenticado, así que no aporta enriquecerlo. **Decisiones:** no se pagina (la spec no lo pide y es una lista personal); no se filtran las materias inactivas ni las fichas no operativas porque E2 pide mostrarlas con su estado (`docs/use-cases.md:921`); y un instructor sin materias asignadas recibe `200 []`, con el mensaje de E3 a cargo del frontend. Los `GET` genéricos de fichas y materias (`/api/grades`, `/api/grades/{id}`, `/api/grades/active`, `/api/class-sections` y `/api/class-sections/{id}`) quedan reservados a `ROLE_ADMIN`, así que el instructor no lista fichas ni materias ajenas por esa vía. Sin migración Mongock (próximo orden libre: 016).

---

## UC009 — Gestionar listas de asistencia

**Módulo:** Asistencia | **Actor:** Instructor | **Estado:** Implementado

**Feature:** Registro de la asistencia por **sesión** (materia + fecha) con guardado masivo de las marcaciones confirmadas, edición puntual del historial (A2), consulta con filtros acotada por rol (A1: el instructor a sus materias, el aprendiz a sus propios registros) y administración de las fechas no lectivas de la materia (A4). El instructor solo marca `PRESENTE` o `FALLA`; `JUSTIFICADA` llega por UC010.

**Endpoints:**

| Método | Ruta                       | Acceso                           | Descripción                                                                                                                                                        |
| ------ | -------------------------- | -------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| PUT    | `/api/attendances/session` | `ROLE_INSTRUCTOR`                | Registra la sesión de una materia en una fecha con las marcaciones confirmadas; `200` con la sesión y sus conteos. Guardar la misma sesión otra vez es idempotente. |
| PATCH  | `/api/attendances/{id}`    | `ROLE_INSTRUCTOR`                | Edición A2: cambia solo el estado de un registro de sus materias; `200`.                                                                                            |
| GET    | `/api/attendances`         | `ROLE_ADMIN`, `ROLE_INSTRUCTOR` o `ROLE_APPRENTICE` | Historial **paginado** (20 por defecto) con filtros opcionales; el instructor ve solo los registros de sus materias y el aprendiz solo los suyos (UC011). |
| GET    | `/api/attendances/{id}`    | `ROLE_ADMIN`, `ROLE_INSTRUCTOR` o `ROLE_APPRENTICE` | Detalle; un registro fuera del alcance de lectura responde `404`.                                                                                          |

**Endpoints retirados:** `POST /api/attendances`, `PUT /api/attendances/{id}` y `DELETE /api/attendances/{id}` responden `405`. El UC solo contempla registrar la sesión, editarla (A2) y consultarla: un registro de asistencia no se crea suelto ni se elimina.

**Request — `PUT /api/attendances/session`**

```json
{
  "classSection": { "id": "665f1c2a9e13b7a1f2c8d9e90" },
  "date": "2026-09-15",
  "attendances": [
    { "studentId": "665f1c2a9e13b7a1f2c8d9ea0", "stateAttendance": "PRESENTE" },
    { "studentId": "665f1c2a9e13b7a1f2c8d9eb0", "stateAttendance": "FALLA" }
  ]
}
```

| Campo                          | Tipo                  | Obligatorio | Reglas                                                                                                                       |
| ------------------------------ | --------------------- | ----------- | ----------------------------------------------------------------------------------------------------------------------------- |
| `classSection`                 | objeto                | Sí          | `@NotNull`; `{ id }` de una materia existente.                                                                                |
| `date`                         | string (`YYYY-MM-DD`) | Sí          | `@NotNull`; fecha de la sesión, sujeta a las guardas de fecha (ver notas).                                                    |
| `attendances`                  | arreglo               | Sí          | `@NotNull`; solo las marcaciones confirmadas. El backend **no** rellena con `PRESENTE` a los aprendices ausentes del payload. |
| `attendances[].studentId`      | string                | Sí          | `@NotNull`; id del `UserProfile` del aprendiz (no el número de documento).                                                    |
| `attendances[].stateAttendance` | string                | Sí          | `@NotNull`; enum `StateAttendance`: el instructor solo puede enviar `PRESENTE` o `FALLA`.                                     |
| `attendances[].id`             | string                | No          | Eco opcional de un registro devuelto antes; el servidor lo **ignora** porque el upsert es por materia, aprendiz y fecha.      |

**Response — `PUT /api/attendances/session`:** `200 OK`

```json
{
  "classSection": { "id": "665f1c2a9e13b7a1f2c8d9e90", "subjectName": "Programación orientada a objetos" },
  "date": "2026-09-15",
  "records": [
    {
      "id": "665f1c2a9e13b7a1f2c8d9ec0",
      "date": "2026-09-15",
      "stateAttendance": "PRESENTE",
      "classSection": { "id": "665f1c2a9e13b7a1f2c8d9e90", "subjectName": "Programación orientada a objetos" },
      "student": {
        "id": "665f1c2a9e13b7a1f2c8d9ea0",
        "documentNumber": "1029384756",
        "firstName": "Ana",
        "firstLastName": "Gómez"
      },
      "modifiedByJustification": null
    }
  ],
  "complete": false,
  "enrolledCount": 25,
  "recordedCount": 1
}
```

`records` contiene los registros persistidos de esa materia y fecha (mismo shape que `AttendanceDTO`); la sesión es **completa** cuando `recordedCount == enrolledCount`, donde `enrolledCount` cuenta los aprendices `MATRICULADO` de la ficha y `recordedCount` los registros de la fecha. Los aprendices ausentes del payload quedan **sin registro** (A5).

**Request — `PATCH /api/attendances/{id}`** (edición A2)

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9ec0",
  "stateAttendance": "FALLA"
}
```

| Campo              | Tipo   | Obligatorio | Reglas                                                                                    |
| ------------------ | ------ | ----------- | ------------------------------------------------------------------------------------------ |
| `id`               | string | Sí          | Debe coincidir con el de la ruta; si falta, `400 error.idnull`, y si no coincide, `error.idinvalid`. |
| `stateAttendance`  | string | Sí          | Solo `PRESENTE` o `FALLA`; cualquier otro valor (o su ausencia) responde `400 error.invalidAttendanceState`. |

**Response — `PATCH /api/attendances/{id}`:** `200 OK` con el `AttendanceDTO` actualizado; `404` si el registro no existe. Un registro de otra materia responde `400 error.notYourClassSection`.

**Request — `POST /api/class-exceptions`** (A4; contrato completo en UC015)

```json
{
  "date": "2026-09-21",
  "reason": "Festivo",
  "classSection": { "id": "665f1c2a9e13b7a1f2c8d9e90" }
}
```

Un instructor solo gestiona las excepciones de sus materias; una fecha pasada es un precedente que solo admite cambiar el motivo.

**Errores:**

| Código | errorKey (cuerpo `message`)         | Causa                                                                                                        |
| ------ | ----------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| 400    | `error.idnotfound`                  | La materia de la sesión no existe.                                                                            |
| 400    | `error.classSectionWithoutInstructor` | E6: la materia no tiene instructor asignado.                                                                |
| 400    | `error.notYourClassSection`         | El instructor autenticado no es el asignado a la materia (sesión, A2 y A4 de excepciones).                    |
| 400    | `error.futureSessionDate`           | E3: la fecha de la sesión es posterior a hoy.                                                                 |
| 400    | `error.trimesterClosed`             | E1: el trimestre que contiene la fecha está `CERRADO`.                                                        |
| 400    | `error.dateOutOfTrimester`          | E2: la fecha no cae en ningún trimestre o cae en uno `FUTURO`.                                                |
| 400    | `error.dateOutOfGradeRange`         | E2: la fecha está fuera del rango de fechas de la ficha.                                                      |
| 400    | `error.nonTeachingDate`             | E4: la fecha está marcada como no lectiva para la materia.                                                    |
| 400    | `error.noActiveApprentices`         | E5: la ficha no tiene aprendices `MATRICULADO`.                                                               |
| 400    | `error.invalidAttendanceState`      | El estado no es `PRESENTE` ni `FALLA` (incluye el PATCH sin estado).                                          |
| 400    | `error.studentNotEnrolled`          | Una marcación apunta a un aprendiz que no está matriculado en la ficha.                                       |
| 400    | `error.idnull` / `error.idinvalid`  | PATCH sin `id` o con un `id` distinto del de la ruta.                                                         |
| 400    | `error.pastExceptionLocked`         | A4: una fecha no lectiva pasada solo admite cambiar el motivo; no se crea, mueve ni elimina una pasada.        |
| 400    | `error.validation`                  | Fallo de validación del cuerpo.                                                                               |
| 403    | —                                   | Rol sin permiso: la sesión y la edición A2 son del instructor; las excepciones aceptan Admin o instructor.    |
| 404    | —                                   | Registro inexistente o fuera del alcance de lectura del instructor.                                            |

**Notas / lo que se necesita:** implementado y verificado (`AttendanceResourceIT` 43/43, `AuditLogResourceIT` 25/25, `ClassExceptionResourceIT` 37/37 + migraciones 011/012 + 229 unitarias). **Sesión:** `PUT /api/attendances/session` hace upsert por materia, aprendiz y fecha; los aprendices ausentes del payload **quedan sin registro** y la sesión se reporta incompleta (`complete = recordedCount == enrolledCount`), el A5 de la spec. El backend **no** rellena con `PRESENTE` a los no enviados: el cliente presenta la lista en A por defecto y al guardar envía todas las marcaciones confirmadas; guardar la misma sesión dos veces es idempotente y la respuesta siempre es `200` con la sesión persistida. **Guardas de la sesión (en orden):** materia inexistente (`idnotfound`), materia sin instructor (`classSectionWithoutInstructor`, E6), instructor ajeno (`notYourClassSection`), fecha futura (`futureSessionDate`, E3), trimestre cerrado (`trimesterClosed`, E1), fecha fuera del trimestre vigente (`dateOutOfTrimester`, E2), fecha fuera del rango de la ficha (`dateOutOfGradeRange`, E2), fecha no lectiva (`nonTeachingDate`, E4), ficha sin aprendices matriculados (`noActiveApprentices`, E5) y, por cada marcación, estado inválido (`invalidAttendanceState`) o aprendiz no matriculado (`studentNotEnrolled`). Todo se valida antes de escribir, así que una sesión rechazada no persiste nada. **Edición A2:** `PATCH /api/attendances/{id}` es solo del instructor asignado, solo cambia `stateAttendance` a `PRESENTE` o `FALLA` y bloquea el trimestre cerrado; un registro ajeno responde `400 notYourClassSection` y un trimestre cerrado, `400 trimesterClosed`. **Consulta A1:** los `GET` aceptan `ROLE_ADMIN`, `ROLE_INSTRUCTOR` o `ROLE_APPRENTICE`; el instructor solo lee los registros de sus materias y el aprendiz solo los propios (un id ajeno responde `404`) y los filtros opcionales `classSectionId`, `date`, `studentId` (id del `UserProfile`) y `stateAttendance` se combinan entre sí y con ese alcance mediante la paginación estándar (`page`/`size`/`sort`, `X-Total-Count` y `Link`). **Auditoría:** cada cambio real de estado (sesión y PATCH) escribe un `AuditLog` con `previousState`, `newState`, `editDate`, `modifiedBy` y `attendance`; crear un registro no genera log y reescribir el mismo estado tampoco. `AuditLogResource` quedó solo para `ROLE_ADMIN`. **A4 — excepciones no lectivas:** además del Administrador, el instructor asignado gestiona las de sus materias (`/api/class-exceptions`, contrato en UC015). Una fecha pasada es un precedente: no se puede crear una pasada, ni cambiar la fecha o la materia de una pasada, ni eliminarla (`pastExceptionLocked`); solo se admite cambiar el motivo. Los `GET` de excepciones quedan acotados al instructor. **Estados y migraciones:** `StateAttendance` es `PRESENTE`, `FALLA` y `JUSTIFICADA`; el instructor nunca marca `JUSTIFICADA` (llega por UC010). `TARDE` se retiró con la migración Mongock **011** (`MigrateAttendanceTardeToPresente`) y la **012** (`MigrateAuditLogTardeToPresente`) reescribe `TARDE`→`PRESENTE` en `previous_state`/`new_state` de `audit_log`, porque los documentos legacy rompían la deserialización. La migración **013** (`MigrateCoordinatorRemoval`) elimina el rol Coordinador de las bases existentes; el próximo orden libre es **016**.

---

## UC011 — Gestionar asistencia (Aprendiz)

**Módulo:** Justificaciones | **Actor:** Aprendiz | **Estado:** Implementado

**Feature:** El aprendiz consulta su historial de asistencia, presenta justificaciones de inasistencia con tipo, rango de fechas, materias afectadas, descripción y soporte, hace seguimiento a sus estados, las edita o cancela mientras estén pendientes y subsana las partes rechazadas. El modelo separa la cabecera (`Justification`) de la decisión por materia (`JustificationDetails`); el servidor crea una parte `PENDIENTE` por cada materia afectada.

**Endpoints:**

| Método              | Ruta                              | Acceso                           | Descripción                                                                                                                       |
| ------------------- | --------------------------------- | -------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| GET                 | `/api/justifications`             | `ROLE_ADMIN` o `ROLE_APPRENTICE` | Lista paginada; el Administrador lee todas y el aprendiz solo las suyas.                                                          |
| GET                 | `/api/justifications/{id}`        | `ROLE_ADMIN` o `ROLE_APPRENTICE` | Detalle; una justificación de otro aprendiz responde `404`, sin revelar su existencia.                                            |
| POST                | `/api/justifications`             | `ROLE_ADMIN` o `ROLE_APPRENTICE` | Crea la cabecera y una parte `PENDIENTE` por materia; `201` con `onTime` y `detailses`.                                           |
| PUT                 | `/api/justifications/{id}`        | `ROLE_ADMIN` o `ROLE_APPRENTICE` | Reemplaza la cabecera; solo si todas las partes están `PENDIENTE`; recalcula plazo y cupo.                                        |
| PATCH               | `/api/justifications/{id}`        | `ROLE_ADMIN` o `ROLE_APPRENTICE` | Actualización parcial de la cabecera; mismas reglas de edición que `PUT`.                                                         |
| PATCH               | `/api/justifications/cancelled`   | `ROLE_ADMIN` o `ROLE_APPRENTICE` | Cancelación (A4): el `id` viaja en el body; las partes pasan a `CANCELADA` y el cupo se libera.                                   |
| GET                 | `/api/justification-details`      | `ROLE_ADMIN` o `ROLE_APPRENTICE` | Lista paginada de partes; el Administrador lee todas y el aprendiz solo las de sus justificaciones.                               |
| GET                 | `/api/justification-details/{id}` | `ROLE_ADMIN` o `ROLE_APPRENTICE` | Detalle de una parte; una parte ajena responde `404`.                                                                             |
| POST · PUT · DELETE | `/api/justification-details`      | `ROLE_ADMIN` o `ROLE_APPRENTICE` | CRUD genérico de partes con validación de propiedad; `DELETE` no cancela la justificación (la cancelación es `PATCH /cancelled`). |
| PATCH               | `/api/justification-details/{id}` | `ROLE_ADMIN` o `ROLE_APPRENTICE` | Subsanación (A5): solo copia texto y archivo de corrección; una parte `RECHAZADA` vuelve a `PENDIENTE` dentro de los 2 días hábiles. |

**Endpoints retirados:** `DELETE /api/justifications/{id}` responde `405`. La cancelación del flujo (A4) se hace con `PATCH /api/justifications/cancelled` y el `id` en el body.

**Alcance por rol:** el Instructor no opera estos recursos genéricos (responde `403`): su flujo de decisión vive en UC010 (`GET /api/justification-details/pending` y `PATCH /api/justification-details/{id}/decision`). El Administrador conserva acceso total y el aprendiz solo opera sobre sus propias justificaciones y partes, tanto en lectura como en escritura (ver notas).

**Request — `POST /api/justifications`**

```json
{
  "description": "Incapacidad médica por gripe",
  "startDate": "2026-09-10",
  "endDate": "2026-09-12",
  "evidence": "JVBERi0xLjQK...",
  "evidenceContentType": "application/pdf",
  "detailses": [{ "classSection": { "id": "665f1c2a9e13b7a1f2c8d9e90" } }],
  "justificationType": { "id": "665f1c2a9e13b7a1f2c8d9ed0" },
  "student": { "id": "665f1c2a9e13b7a1f2c8d9ea0" }
}
```

`PUT /api/justifications/{id}` usa el mismo cuerpo (con `id` obligatorio en el body) y `PATCH /api/justifications/{id}` acepta los mismos campos de forma parcial (`application/json` o `application/merge-patch+json`); en ambos el `id` de la ruta debe coincidir con el del body (`error.idinvalid`).

| Campo                   | Tipo                        | Obligatorio       | Reglas                                                                                                                                                                                                                                                     |
| ----------------------- | --------------------------- | ----------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `description`           | string                      | Sí                | `@NotNull`, máximo 300.                                                                                                                                                                                                                                    |
| `startDate` / `endDate` | string (`YYYY-MM-DD`)       | Sí                | `@NotNull`; el inicio no puede ser posterior al fin (`error.datesorder`). El rango debe cubrir fallas reales del aprendiz (`error.noFailuresFound`, E4).                                                                                                   |
| `evidence`              | string (base64 de `byte[]`) | No                | Soporte. Si `evidenceContentType` viaja, el archivo debe ser PDF o imagen y no superar **5 MB** (`error.invalidEvidence`, E1). Un soporte ausente o incompleto cae en `error.validation` (E2).                                                               |
| `evidenceContentType`   | string                      | Sí                | `@NotNull`; tipo MIME declarado por el cliente.                                                                                                                                                                                                            |
| `detailses`             | arreglo                     | Sí en la práctica | Una entrada por materia afectada, `{ "classSection": { "id": "..." } }`. En `POST` el servidor crea una parte `PENDIENTE` por cada materia persistida; en `PUT`/`PATCH` delimitan las materias con las que se revalidan plazo y cupo y **no reemplazan** las partes persistidas. |
| `justificationType`     | objeto                      | Sí                | `@NotNull`; tipo del catálogo UC016, que debe existir y estar `ACTIVO` (`error.justificationTypeInactive`).                                                                                                                                                |
| `student`               | objeto                      | Sí                | `@NotNull`; perfil del aprendiz. Para un aprendiz debe ser el suyo (`error.notYourJustification` si no); el Administrador puede indicar cualquiera.                                                                                                        |
| `onTime`                | boolean                     | No                | Marca calculada por el servidor: el cliente **no debe enviarla** y su valor se recalcula (o se conserva) en el servidor. Viaja en la respuesta.                                                                                                           |

**Request — `PATCH /api/justification-details/{id}`** (subsanación, A5)

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9ef0",
  "correctionText": "Adjunto el certificado médico corregido",
  "correctionFileUrl": "JVBERi0xLjQK...",
  "correctionFileUrlContentType": "application/pdf"
}
```

| Campo                          | Tipo            | Obligatorio | Reglas                                                                                           |
| ------------------------------ | --------------- | ----------- | ------------------------------------------------------------------------------------------------ |
| `correctionText`               | string          | No          | Texto de la corrección, máximo 300.                                                              |
| `correctionFileUrl`            | string (base64) | No          | Archivo de corrección; el `PATCH` solo copia este campo y su MIME, nunca el estado ni las relaciones. |
| `correctionFileUrlContentType` | string          | No          | MIME del archivo de corrección.                                                                  |

Una parte `RECHAZADA` vuelve a `PENDIENTE` con `responseDate` en `null` y conserva `rejectionReason` como traza hasta la próxima decisión (UC010). Una parte `PENDIENTE` solo actualiza sus campos de corrección y una parte `ACEPTADA` o `CANCELADA` responde `error.alreadyProcessed`.

**Response — `POST /api/justifications`:** `201 Created`; `PUT`, `PATCH` y `PATCH /cancelled` responden `200 OK` con el `JustificationDTO` completo:

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9ee0",
  "description": "Incapacidad médica por gripe",
  "startDate": "2026-09-10",
  "endDate": "2026-09-12",
  "evidence": "JVBERi0xLjQK...",
  "evidenceContentType": "application/pdf",
  "onTime": true,
  "justificationType": { "id": "665f1c2a9e13b7a1f2c8d9ed0", "name": "Incapacidad médica" },
  "student": { "id": "665f1c2a9e13b7a1f2c8d9ea0", "documentNumber": "1029384756" },
  "detailses": [
    {
      "id": "665f1c2a9e13b7a1f2c8d9ef0",
      "stateJustification": "PENDIENTE",
      "rejectionReason": "",
      "correctionText": "",
      "correctionFileUrlContentType": "",
      "responseDate": null,
      "classSection": { "id": "665f1c2a9e13b7a1f2c8d9e90", "subjectName": "Programación orientada a objetos" }
    }
  ]
}
```

La respuesta incluye `onTime` y las partes (`detailses`) con el estado de cada decisión; el tipo anidado expone `id` y `name`, y el estudiante `id` y `documentNumber`. La lista de justificaciones y la de partes son **paginadas** (20 por defecto, `X-Total-Count` y `Link`).

**Errores:** todos los fallos de negocio son `400 Bad Request` con la clave en `$.message`:

| errorKey (cuerpo `message`)        | Causa                                                                                                                         |
| ---------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| `error.idexists`                   | `POST` con un `id` ya presente.                                                                                               |
| `error.idnull` / `error.idinvalid` | `PUT`/`PATCH` sin `id` en el body o con un `id` distinto del de la ruta; `error.idnotfound` si no existe.                      |
| `error.validation`                 | Campos obligatorios ausentes o inválidos (E2).                                                                                |
| `error.notYourJustification`       | La justificación o la parte pertenece a otro aprendiz, o el `student` del payload no es el autenticado.                       |
| `error.alreadyProcessed`           | Edición o cancelación de una justificación con alguna parte decidida, o `PATCH` de una parte `ACEPTADA`/`CANCELADA` (E3).     |
| `error.noFailuresFound`            | El rango y las materias no cubren ninguna falla real del aprendiz (E4).                                                       |
| `error.justificationTypeInactive`  | El tipo no existe o no está `ACTIVO` (UC016).                                                                                 |
| `error.invalidEvidence`            | El soporte no es PDF/imagen o supera 5 MB (E1).                                                                               |
| `error.datesorder`                 | `startDate` posterior a `endDate`; **clave compartida** con UC007/UC014.                                                      |
| `error.trimesterClosed`            | Alguna falla cae en un trimestre `CERRADO` (E7); **clave compartida** con UC009/UC015.                                        |
| `error.notMatriculado`             | Alguna materia pertenece a una ficha donde el aprendiz no está `MATRICULADO` (E8).                                            |
| `error.quotaExceeded`              | Las fechas del envío superan el cupo del tipo (E6); el texto con los días restantes viaja en `title`/`detail` y la clave en `message`. |
| `error.correctionExpired`          | La subsanación de una parte rechazada llegó fuera de los 2 días hábiles (E5).                                                 |
| `403`                              | El Instructor en el CRUD genérico (su flujo de decisión vive en UC010) o cualquier rol fuera de Administrador/Aprendiz.       |
| `404`                              | Justificación o parte inexistente o fuera del alcance de lectura del aprendiz.                                                |

**Notas / lo que se necesita:** UC011 implementado y verificado (`JustificationResourceIT` 53/53, `JustificationDetailsResourceIT` 55/55 + unitarias).

- **Identidad y seguridad:** las operaciones se acotan en el servicio al aprendiz autenticado; una justificación o parte ajena responde `404` en lectura y `error.notYourJustification` en escritura, y el `student` del payload debe ser el propio. Las partes exigen `ROLE_ADMIN` o `ROLE_APPRENTICE` con validación de propiedad (el instructor solo suma la lectura del detalle de las partes de sus materias, UC010). El **Instructor queda fuera del CRUD genérico**: su flujo de decisión vive en UC010 (bandeja, detalle y decisión por materia).
- **Historial de asistencia (paso 1):** `GET /api/attendances` acepta `ROLE_APPRENTICE` y acota la lectura a sus propios registros; con los filtros `classSectionId`, `date` y `stateAttendance=FALLA` el aprendiz obtiene las fallas que puede justificar (contrato completo en UC009).
- **Plazo (marca `onTime`):** la fecha límite se cuenta desde el **día hábil siguiente a la última falla cubierta** (ese día cuenta como día 1) sumando `studentJustificationDays` de la configuración global (UC019). Los días hábiles son **lunes a viernes**: el proyecto no tiene calendario de festivos. La marca **no bloquea** el envío; solo lo clasifica en tiempo (`true`) o fuera de tiempo (`false`), y se recalcula en cada edición pendiente.
- **Cupo (E6):** `limitPerTrimester` del tipo es el máximo de **días con falla (fechas distintas)** justificables por trimestre. Cuentan las justificaciones del mismo tipo y aprendiz con partes en `PENDIENTE` o `ACEPTADA`; las partes `RECHAZADA` y `CANCELADA` liberan sus fechas, y una fecha ya cubierta no vuelve a contar. El cupo se calcula cruzando el rango de cada justificación con las fallas reales del aprendiz en las materias de sus partes (no hay campo de fechas); al excederse responde `error.quotaExceeded` e informa los días restantes.
- **Ciclo de vida:** editar (`PUT`/`PATCH`) solo mientras todas las partes estén `PENDIENTE`; en caso contrario responde `error.alreadyProcessed` (E3) y recalcula plazo y cupo. Cancelar (A4) con `PATCH /api/justifications/cancelled` (el `id` en el body): las partes pasan a `CANCELADA` y liberan cupo; `DELETE` está retirado (`405`). Subsanar (A5) una parte `RECHAZADA` con `PATCH /api/justification-details/{id}` dentro de **2 días hábiles** desde `responseDate`: la parte vuelve a `PENDIENTE` con `responseDate` nula y conserva el motivo de rechazo como traza; fuera del plazo responde `error.correctionExpired` (E5).
- **Notificaciones:** cada cambio de estado (creación → `PENDIENTE`, cancelación → `CANCELADA`) invoca una sola vez el puerto `JustificationNotificationPort`; su implementación (UC018) es una entrega real que persiste la notificación `JUSTIFICACION` con la referencia a la justificación: la creación avisa al aprendiz dueño y a cada instructor de las materias afectadas, y la cancelación solo al aprendiz. La decisión del instructor (UC010) notifica `ACEPTADA`/`RECHAZADA` una vez por parte decidida, con el mismo puerto.
- **Modelo:** `StateJustification` incorpora `CANCELADA`. El campo `onTime` es **aditivo** y no requiere migración Mongock; el próximo orden libre es **016**.

---

## UC010 — Gestionar justificaciones

**Módulo:** Justificaciones | **Actor:** Instructor | **Estado:** Implementado

**Feature:** Revisión y decisión de las justificaciones recibidas, con una decisión independiente por materia. El instructor consulta las partes pendientes de las materias que dicta y decide cada una (aprobar o rechazar); al aprobar, las fallas (`FALLA`) del aprendiz en esa materia dentro del período pasan a `JUSTIFICADA` con auditoría. La marca de plazo de la justificación (`onTime`) se conserva: la decisión nunca la recalcula. Una decisión que llega después del plazo de respuesta del instructor queda marcada como demorada en la parte.

**Endpoints:**

| Método | Ruta                                         | Acceso                                                                                                          | Descripción                                                                                                                       |
| ------ | -------------------------------------------- | --------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| GET    | `/api/justification-details/pending`         | `ROLE_ADMIN` o `ROLE_INSTRUCTOR`                                                                                | Bandeja de partes; por defecto solo `PENDIENTE` y el instructor ve únicamente las materias asignadas (el Administrador ve todas). |
| GET    | `/api/justification-details/{id}`            | `ROLE_ADMIN`, `ROLE_INSTRUCTOR` (materias asignadas) o `ROLE_APPRENTICE` (justificaciones propias)              | Detalle de la parte con el soporte adjunto; fuera del alcance responde `404`.                                                     |
| PATCH  | `/api/justification-details/{id}/decision`   | `ROLE_ADMIN` o `ROLE_INSTRUCTOR`                                                                                | Decide una parte `PENDIENTE` (`ACEPTADA` o `RECHAZADA`). Solo el instructor asignado a la materia al momento de decidir (E4).     |

**Bandeja — `GET /api/justification-details/pending`:** paginada (20 por defecto, `sort=id,desc`: la solicitud más reciente primero, `X-Total-Count` y `Link`). Los filtros opcionales se combinan con AND:

| Parámetro                    | Tipo         | Reglas                                                                                                       |
| ---------------------------- | ------------ | ------------------------------------------------------------------------------------------------------------ |
| `stateJustification`         | string       | Estado a incluir; sin él se listan solo las `PENDIENTE` y con otro estado se consulta el histórico.           |
| `classSectionId`             | string       | Materia a filtrar.                                                                                           |
| `createdFrom` / `createdTo`  | `YYYY-MM-DD` | Rango sobre la **fecha de solicitud** de la cabecera; ambos extremos son inclusive (`createdTo` cubre el día completo). |

Cada parte de la respuesta expone `stateJustification`, `rejectionReason`, `correctionText`, `responseDate`, `requestDate`, `outOfTimeReason`, `lateDecision`, la materia (`classSection.id`, `classSection.subjectName`) y la cabecera recortada (`justification` con `description`, `startDate`, `endDate`, `onTime`, `justificationType.id`/`.name` y `student`).

**Detalle — `GET /api/justification-details/{id}`:** devuelve la parte con la misma cabecera recortada de la bandeja y, además, el soporte de la cabecera: `justification.evidence` (el archivo en base64) y `justification.evidenceContentType`. Es la lectura con la que el instructor revisa el adjunto antes de decidir (paso 4 del flujo). El alcance de lectura es el del flujo: el Administrador lee cualquier parte; el instructor solo las partes de las **materias asignadas al momento de la consulta** y el aprendiz solo las de sus propias justificaciones. Una parte fuera de ese alcance responde `404`, igual que una inexistente, para no filtrar su existencia. La bandeja `/pending` no devuelve el archivo (`evidence` no viaja en la lista): el soporte se lee de este endpoint.

**Request — `PATCH /api/justification-details/{id}/decision`:**

```json
{
  "stateJustification": "RECHAZADA",
  "rejectionReason": "Soporte no legible",
  "outOfTimeReason": null
}
```

| Campo                | Tipo   | Obligatorio                                                       | Reglas                                                                                            |
| -------------------- | ------ | ----------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| `stateJustification` | string | Sí                                                                | `@NotNull`; solo `ACEPTADA` o `RECHAZADA` (`error.invalidDecisionState`).                         |
| `rejectionReason`    | string | Sí al rechazar                                                    | Máximo 300; sin él responde `error.rejectionReasonRequired` (E1).                                 |
| `outOfTimeReason`    | string | Sí al aprobar una justificación fuera de tiempo (`onTime=false`)  | Máximo 300; sin él responde `error.outOfTimeReasonRequired` (A2). Solo lo escribe la decisión.    |

**Response — `PATCH /api/justification-details/{id}/decision`:** `200 OK` con la parte decidida:

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9ef0",
  "stateJustification": "RECHAZADA",
  "rejectionReason": "Soporte no legible",
  "outOfTimeReason": null,
  "lateDecision": false,
  "responseDate": "2026-09-15T16:04:05Z",
  "requestDate": "2026-09-14T13:20:00Z",
  "classSection": { "id": "665f1c2a9e13b7a1f2c8d9e90", "subjectName": "Programación orientada a objetos" },
  "justification": {
    "id": "665f1c2a9e13b7a1f2c8d9ee0",
    "description": "Incapacidad médica",
    "startDate": "2026-09-10",
    "endDate": "2026-09-12",
    "onTime": true,
    "justificationType": { "id": "665f1c2a9e13b7a1f2c8d9ec0", "name": "Incapacidad médica" },
    "student": { "id": "665f1c2a9e13b7a1f2c8d9ea0", "documentNumber": "1029384756" }
  }
}
```

**Reglas de decisión:**

- **E1 — Rechazo con motivo:** una `RECHAZADA` exige `rejectionReason`; el motivo queda registrado en la parte.
- **E2 — Solo partes pendientes:** una parte ya decidida (`ACEPTADA`, `RECHAZADA` o `CANCELADA`) responde `error.alreadyProcessed`; la subsanación de UC011 reabre una parte rechazada como `PENDIENTE` y la decisión reinicia.
- **E4 — Instructor asignado:** solo decide el instructor de la materia de la parte en el momento de la decisión; cualquier otro instructor recibe `error.notYourClassSection`. El Administrador decide cualquier parte.
- **Aprobación fuera de tiempo (A2):** si la justificación quedó marcada `onTime=false` al enviarse, la aprobación exige `outOfTimeReason`; el motivo viaja en la respuesta y queda en la parte.
- **`onTime` inmutable:** la decisión nunca recalcula la marca de plazo; una misma justificación puede aprobarse en una materia y rechazarse en otra.
- **Sin bloqueo por trimestre:** un trimestre cerrado no impide decidir una parte pendiente y la conversión F→J se aplica igual, aunque el aprendiz esté desvinculado de la ficha.
- **Plazo de respuesta del instructor (UC019):** la fecha límite es la **fecha de solicitud** de la cabecera más `instructorResponseDays` días hábiles (lunes a viernes); decidir el mismo día límite todavía no es tarde. Una decisión posterior guarda `lateDecision=true` como marca de auditoría. No hay escalado ni decisión automática.
- **Notificación:** cada decisión invoca una sola vez `JustificationNotificationPort` con la cabecera y el estado resultante; la entrega real pertenece a UC018.

**Efectos de la aprobación:** convierte a `JUSTIFICADA` cada `FALLA` del aprendiz en la materia de la parte dentro del período justificado, enlaza cada registro con `modifiedByJustification` y escribe un `AuditLog` por cambio real (`previousState`, `newState`, `editDate`, `modifiedBy`, `attendance`). El rechazo solo registra el motivo y la fecha de respuesta.

**Campos nuevos de `JustificationDetails` (UC010):**

| Campo             | JSON              | Reglas                                                                                                     |
| ----------------- | ----------------- | ---------------------------------------------------------------------------------------------------------- |
| `requestDate`     | `requestDate`     | Fecha de solicitud de la cabecera (`createdDate`); la expone el servidor y el cliente no la envía.          |
| `outOfTimeReason` | `outOfTimeReason` | Motivo de la aprobación fuera de tiempo (A2); solo lo escribe la decisión.                                  |
| `lateDecision`    | `lateDecision`    | Marca de decisión demorada; el servidor la calcula al decidir y la limpia cuando UC011 reabre la parte.     |

**Errores:** todos los fallos de negocio son `400 Bad Request` con la clave en `$.message`:

| errorKey (cuerpo `message`)     | Causa                                                                                          |
| ------------------------------- | ---------------------------------------------------------------------------------------------- |
| `error.invalidDecisionState`    | La decisión no es `ACEPTADA` ni `RECHAZADA`.                                                   |
| `error.rejectionReasonRequired` | Rechazo sin motivo (E1).                                                                       |
| `error.outOfTimeReasonRequired` | Aprobación de una justificación `onTime=false` sin el motivo adicional (A2).                    |
| `error.alreadyProcessed`        | La parte ya no está `PENDIENTE` (E2); clave compartida con UC011.                              |
| `error.notYourClassSection`     | Quien decide no es el instructor asignado a la materia (E4); clave compartida con UC009/UC015. |
| `error.validation`              | Campos obligatorios ausentes o inválidos del VM.                                               |
| `404`                           | La parte no existe o está fuera del alcance de lectura (materia de otro instructor).            |

**Notas / lo que se necesita:**

- Implementado y verificado (`JustificationDetailsResourceIT` 55/55 y `JustificationResourceIT` 53/53).
- **Entrega de notificaciones (UC018):** implementada. El puerto `JustificationNotificationPort` se invoca en creación (`PENDIENTE`), cancelación (`CANCELADA`) y decisión (`ACEPTADA`/`RECHAZADA`), y cada evento persiste su notificación `JUSTIFICACION` con la referencia a la justificación (`JustificationNotificationDelivery`).
- **Resolución de alertas (UC013):** tras convertir las fallas F→J, la aprobación reevalúa las alertas activas del aprendiz en la materia y resuelve automáticamente las que quedan por debajo del umbral (A4 de UC013); nunca genera alertas nuevas.
- `instructorResponseDays` ya tiene consumidor: el plazo de respuesta del instructor y la marca `lateDecision`.
- El instructor no opera el CRUD genérico de justificaciones (`/api/justifications` y los endpoints genéricos de `/api/justification-details` le responden `403`), **salvo la lectura del detalle de una parte de sus materias** (`GET /api/justification-details/{id}`, `200` con el soporte; `404` fuera de su alcance): su flujo son la bandeja, el detalle y la decisión de esta sección.

---

## UC013 — Gestionar alertas de inasistencia

**Módulo:** Alertas y notificaciones | **Actor:** Sistema / Instructor / Aprendiz | **Estado:** Implementado

**Feature:** El sistema evalúa la asistencia del aprendiz y genera y resuelve automáticamente alertas por fallas consecutivas (materia) y acumuladas (ficha), con estados No leída, Leída, Atendida y Resuelta automáticamente. El aviso al aprendiz y a los instructores viaja por las notificaciones de UC018.

**Endpoints:**

| Método | Ruta                               | Acceso                           | Descripción                                                                                                                                                                      |
| ------ | ---------------------------------- | -------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| GET    | `/api/alerts`                      | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Bandeja **paginada** (20 por defecto, `sort=generatedAt,desc`) con filtros opcionales `type`, `state`, `gradeId`, `studentId`, `from` y `to`; devuelve `X-Total-Count` y `Link`. |
| GET    | `/api/alerts/students/{studentId}` | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Historial de alertas de un aprendiz (A5), paginado y acotado al alcance de lectura del solicitante.                                                                              |
| GET    | `/api/alerts/{id}`                 | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Detalle de una alerta; una alerta ajena o inexistente responde `404`.                                                                                                            |
| PATCH  | `/api/alerts/{id}/read`            | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Marca la alerta como leída (A2). La transición solo aplica desde `NO_LEIDA`, así que repetirla es idempotente; una alerta ajena responde `404`.                                  |
| PATCH  | `/api/alerts/{id}/attend`          | `ROLE_ADMIN` o `ROLE_INSTRUCTOR` | Marca la alerta como atendida con la observación del seguimiento (A3); una alerta resuelta automáticamente responde `400 error.alertAlreadyResolved`.                            |

**Request — `PATCH /api/alerts/{id}/attend`**

```json
{
  "observation": "Se contactó al aprendiz y se acordó un plan de asistencia."
}
```

| Campo         | Tipo   | Obligatorio | Reglas                              |
| ------------- | ------ | ----------- | ----------------------------------- |
| `observation` | string | Sí          | `@NotNull`, máximo 300 caracteres.  |

**Response:** `200 OK` con el `AlertaDTO` en los endpoints que devuelven una alerta; las listas devuelven un arreglo JSON con la página actual y las cabeceras `X-Total-Count` y `Link`.

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9f00",
  "student": {
    "id": "665f1c2a9e13b7a1f2c8d9ea0",
    "documentNumber": "1029384756",
    "firstName": "Ana",
    "firstLastName": "Gómez"
  },
  "classSection": { "id": "665f1c2a9e13b7a1f2c8d9e90", "subjectName": "Programación orientada a objetos" },
  "grade": { "id": "665f1c2a9e13b7a1f2c8d9e70", "code": "3412345" },
  "trimester": { "id": "665f1c2a9e13b7a1f2c8d9e40", "name": "2026-2" },
  "type": "CONSECUTIVAS",
  "state": "NO_LEIDA",
  "absenceCount": 3,
  "threshold": 3,
  "generatedAt": "2026-09-11T15:04:05Z",
  "resolvedAt": null,
  "observation": null
}
```

El `type` es `CONSECUTIVAS` o `ACUMULADAS`, y el `state` es `NO_LEIDA`, `LEIDA`, `ATENDIDA` o `RESUELTA_AUTOMATICAMENTE`. En una alerta `ACUMULADAS` el campo `classSection` viaja en `null` (la alerta es por ficha) y `grade` viaja siempre. Las referencias anidadas exponen solo su identidad: `student` (`id`, `documentNumber`, `firstName`, `firstLastName`), `classSection` (`id`, `subjectName`), `grade` (`id`, `code`) y `trimester` (`id`, `name`).

**Reglas de generación:**

- **Consecutivas (por materia):** se genera al alcanzar el umbral de fallas consecutivas en las **sesiones programadas** de la materia dentro del trimestre. La racha se mide desde la última sesión hacia atrás sobre los días de la semana del horario de la materia: un `PRESENTE` o un `JUSTIFICADA` **cortan** la racha, una fecha no lectiva es **neutra** y una fecha programada **sin registro se saltea** (ni corta ni suma).
- **Acumuladas (por ficha):** se genera cuando el total de `FALLA` del aprendiz en **todas las materias de la ficha** dentro del trimestre alcanza el umbral; las fallas `JUSTIFICADA` **no cuentan**.
- **Umbrales:** `consecutiveAbsenceAlertThreshold` (default 3) y `accumulatedAbsenceAlertThreshold` (default 5) de la configuración global (UC019); si la fila o el valor faltan, aplican los defaults (E4).
- **Alerta activa única (E1):** solo puede existir una alerta activa por combinación (aprendiz + materia + trimestre + `CONSECUTIVAS`) o (aprendiz + ficha + trimestre + `ACUMULADAS`). Una alerta resuelta es historial y no bloquea que se genere una nueva si el aprendiz vuelve a superar el umbral en el mismo trimestre.
- **Aprendiz desvinculado (E2):** si el aprendiz no está `MATRICULADO` en la ficha de la materia, no se generan alertas nuevas para esa ficha; las existentes se conservan.
- **Reinicio por trimestre:** la ventana de conteo va desde la fecha de inicio del trimestre que contiene la fecha de referencia hasta esa fecha.
- **Resolución automática (A4):** al aprobarse una justificación que deja el conteo por debajo del umbral, la alerta activa correspondiente pasa a `RESUELTA_AUTOMATICAMENTE` con su `resolvedAt`. La resolución solo revisa alertas activas: una aprobación nunca genera alertas nuevas.

**Errores:** `400 error.alertAlreadyResolved` (atención de una alerta resuelta, A3); `400 error.validation` (observación ausente o de más de 300 caracteres); `403` para el aprendiz y cualquier rol fuera de Administrador/Instructor; `404` para una alerta inexistente o fuera del alcance de lectura.

**Notas / lo que se necesita:** verificado con `AlertaResourceIT` 21/21, 262 pruebas unitarias y 143 de integración en verde.

- **Alcance por rol:** la clase exige `ROLE_ADMIN` o `ROLE_INSTRUCTOR`; el **aprendiz recibe `403`** porque su aviso llega por la bandeja de UC018. El Administrador lee y atiende todas las alertas; el instructor solo las `CONSECUTIVAS` de sus materias y las `ACUMULADAS` de sus fichas. Una alerta fuera de ese alcance se resuelve como ausente (`404` en detalle, lectura y atención; excluida de las listas) para no filtrar su existencia.
- **Evaluación:** corre al **guardar la sesión** y al **editar** un registro de asistencia (`AttendanceServiceImpl`), solo cuando el estado realmente cambia (un registro nuevo cuenta como cambio), y al **aprobar** una justificación (`JustificationDetailsServiceImpl`), después de convertir las fallas F→J de la parte. La evaluación se deriva de la escritura: si falla, se registra en el log y la asistencia o la decisión no se pierden.
- **Notificaciones (UC018):** al generar y al resolver una alerta se persisten notificaciones `ALERTA` con `referenceType = "ALERT"` y el id de la alerta: al aprendiz y a los instructores correspondientes —el de la materia en una `CONSECUTIVAS`; todos los de la ficha en una `ACUMULADAS`, deduplicados por usuario—.
- **Reintentos:** el job `@Scheduled` (01:00) reintenta las notificaciones en `REINTENTAR`; solo las `CREDENTIALS` viajan por correo y se reintentan con la lógica del reenvío de credenciales (E7 de UC006), mientras que las in-app (`ALERTA` y `JUSTIFICACION`) no tienen canal externo y no se reintentan. Una entrega lograda pasa a `ENVIADA`; una fallida sigue en `REINTENTAR`.
- **Migración:** la orden Mongock **015** (`MigrateDesertionCounterDrop`) dropea la colección `desertion_counter` (el rollback es no-op: los contadores no se pueden reconstruir). El modelo de alertas crea su propia colección y no necesita migración de datos; el próximo orden libre es **016**.
- **Pendiente del frontend:** la bandeja de alertas y el módulo stock `DesertionCounter` de `src/main/webapp` (menú, rutas, reducers, modelo e i18n) siguen existiendo; su eliminación y la UI de alertas están en [`docs/frontend-debt.md`](./frontend-debt.md).

---

## UC018 — Gestionar notificaciones

**Módulo:** Alertas y notificaciones | **Actor:** Usuario | **Estado:** Implementado

**Feature:** Bandeja in-app de notificaciones del usuario, con estado de entrega interno y estado de lectura independiente. Incluye el cierre del fallback de credenciales de UC006 (E7) con el reenvío manual del Administrador.

**Endpoints:**

| Método | Ruta                                                         | Acceso       | Descripción                                                                                                             |
| ------ | ------------------------------------------------------------ | ------------ | ----------------------------------------------------------------------------------------------------------------------- |
| GET    | `/api/notifications?read=&type=&from=&to=&page=&size=&sort=` | Autenticado  | Bandeja del usuario autenticado, más reciente primero; filtros combinables y paginación (20 por defecto).                |
| PATCH  | `/api/notifications/{id}/read`                               | Autenticado  | Marca una notificación del usuario como leída; `200` con `NotificacionDTO`, `404` si no existe o es de otro usuario.      |
| PATCH  | `/api/notifications/read-all`                                | Autenticado  | Marca todas las no leídas del usuario como leídas (A1); `200` sin cuerpo.                                                |
| PATCH  | `/api/admin/users/resend-credentials`                        | `ROLE_ADMIN` | Reenvía el acceso con un enlace de restablecimiento (E7 de UC006); `200` con `AdminUserDTO`.                            |

**Response — `GET /api/notifications`:** `200 OK`; arreglo JSON con la página actual y las cabeceras `X-Total-Count`, `Link` y **`X-Unread-Count`** (indicador de no leídas del usuario, A3).

```json
{
  "id": "665f1c2a9e13b7a1f2c8d9f10",
  "tipo": "JUSTIFICACION",
  "mensaje": "Tu justificación fue aprobada.",
  "estado": "PENDIENTE",
  "read": false,
  "referenceType": "JUSTIFICATION",
  "referenceId": "665f1c2a9e13b7a1f2c8d9f00",
  "createdDate": "2026-09-11T15:04:05Z"
}
```

**Filtros y orden:** `read` (booleano), `type` (tipo de notificación), `from`/`to` (rango de `createdDate`, ISO-8601) y la paginación estándar; el orden por defecto es `createdDate,desc`. Cada usuario ve solo sus notificaciones: bandeja, conteo, marcar una y marcar todas están acotados al autenticado, y una notificación ajena responde `404` (no revela su existencia). El estado de entrega (`estado`) y el de lectura (`read`) son independientes.

**Entrega real (tipos y destinatarios):** las notificaciones son in-app y nacen `PENDIENTE` y no leídas (`read = false`); el canal correo usa `ENVIADA` y una entrega fallida queda `REINTENTAR` (E3). Cada evento de UC010/UC011 persiste una notificación `JUSTIFICACION` con `referenceType = "JUSTIFICATION"` y el id de la justificación: la creación (`PENDIENTE`) notifica al aprendiz dueño y a cada instructor de las materias afectadas (una por instructor); `ACEPTADA`, `RECHAZADA` y `CANCELADA` notifican solo al aprendiz. El tipo `CREDENTIALS` cubre el fallback de UC006: al fallar el correo de creación se persiste `PENDIENTE`, y el reenvío la cierra como `ENVIADA` o `REINTENTAR`. El tipo `ALERTA` lo produce UC013 (ver arriba): cada generación y cada resolución automática persiste una notificación `ALERTA` con `referenceType = "ALERT"` y el id de la alerta para el aprendiz y los instructores correspondientes.

**Reenvío de credenciales (E7 de UC006):** `PATCH /api/admin/users/resend-credentials` recibe `{ "documentNumber": "..." }`; resuelve el perfil (documento inexistente → `400 error.documentNumberNotFound`), genera un `resetKey` nuevo, marca `mustChangePassword = true` y envía el correo de restablecimiento, de modo que el usuario elige su propia contraseña y nunca viaja una contraseña en texto plano. Si hay una notificación `CREDENTIALS` abierta (`PENDIENTE` o `REINTENTAR`) pasa a `ENVIADA` cuando el correo sale o a `REINTENTAR` cuando falla; sin notificación abierta el reenvío igual manda el correo.

**Migración:** 014 `notificacion-read-state` hace backfill de `read = false` en los documentos previos al estado de lectura.

**Reintentos automáticos:** un job `@Scheduled` (01:00) reintenta las notificaciones en `REINTENTAR`; solo las `CREDENTIALS` viajan por correo y se reintentan con la lógica de reenvío de credenciales (E7 de UC006), mientras que las in-app (`ALERTA`, `JUSTIFICACION`) no tienen canal externo y no se reintentan. Una entrega lograda pasa a `ENVIADA`; una fallida sigue en `REINTENTAR`.

---

## UC023 — Consultar dashboard

**Módulo:** Dashboard | **Actor:** Usuario (según rol) | **Estado:** Implementado

**Feature:** Panel de resumen por rol, calculado al momento de la consulta. Cada rol ve **solo su propio panel**: el Administrador conserva sus KPIs y últimas fichas, el Instructor recibe su carga, justificaciones, alertas y sesiones, y el Aprendiz recibe su asistencia, fallas, justificaciones, matrículas y sesiones. Los indicadores que dependen del trimestre usan el **trimestre activo**; sin uno, viajan vacíos y el mensaje "No hay un trimestre activo" queda en `trimesterMessage` (E2).

**Endpoints:**

| Método | Ruta             | Acceso                                              | Descripción                                           |
| ------ | ---------------- | --------------------------------------------------- | ----------------------------------------------------- |
| GET    | `/api/dashboard` | `ROLE_ADMIN`, `ROLE_INSTRUCTOR` o `ROLE_APPRENTICE` | Devuelve el panel correspondiente al rol autenticado. |

**Response — `GET /api/dashboard`:** `200 OK`. La forma del cuerpo depende del rol autenticado; el método `role()` de la interfaz `DashboardDTO` no se serializa como campo del JSON.

### Panel de Administrador (sin cambios)

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

### Panel de Instructor

```json
{
  "pendingJustifications": 3,
  "activeAlerts": 2,
  "assignedSubjects": 5,
  "assignedGrades": 2,
  "assignedApprentices": 41,
  "todayClasses": [
    {
      "classSectionId": "665f1c2a9e13b7a1f2c8d9e71",
      "subjectName": "Matemáticas",
      "gradeCode": "3412345",
      "date": "2026-09-15",
      "startTime": "07:00:00",
      "endTime": "09:00:00"
    }
  ],
  "upcomingClasses": [],
  "trimesterMessage": null
}
```

| Campo                  | Tipo            | Descripción                                                                                                                                                                                                 |
| ---------------------- | --------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `pendingJustifications`| integer         | Partes de justificación en estado `PENDIENTE` de sus materias, el trabajo pendiente de la bandeja de UC010.                                                                                                   |
| `activeAlerts`         | integer         | Alertas activas (`NO_LEIDA`, `LEIDA` o `ATENDIDA`) de sus materias y fichas, con el mismo alcance de la bandeja de UC013.                                                                                     |
| `assignedSubjects`     | integer         | Materias asignadas al instructor (incluidas las inactivas, que se muestran con su estado en UC017).                                                                                                          |
| `assignedGrades`       | integer         | Fichas distintas donde dicta al menos una materia.                                                                                                                                                            |
| `assignedApprentices`  | integer         | Aprendices en estado `MATRICULADO` en esas fichas, contados una vez por aprendiz aunque esté en varias.                                                                                                       |
| `todayClasses`         | array de sesión | Sesiones de hoy derivadas de los horarios de sus materias en el trimestre activo, descontando las fechas no lectivas de UC009 (A4); acotado a 10.                                                              |
| `upcomingClasses`      | array de sesión | Próximas 5 sesiones después de hoy, con el mismo cálculo.                                                                                                                                                     |
| `trimesterMessage`     | string \| null  | `"No hay un trimestre activo"` cuando no hay trimestre activo; `null` en caso contrario.                                                                                                                      |

La sesión (`todayClasses` y `upcomingClasses`) tiene `classSectionId`, `subjectName`, `gradeCode`, `date`, `startTime` y `endTime`.

### Panel de Aprendiz

```json
{
  "attendance": { "present": 12, "failure": 2, "justified": 1, "percentage": 80.0 },
  "failuresByGrade": [
    { "gradeId": "665f1c2a9e13b7a1f2c8d9e72", "gradeCode": "3412345", "unexcusedFailures": 2, "threshold": 5, "missingToThreshold": 3 }
  ],
  "justifications": {
    "pending": 1,
    "approved": 2,
    "rejected": 1,
    "withinCorrectionWindow": [
      { "id": "665f1c2a9e13b7a1f2c8d9e73", "subjectName": "Matemáticas", "deadline": "2026-09-17", "remainingBusinessDays": 2 }
    ]
  },
  "grades": [
    {
      "gradeId": "665f1c2a9e13b7a1f2c8d9e72",
      "gradeCode": "3412345",
      "programName": "Análisis y Desarrollo de Software",
      "subjects": [{ "id": "665f1c2a9e13b7a1f2c8d9e71", "subjectName": "Matemáticas" }]
    }
  ],
  "upcomingClasses": [],
  "activeAlerts": 1,
  "trimesterMessage": null
}
```

| Campo                  | Tipo            | Descripción                                                                                                                                                                                              |
| ---------------------- | --------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `attendance`           | object \| null  | Totales por estado de las sesiones registradas del aprendiz en el trimestre activo (`present`/`failure`/`justified` = A/F/J) y `percentage` sobre ese total con 2 decimales. `null` sin trimestre activo. |
| `failuresByGrade`      | array           | Fallas no justificadas (`FALLA`) por ficha matriculada en el trimestre activo, el umbral acumulado de UC013 (`threshold`; `GlobalConfiguration`, default 5) y `missingToThreshold` = `max(0, threshold - fallas)`. Vacío sin trimestre activo. |
| `justifications`       | object          | Partes de las justificaciones del aprendiz por estado (`pending`, `approved`, `rejected`); `withinCorrectionWindow` lista las `RECHAZADA` que aún se pueden subsanar (2 días hábiles desde `responseDate`, UC011 A5/E5) con `deadline` y `remainingBusinessDays` (0 = vence hoy). |
| `grades`               | array           | Sus fichas en estado `MATRICULADO` con `gradeId`, `gradeCode`, `programName` y `subjects` (`id` y `subjectName`).                                                                                          |
| `upcomingClasses`      | array de sesión | Próximas 5 sesiones de sus materias en el trimestre activo desde hoy, descontando las fechas no lectivas. Vacío sin trimestre activo.                                                                     |
| `activeAlerts`         | integer         | Alertas activas que lo afectan: cualquier estado distinto de `RESUELTA_AUTOMATICAMENTE`.                                                                                                                   |
| `trimesterMessage`     | string \| null  | Igual que en el panel del Instructor.                                                                                                                                                                       |

**Alcance por rol:** el panel del Instructor se limita a sus materias y fichas (`classSectionRepository.findByInstructorId` y la banda de alertas de UC013); el del Aprendiz, a sus matrículas `MATRICULADO` y a sus propios registros de asistencia, justificaciones y alertas. Un usuario sin perfil resoluble recibe su panel en cero, nunca el de otro rol.

**Errores:** `401` sin sesión; `403` con un rol distinto de los tres admitidos.

**Excepciones:** E1 — sin datos, los indicadores viajan en cero y las listas vacías. E2 — sin trimestre activo, los indicadores de trimestre (`todayClasses` y `upcomingClasses` en el Instructor; `attendance`, `failuresByGrade` y `upcomingClasses` en el Aprendiz) viajan vacíos y `trimesterMessage` expone "No hay un trimestre activo"; los indicadores de trimestre independiente (justificaciones, alertas, matrículas y carga del instructor) se siguen calculando.

**Notas:** la asistencia se calcula sobre las **sesiones registradas** (registros de asistencia dentro del rango del trimestre), como pide la spec. Las **fallas por ficha** usan el umbral **acumulado** de UC013 y agrupan por ficha matriculada. El **plazo de subsanación** replica los 2 días hábiles de UC011 (`BusinessDays.CORRECTION_BUSINESS_DAYS`) contados desde el día siguiente al rechazo. Las **alertas activas** no se restringen al trimestre activo: una alerta sin resolver sigue contando. Las materias asignadas y las fichas del instructor incluyen las inactivas (mismo criterio que UC017). Todo lo que pide la spec de UC023 quedó calculado con el modelo actual: no hubo métricas fuera del contrato.

**Verificación:** `DashboardResourceIT` cubre los paneles de Instructor (2 casos) y Aprendiz (2 casos), además de los casos preexistentes de rol no admitido y sin sesión; `CurrentUserContextTest` y `ClassSessionsTest` cubren los helpers compartidos.
