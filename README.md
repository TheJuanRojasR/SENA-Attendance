# SENA Attendance

Sistema web para el registro y seguimiento de la asistencia de los aprendices en las fichas de formación del SENA. Cubre cuentas y acceso, administración de la estructura académica (programas, trimestres, fichas y competencias), registro de asistencia, justificaciones, alertas de inasistencia, notificaciones y dashboard por rol.

Roles del sistema: **Aprendiz**, **Instructor** y **Administrador**.

## Funcionalidades

- **Cuenta y acceso:** autorregistro de aprendices, inicio de sesión con tipo y número de documento, recuperación de contraseña y cambio obligatorio de contraseña.
- **Administración:** usuarios y perfiles, catálogos (jornadas, modalidades, tipos de documento y de justificación, configuración global), programas, trimestres, fichas y competencias con horarios y excepciones.
- **Asistencia:** registro por competencia y fecha de sesión con estados Asistió, Falla y Justificada; auditoría de cambios y bloqueo de edición al cerrar el trimestre.
- **Justificaciones:** radicación por período y materias afectadas, decisión por materia, control de plazos y cupos, y subsanación de rechazos.
- **Alertas y notificaciones:** alertas de inasistencia consecutivas y acumuladas, y bandeja de notificaciones por usuario.
- **Dashboard:** resumen de la información según el rol.

## Tecnologías

| Capa     | Tecnologías                                               |
| -------- | --------------------------------------------------------- |
| Backend  | Java 21, Spring Boot 4, MongoDB, JWT, Maven               |
| Frontend | React 19, TypeScript, Redux Toolkit, Bootstrap 5, Webpack |
| Pruebas  | JUnit, ArchUnit y Cucumber (backend); Vitest (frontend)   |

El proyecto se generó con [JHipster 9.1.0](https://www.jhipster.tech/documentation-archive/v9.1.0) y se adaptó a las reglas de negocio del sistema.

## Requisitos

- Java 21
- Node.js 24.16 o superior
- Docker, para MongoDB en desarrollo

Si no tienes Node.js instalado, el wrapper `./npmw` descarga y usa la versión recomendada por el proyecto.

## Puesta en marcha (desarrollo)

1. Instala las dependencias del frontend:

   ```bash
   npm install
   ```

2. Levanta MongoDB en Docker:

   ```bash
   npm run docker:db:up
   ```

3. Inicia el backend (queda en el puerto 8080):

   ```bash
   npm run backend:start
   ```

4. Inicia el frontend en otra terminal (queda en el puerto 9060 y redirige `/api` al backend):

   ```bash
   npm start
   ```

5. Abre [http://localhost:9060](http://localhost:9060).

Puedes iniciar backend y frontend juntos con `npm run watch`, y detener MongoDB con `npm run docker:db:down`. La lista completa de comandos está en `package.json`.

## Pruebas

```bash
# Backend: unitarias, integración, Cucumber y ArchUnit
./mvnw verify

# Frontend: lint + Vitest con cobertura
npm test
```

## Despliegue

Build de producción como jar:

```bash
./mvnw -Pprod clean verify
java -jar target/sena-attendance-0.0.1-SNAPSHOT.jar
```

Como imagen Docker:

```bash
npm run java:docker:prod
docker compose -f src/main/docker/app.yml up -d
```

Antes de desplegar, revisa el perfil de Spring y las variables de entorno en `src/main/docker/app.yml` (por defecto usa el perfil de desarrollo). En producción configura como mínimo `SPRING_PROFILES_ACTIVE`, `SPRING_MONGODB_URI`, `JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET` y las variables de correo (`SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`).

## Estructura del proyecto

- `src/main/java/com/mycompany/senaattendance/` — backend: dominio, recursos REST, servicios y configuración.
- `src/main/webapp/` — frontend React: aplicación, entidades e internacionalización.
- `src/test/` — pruebas del backend: unitarias, integración, Cucumber y ArchUnit.
- `src/main/docker/` — archivos Docker Compose de MongoDB, la aplicación y servicios de apoyo.
- `docs/` — documentación funcional y técnica del proyecto.
- `sena-attendance.jdl` — modelo de entidades en formato JHipster.

## Documentación

- [Casos de uso](docs/use-cases.md) — actores, reglas de negocio y flujos de los 23 casos de uso.
- [Contratos de API](docs/api-contracts.md) — endpoints HTTP por caso de uso.

## Convenciones de desarrollo

- Commits con [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/) (`feat:`, `fix:`, `docs:`, etc.).
- El hook `pre-commit` ejecuta Prettier sobre los archivos modificados.
- El frontend se analiza con ESLint mediante `npm run lint`.
