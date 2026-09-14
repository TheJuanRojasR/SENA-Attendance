# Trabajo pendiente del backend — SENA Attendance

Este archivo es el **seguimiento vivo del backend**: registra el código muerto o heredado que debe eliminarse y la deuda funcional conocida, a medida que los casos de uso se implementan. Las reglas de negocio y los flujos están en [`docs/use-cases.md`](./use-cases.md); el contrato HTTP y el estado de implementación de cada UC están en [`docs/api-contracts.md`](./api-contracts.md). Aquí no se repite el contrato: se referencia su sección.

- **Propietario:** el desarrollador de backend.
- **Mantenimiento:** se actualiza a medida que se implementan las UCs; cada ítem se resuelve en la UC que le corresponde. Cuando una UC ya está cerrada, su limpieza se ejecuta en una unidad de limpieza posterior.
- **Alcance:** UC001 y UC002 están implementadas y verificadas; el resto está en estado `Parcial` o `No implementado` según `docs/api-contracts.md`. La deuda listada aquí se resuelve al implementar cada UC, no como trabajo suelto.
- **Medición de referencia:** los conteos de la sección [Calidad / tests / gates](#calidad--tests--gates) se midieron sobre `HEAD 56f2852` (2026-09-11) con los comandos indicados.

## Leyenda de estados

| Estado        | Significado                                                    |
| ------------- | -------------------------------------------------------------- |
| `Pendiente`   | No iniciado o sin verificar contra el código y el caso de uso. |
| `En progreso` | Iniciado, todavía sin cerrar el flujo de extremo a extremo.    |
| `Hecho`       | Implementado y verificado contra el código y el caso de uso.   |

---

## Eliminar en su UC

Código heredado de JHipster que ya no se usa y debe borrarse. El flujo de activación por correo no aplica al producto: UC001 **no envía correo de activación** y la cuenta nace `activated = true` (`UserService.registerUser`, `UserService.java:180`). Como UC001 ya está cerrada, la eliminación se ejecutará en una **unidad de limpieza** posterior, referenciada a UC001/UC005 (limpieza de cuentas).

| Elemento                                                                                                                                        | Evidencia (archivo:línea)                                                       | Se elimina en (UC)                         | Estado      |
| ----------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------- | ------------------------------------------ | ----------- |
| Campo `User.activationKey` (y su getter/setter)                                                                                                 | `domain/User.java:61-63`, `:138-139`, `:142-143`                                | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| `UserRepository.findOneByActivationKey`                                                                                                         | `repository/UserRepository.java:17`                                             | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| `UserRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore`                                                         | `repository/UserRepository.java:18`                                             | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| `UserService.activateRegistration`                                                                                                              | `service/UserService.java:85-95`                                                | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| `UserService.removeNotActivatedUsers` (tarea `@Scheduled`)                                                                                      | `service/UserService.java:592-605`                                              | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| `AccountResource.activateAccount` y `GET /api/activate`                                                                                         | `web/rest/AccountResource.java:69-81`                                           | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| Endpoint público `/api/activate` en la cadena de seguridad                                                                                      | `config/SecurityConfiguration.java:66`                                          | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| `MailService.sendActivationEmail`                                                                                                               | `service/MailService.java:141-145`                                              | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| Plantilla `templates/mail/activationEmail.html` (referencia a `user.activationKey`)                                                             | `src/main/resources/templates/mail/activationEmail.html:64,74`                  | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| `DomainUserDetailsService.loadUserByUsername`                                                                                                   | `security/DomainUserDetailsService.java:31-47`                                  | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| `UserNotActivatedException`                                                                                                                     | `security/UserNotActivatedException.java:9-19`                                  | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| Tests de activación: `AccountResourceIT.testActivateAccount` / `testActivateAccountWithWrongKey` y `UserServiceIT.assertThatNotActivatedUsers*` | `web/rest/AccountResourceIT.java:657-679`, `service/UserServiceIT.java:177-206` | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| Test de correo de activación: `MailServiceIT.testSendActivationEmail`                                                                           | `service/MailServiceIT.java:145-158`                                            | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| Tests de login por `UserDetailsService` (incluido el caso no activado)                                                                          | `security/DomainUserDetailsServiceIT.java:87-126`                               | UC001/UC005 (unidad de limpieza)           | `Pendiente` |
| `DesertionCounter` completo (entidad, repositorio, servicio, DTO, mapper, `/api/desertion-counters` y tests)                                    | `web/rest/DesertionCounterResource.java:30-32`; `domain/DesertionCounter.java`  | UC013 (las alertas reemplazan el contador) | `Pendiente` |

Notas de verificación:

- No hay campos de activación en DTOs: `activationKey` no aparece en `service/dto/`, por lo que el único campo a retirar es el de la entidad `User`.
- `registerUser` nunca genera una clave de activación: en producción `setActivationKey` solo se invoca para limpiarla (`UserService.java:90`).
- `DesertionCounter` no tiene referencias fuera de su propio módulo (dominio, repositorio, servicio, DTO, mapper, resource y tests); `docs/use-cases.md` pide descartarlo.

### `DomainUserDetailsService.loadUserByUsername` no está en el camino activo

El login resuelve las credenciales contra `UserProfileRepository` en `AuthenticateController.authorize` (`web/rest/AuthenticateController.java:64-88`) y solo usa la clase anidada `DomainUserDetailsService.UserWithId` para armar los `UserDetails` (`AuthenticateController.java:90`). Ningún componente de producción llama a `loadUserByUsername`: la cadena de seguridad usa `oauth2ResourceServer().jwt()` (`config/SecurityConfiguration.java:85`), sin `formLogin`, `httpBasic` ni `AuthenticationManager`. Sus únicos consumidores actuales son las pruebas (`security/DomainUserDetailsServiceIT.java:87-126`). Por eso se eliminan junto con el flujo de activación.

### `rememberMe` — decisión abierta, no eliminación

`rememberMe` **no es código muerto**: `LoginVM` lo recibe (`web/rest/vm/LoginVM.java:23`), `AuthenticateController.createToken` lo usa para extender la vigencia a 30 días (`AuthenticateController.java:129-138`) y la configuración define `token-validity-in-seconds-for-remember-me: 2592000` (`config/application-dev.yml:83`, `config/application-prod.yml:84`). Los casos de uso no lo mencionan y se decidió **mantenerlo**. Queda como **decisión abierta**: documentarlo en UC002 o retirarlo del contrato cuando el producto defina si expone sesión persistente.

---

## Corregir en su UC

Deuda funcional conocida del backend. Una fila por UC o área, con el pendiente principal y su evidencia. El estado refleja la implementación actual, no el esfuerzo pendiente.

| UC / Área                         | Pendiente principal                                                                                                                                                                                                                                                                                                               | Evidencia / referencia                                                                                                 | Estado      |
| --------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- | ----------- |
| UC003 — Modificar datos           | Resuelto: `GET /api/account/profile`, teléfono de 10 dígitos, rechazo de modificación de documento (E3) y contraseña (E4–E6).                                                                                                                                                                                                    | `web/rest/vm/AccountUpdateVM.java:23-24` (solo `@Size`, sin `@Pattern`); `service/UserService.java:622-693`            | `Hecho`     |
| UC004 — Cerrar sesión             | Sin deuda backend: JWT sin estado, el cierre lo hace el frontend descartando el token. No hay endpoint de logout (propuesta opcional en el contrato).                                                                                                                                                                             | `docs/api-contracts.md` — UC004; `AuthenticateController.java:129-152`                                                 | `Hecho`     |
| UC005 — Recuperar contraseña      | Resuelto: enlace válido 30 min, política completa de contraseña, errores `resetlinkexpired/used/invalid` y limpieza de `mustChangePassword`.                                                                                                                                                                                     | `web/rest/AccountResource.java:148-152`, `:155-161`; `service/UserService.java:97-109`                                 | `Hecho`     |
| UC006 — Gestionar perfiles        | Corrección de datos y cambio de rol sin revalidar todas las guardas; la guarda del instructor único solo mira materias activas (no fichas Pendiente/Activa); `DELETE /api/admin/users/{login}` contradice "los usuarios nunca se eliminan"; la notificación de credenciales no tiene endpoint de reenvío.                         | `service/UserService.java:460-481` (lastAdmin), `:490-525` (lastInstructor); `web/rest/UserResource.java:222-226`      | `Pendiente` |
| UC007 — Gestionar fichas          | Faltan el cálculo automático de estados por fechas, las acciones Aplazar/Reanudar/Cancelar, la validación de código numérico y único, y la guarda de eliminación por aprendices/asistencias. El enum solo tiene tres estados.                                                                                                     | `domain/enumeration/StateGrade.java:7-9`; `docs/api-contracts.md` — UC007                                              | `Pendiente` |
| UC008 — Gestionar aprendices      | El flujo no está modelado: no recibe número de documento ni valida que el aprendiz exista/esté activo, no impide duplicados ni reingresos, no verifica el estado de la ficha y `ROLE_INSTRUCTOR` puede crear/modificar/eliminar vínculos.                                                                                         | `web/rest/ApprenticeResource.java:60`; `docs/api-contracts.md` — UC008                                                 | `Pendiente` |
| UC009 — Listas de asistencia      | CRUD plano: falta el guardado por sesión con todos los aprendices, el default `A`, la sesión incompleta y las validaciones de fecha/trimestre/ficha/excepción no lectiva; `DELETE` sigue permitido.                                                                                                                               | `web/rest/AttendanceResource.java:60`; `domain/enumeration/StateAttendance.java:7-10`; `docs/api-contracts.md` — UC009 | `Pendiente` |
| UC010 — Gestionar justificaciones | No hay endpoint de decisión para el instructor: `JustificationResource` solo admite Admin, Coordinador y Aprendiz, así que el instructor recibe `403`.                                                                                                                                                                            | `web/rest/JustificationResource.java:60-68`; `docs/api-contracts.md` — UC010                                           | `Pendiente` |
| UC011 — Asistencia (Aprendiz)     | Faltan cupo por tipo, marca de plazo, bloqueo de trimestres cerrados, verificación de matrícula, plazo de subsanación y notificaciones; `JustificationDetails` no tiene `@PreAuthorize`.                                                                                                                                          | `web/rest/JustificationDetailsResource.java:60-192`; `domain/enumeration/StateJustification.java:7-9`                  | `Pendiente` |
| UC012 — Programas de aprendizaje  | No se valida que el código sea numérico (E5) y `DELETE` no verifica si el programa tiene fichas (E8). La unicidad de nombre, iniciales y código sí está implementada.                                                                                                                                                             | `repository/ProgramRepository.java:56-102`; `docs/api-contracts.md` — UC012                                            | `Pendiente` |
| UC014 — Trimestres académicos     | `POST` no valida que la fecha inicio sea desde mañana ni que la fecha fin no esté vencida; `PUT` no valida nada; `DELETE` no verifica horarios ni asistencias asociadas.                                                                                                                                                          | `docs/api-contracts.md` — UC014; `web/rest/TrimesterResource.java:61-201`                                              | `Pendiente` |
| UC015 — Gestionar materias        | Faltan nombre único por ficha, horario dentro de la jornada, no solapamiento, sesión sin cruzar medianoche, bloqueo de trimestre cerrado, restricción a fichas Pendiente/Activa y borrado en cascada. El instructor es obligatorio en la API aunque el UC lo quiere opcional.                                                     | `service/dto/ClassSectionDTO.java:22-23`; `docs/api-contracts.md` — UC015                                              | `Pendiente` |
| UC016 — Tipos de justificación    | Sin validación de nombre duplicado (E1), límite mayor a 0 (E2), bloqueo de eliminación en uso (E3) ni endpoint de solo activos para el formulario del aprendiz.                                                                                                                                                                   | `docs/api-contracts.md` — UC016                                                                                        | `Pendiente` |
| UC017 — Mis fichas y materias     | No hay endpoint dedicado de "mis fichas" (el frontend las deriva de `/api/class-sections/mine`), ni búsqueda por número de ficha, ni paginación.                                                                                                                                                                                  | `docs/api-contracts.md` — UC017                                                                                        | `Pendiente` |
| UC019 — Configuración global      | Resuelto: los 4 parámetros con defaults, validación 1–30, lectura solo Admin y singleton con id fijo `global-configuration`.                                                                                                                                                                                                     | `domain/GlobalConfiguration.java`; `service/dto/GlobalConfigurationDTO.java`                                           | `Hecho`     |
| UC020 — Gestionar jornadas        | Resuelto: nombre único, horas distintas, borrado bloqueado en uso, nace activa, PUT/PATCH con id en body, listado paginado y escrituras solo Admin.                                                                                                                                                                              | `docs/api-contracts.md` — UC020; `web/rest/TimeSlotResource.java:55-169`                                               | `Hecho`     |
| UC021 — Gestionar modalidades     | Resuelto: nombre único, nace activa, borrado bloqueado en uso, PUT/PATCH con id en body, listado paginado y escrituras solo Admin.                                                                                                                                                                                               | `docs/api-contracts.md` — UC021; `web/rest/ModalityResource.java:55-169`                                               | `Hecho`     |
| UC022 — Tipos de documento        | Sin validar nombre ni iniciales duplicados (E1/E2), sin bloquear el cambio de iniciales de un tipo en uso (E3) ni la eliminación por uso (E4). El estado `isActive` ya existe. Además, `DocumentTypeResource` aún usa `PUT /{id}` y `PATCH /{id}`: debe alinearse a la convención del proyecto (id solo en el body en PUT/PATCH). | `service/dto/DocumentTypeDTO.java:23`; `web/rest/DocumentTypeResource.java:54-177`; `docs/api-contracts.md` — UC022    | `Pendiente` |
| UC023 — Consultar dashboard       | Solo el panel de Administrador tiene datos reales; Instructor y Aprendiz reciben el payload del Administrador como solución temporal.                                                                                                                                                                                             | `service/dto/dashboard/DashboardDTO.java:6`; `docs/api-contracts.md` — UC023                                           | `Pendiente` |
| UC013 — Alertas de inasistencia   | No implementada: falta la entidad `Alerta`, la evaluación al guardar asistencia o aprobar justificaciones y las notificaciones asociadas.                                                                                                                                                                                         | `docs/api-contracts.md` — UC013; `domain/enumeration/NotificacionTipo.java:7`                                          | `Pendiente` |
| UC018 — Gestionar notificaciones  | No implementada: la entidad `Notificacion` existe pero sin controlador REST, sin estado de lectura y sin referencia al objeto de origen.                                                                                                                                                                                          | `domain/Notificacion.java:17-37`; `docs/api-contracts.md` — UC018                                                      | `Pendiente` |

### Deuda transversal

Afecta a varias UCs a la vez; se resuelve al tocar el área correspondiente.

| Tema                                                | Pendiente principal                                                                                                                        | Evidencia (archivo:línea)                                                                                                   | Estado      |
| --------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------- | ----------- |
| `@PreAuthorize` con `ROLE_COORDINATOR`              | Varios resources siguen aceptando un rol que los casos de uso ya no contemplan; debe quedar solo `ROLE_ADMIN` (u operativo donde aplique). | `web/rest/DocumentTypeResource.java:55`; `ProgramResource.java:63`; `TrimesterResource.java:61`                              | `Pendiente` |
| `JustificationResource` excluye a `ROLE_INSTRUCTOR` | El instructor no puede decidir sus justificaciones (`403`).                                                                                | `web/rest/JustificationResource.java:60-68`                                                                                 | `Pendiente` |
| `JustificationDetailsResource` sin `@PreAuthorize`  | Cualquier usuario autenticado puede escribir/borrar partes de justificación.                                                               | `web/rest/JustificationDetailsResource.java:60-192`                                                                         | `Pendiente` |
| `ClassSectionDTO.instructor` con `@NotNull`         | La API exige instructor al crear la materia, aunque UC015 lo permite opcional.                                                             | `service/dto/ClassSectionDTO.java:22-23`                                                                                    | `Pendiente` |
| `GlobalConfiguration` con 2 de 4 parámetros         | Resuelto: los cuatro parámetros existen, incluidos los dos umbrales de alerta de UC013 (defaults 3 y 5, backfill para filas legacy).       | `domain/GlobalConfiguration.java`                                                                                           | `Hecho`     |
| `Notificacion` sin REST                             | La entidad existe y se usa internamente al fallar el correo de credenciales, pero no hay bandeja ni reenvío (UC018/E7 de UC006).           | `domain/Notificacion.java:17-37`; `docs/api-contracts.md` — UC018                                                           | `Pendiente` |
| `DashboardDTO` solo Administrador                   | El contrato sellado solo permite `AdminDashboardDTO`; faltan los paneles de Instructor y Aprendiz.                                         | `service/dto/dashboard/DashboardDTO.java:6`                                                                                 | `Pendiente` |
| `rememberMe` fuera de las UCs                       | Se mantiene por decisión de producto; no está documentado en ningún caso de uso.                                                           | `web/rest/vm/LoginVM.java:23`; `AuthenticateController.java:129-138`                                                        | `Pendiente` |

---

## Calidad / tests / gates

Puertas de calidad que **hoy están en rojo de forma pre-existente**. Se excluyen para verificar cambios nuevos y no bloquean las UCs. Las cifras de referencia iniciales quedaron desactualizadas porque crecen con el código nuevo; abajo se registra la medición actual.

| Puerta                              | Referencia inicial | Medición en HEAD `56f2852`        |
| ----------------------------------- | ------------------ | --------------------------------- |
| `TechnicalStructureTest` (ArchUnit) | 121+ violaciones   | **131** violaciones `Service→Web` |
| `modernizer`                        | 54 violaciones     | **59** violaciones                |
| Suite IT completa                   | 38 errores         | **39** errores                    |

### `TechnicalStructureTest` (ArchUnit)

La regla de capas falla por 131 dependencias de `service` hacia `web` (por ejemplo, constructores de `BadRequestAlertException`). Las 131 pertenecen a la violación `Service→Web`. El resto de las pruebas unitarias pasa (164 unitarias, solo esta falla).

### `modernizer`

59 violaciones, en su mayoría `Prefer java.util.Optional.orElseThrow()` y `Prefer java.util.List.of()` en los `*ServiceImpl`; el plugin escanea `src/main` y `src/test`.

### Suite de integración completa en rojo

39 errores distribuidos en 5 clases:

| Clase IT                              | Resultado     | Causa verificada                                                                                                                                                 |
| ------------------------------------- | ------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `DomainUserDetailsServiceIT`          | 6/6 errores   | Correos `@localhost` que violan `EMAIL_REGEX` (`config/Constants.java:12`).                                                                                      |
| `TokenAuthenticationIT`               | 4/4 errores   | Carga de contexto: `No qualifying bean of type 'UserProfileRepository'` (slicing sin repositorio).                                                               |
| `TokenAuthenticationSecurityMetersIT` | 5/5 errores   | Carga de contexto: mismo bean `UserProfileRepository` ausente.                                                                                                   |
| `DashboardResourceIT`                 | 1/3 errores   | `testGetDashboardAsAdmin`: el cuerpo/estado queda nulo.                                                                                                          |
| `UserResourceIT`                      | 23/29 errores | Estado compartido/orden: `seededDocumentTypeId()` hace `findAll().iterator().next()` y falla con `NoSuchElementException` cuando no hay `DocumentType` sembrado. |

Estas fallas existían antes de las UCs en curso; no son regresiones de UC001/UC002. Se recomienda arreglarlas como unidad independiente (sembrar el `UserProfileRepository` en los recortes de contexto, usar correos válidos y aislar el estado entre ITs).

### Comandos de verificación

Para verificar un cambio sin las puertas rojas pre-existentes (ArchUnit y modernizer):

```bash
./mvnw -ntp verify -Dmodernizer.skip=true \
  -Dtest='!TechnicalStructureTest' -Dsurefire.failIfNoSpecifiedTests=false
```

Para ejecutar la prueba de integración de una sola UC, evitando las clases rojas por estado compartido:

```bash
./mvnw -ntp verify -Dmodernizer.skip=true \
  -Dtest='!TechnicalStructureTest' -Dsurefire.failIfNoSpecifiedTests=false \
  -Dit.test=<ClaseIT>
```
