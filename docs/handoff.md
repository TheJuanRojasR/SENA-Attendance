# Handoff de sesión — SENA-Attendance (backend)

Documento de traspaso para retomar el trabajo en una ventana nueva. La fuente viva de estado es **Engram** (`project: sena-attendance`, `topic_key: technical-debt/ledger`) y estos archivos: [`docs/api-contracts.md`](./api-contracts.md), [`docs/frontend-debt.md`](./frontend-debt.md), [`docs/backend-debt.md`](./backend-debt.md), [`docs/use-cases.md`](./use-cases.md).

## 1. Reglas del trabajo

- **SOLO backend.** El frontend lo hace otra persona: **NUNCA** tocar `src/main/webapp/**` ni los i18n (solo se listan claves en `frontend-debt.md`).
- Proceso por CADA UC: (1) verificar la UC contra el código real con evidencia `archivo:línea` (delegar a un explorador read-only), (2) listar brechas agrupadas y proponer unidades chicas, (3) implementar **una por una: un commit por unidad**, con tests y doc en el mismo commit, (4) actualizar `api-contracts.md`, `frontend-debt.md`, `backend-debt.md`, (5) al cerrar, marcar la UC `Implementado` en `api-contracts.md`.
- **Convención de API (obligatoria):** en `PUT` y `PATCH` el `id` va **solo en el body** (sin `/{id}` en la ruta); en `GET /{id}` y `DELETE /{id}` el `id` sigue en la ruta. Mantener ambos verbos (`PUT` = reemplazo completo; `PATCH` = parcial). Referencias: `TimeSlotResource`, `ModalityResource`.
- **Comentarios en código:** solo los que expliquen el comportamiento/contrato del código. NO narrar el cambio ni la tarea.
- **Commits:** conventional en inglés, sin `Co-Authored-By` ni atribución de IA. El pre-commit corre husky + prettier (formatea Java/TS; los `docs/*.md` **no** entran en el patrón de lint-staged).
- **Docs:** los artefactos técnicos van en inglés para código; los docs del repo están en español neutro.

## 2. Cómo verificar

```bash
./mvnw verify -Dit.test=<ClaseIT> -Dsurefire.excludes='**/TechnicalStructureTest.java' -Dmodernizer.skip=true
```

- La **suite IT completa está ROJA por fallos PRE-EXISTENTES** (5 clases: `DomainUserDetailsServiceIT`, `TokenAuthenticationIT`, `TokenAuthenticationSecurityMetersIT`, `DashboardResourceIT`, `UserResourceIT`). **NUNCA** usar el `verify` pelado ni `-Dtest=...` (rompe el perfil de failsafe). Verificar **por clase** con `-Dit.test`.
- `UserResourceIT` (pre-existente roja por estado compartido): correr **por método**, p. ej. `-Dit.test='UserResourceIT#updateUser*'`.
- ArchUnit (`TechnicalStructureTest`, ~131 violaciones) y modernizer (~59) también rojos pre-existentes.
- **Migraciones Mongock:** órdenes `001..008` usadas; el próximo libre es `009`.
- La validación del body (`@Valid`) corre **antes** que `@PreAuthorize`; los tests de `403` necesitan un body válido.

## 3. Estado de las UCs

**Implementado** en `api-contracts.md`: UC001, UC002, UC003, UC005, UC006, UC012, UC016, UC019, UC020, UC021, UC022.
**UC004:** sin deuda backend (JWT stateless).

### UC006 — pendiente único (cross-UC)
E7 (reenvío manual de credenciales) → **UC018** (REST de notificaciones). Requiere **regenerar** la contraseña temporal (no se persiste el texto plano). Anotado en `backend-debt.md` (fila UC018).

### UC014 — PARCIAL (retomar acá)
Hecho: `PUT` valida con las mismas reglas que `PATCH`; `DELETE` bloquea con horarios/asistencias (`error.trimesterInUse`, E6); mensajes E1/E3/E4 alineados; escrituras solo `ROLE_ADMIN`. `TrimesterResourceIT` 44/44.

Pendiente:
1. **E2 al crear**: `save` no exige *fecha inicio ≥ mañana* ni *fecha fin ≥ hoy*. Ojo: `TrimesterResourceIT#createTrimesterAdjacentIsAllowed` y `#createTrimesterPastEndIsInactive` afirman lo contrario (hay que reescribirlos), y los tests de solape/orden crean con fechas pasadas (revisar el orden de validaciones para que sigan fallando por la razón correcta).
2. **Tres estados (Futuro/Activo/Cerrado)**: hoy solo hay `status` booleano; el UC pide calcularlos por fecha y filtrar por estado (A2) + posible `GET /active`. La clasificación ya existe on-the-fly (`TrimesterServiceImpl.classifyState`).
3. **Convención**: `PUT /api/trimesters/{id}` todavía usa el id en la ruta.

### Orden de las UCs que faltan (por dependencias)
UC014 (cerrar) → UC015 (Materias) → UC007 (Fichas) → UC008 (Aprendices) → UC017 (Mis fichas y materias) → UC009 (Listas de asistencia) → UC011 (Asistencia Aprendiz) → UC010 (Justificaciones Instructor) → UC018 (Notificaciones) → UC013 (Alertas de inasistencia; al hacerla, **eliminar la entidad `DesertionCounter`**) → UC023 (Dashboard).

## 4. Deuda transversal (resolver al tocar el área)

- `@PreAuthorize` con `ROLE_COORDINATOR`: ya restringidos a `ROLE_ADMIN` DocumentType, TimeSlot, Modality, JustificationType y Program. **Queda `Trimester`** (incluido su `GET /{id}`).
- `/api/user-profiles` (`UserProfileResource`) **sin `@PreAuthorize`**: cualquier autenticado puede CRUD perfiles.
- `registerUser` **no atómico** (usuario + perfil) y **unicidad de nombres solo a nivel app** (sin índice único con collation).
- `JustificationResource` excluye `ROLE_INSTRUCTOR`; `JustificationDetailsResource` sin `@PreAuthorize`; `ClassSectionDTO.instructor` con `@NotNull`; `DashboardDTO` solo admin; `rememberMe` fuera de las UCs; `Notificacion` sin REST (UC018).

## 5. Gotchas aprendidos

- **Un test nuevo que crea una entidad debe registrar la instancia para el `@AfterEach`** (p. ej. `insertedProgram = ...`), si no ensucia el contenedor Mongo compartido y los tests siguientes fallan por duplicados.
- Los catálogos usan **claves de error de negocio** en el body (`message: error.<clave>`), no cabeceras. Los tests de unicidad suelen usar la misma capitalización; conviene cubrir case-insensitivity.
- Al añadir validaciones de formato (p. ej. código numérico), **los datos de los tests existentes pueden volverse inválidos**; revisar los helpers/constantes del IT.
- `existsBy<X>Id` derivado resuelve `@DBRef` (verificado con `UserProfile.documentType`, `Grade.program`, `Justification.justificationType`).
- En `POST` de catálogos con "nace Activo", forzar el estado en el servicio e **ignorar** el valor enviado.

## 6. Próxima ventana — arranque sugerido

1. `mem_search` del `technical-debt/ledger` en Engram + leer `docs/backend-debt.md`.
2. `git log --oneline -15` y `git status` (el árbol debe estar limpio).
3. Cerrar **UC014** (los 3 pendientes) o, si se prefiere avanzar, arrancar **UC015** con el proceso de la sección 1.

## 7. Artefactos

- `docs/use-cases.md` (spec, fuente de verdad de las reglas).
- `docs/api-contracts.md` (contrato + estado por UC).
- `docs/frontend-debt.md` (lo que necesita el front, por UC + claves i18n pendientes).
- `docs/backend-debt.md` (deuda de backend y código a eliminar "en su UC").
