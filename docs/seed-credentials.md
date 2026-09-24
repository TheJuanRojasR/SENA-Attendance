# Credenciales de la semilla — SENA Attendance

## Cómo aplicar la semilla

**MongoDB en Docker** No hace falta instalar mongosh: se ejecuta
dentro del contenedor. Identifica el nombre con `docker ps` (en tu equipo es `mongodb_v7`) y usa:

```bash
docker exec -i mongodb_v7 mongosh --quiet --file /dev/stdin < docs/mongo_populate.js
```

**En el servidor (imagen desde Docker Hub + tu compose con `mongo` y `app`).** La semilla no va
dentro de la imagen ni hace falta reconstruirla: es solo datos y se aplica una vez contra el
contenedor de Mongo; las redes conservan los datos del volumen.

```bash
ssh usuario@servidor
cd /ruta/a/docker/senaAttendance      # tu carpeta del compose
docker compose ps                     # nombre real del contenedor de Mongo
docker exec <contenedor-app> env | grep -i mongodb   # confirma que la base es senaAttendance
```

Ejecuta la semilla dentro del contenedor de Mongo. Desde tu máquina, sin copiar el archivo:

```bash
ssh usuario@servidor 'docker exec -i <contenedor-mongo> mongosh --quiet --file /dev/stdin' < docs/mongo_populate.js
```

O con el archivo copiado por `scp` al servidor:

```bash
docker exec -i <contenedor-mongo> mongosh --quiet --file /dev/stdin < /tmp/mongo_populate.js
```

## Cómo iniciar sesión

La pantalla de login pide **tipo de documento + número de documento + contraseña** (no el usuario).
El usuario interno se deriva del documento con el formato `<iniciales>_<número>` (por ejemplo,
`cc_79654321`); se incluye solo como referencia.

---

## Administrador

| Nombre completo             | Tipo de documento | Número de documento | Usuario       | Correo                    | Contraseña    |
| --------------------------- | ----------------- | ------------------- | ------------- | ------------------------- | ------------- |
| Claudia Marcela Ríos Peña   | Cédula de Ciudadanía (CC) | 43567890    | `cc_43567890` | claudia.rios@sena.edu.co  | `Adm#Sena2026` |

## Instructores

| Nombre completo                  | Tipo de documento | Número de documento | Usuario         | Correo                       | Contraseña        |
| -------------------------------- | ----------------- | ------------------- | --------------- | ---------------------------- | ----------------- |
| Jorge Andrés Ramírez Ortiz       | Cédula de Ciudadanía (CC) | 79654321    | `cc_79654321`   | jorge.ramirez@sena.edu.co    | `Ins#Sena2026-01` |
| Liliana Marcela Torres Gómez     | Cédula de Ciudadanía (CC) | 52890417    | `cc_52890417`   | liliana.torres@sena.edu.co   | `Ins#Sena2026-02` |
| Andrés Felipe Cárdenas Muñoz     | Cédula de Ciudadanía (CC) | 1023456789  | `cc_1023456789` | andres.cardenas@sena.edu.co  | `Ins#Sena2026-03` |

## Aprendices — Ficha 2829810 (Análisis y Desarrollo de Software · presencial · mañana)

| Nombre completo                    | Tipo de documento | Número de documento | Usuario           | Correo                          | Contraseña        |
| ---------------------------------- | ----------------- | ------------------- | ----------------- | ------------------------------- | ----------------- |
| Juan José Rojas Marín              | Cédula de Ciudadanía (CC) | 1003456789  | `cc_1003456789`   | juan.rojas@soy.sena.edu.co      | `Apr#Sena2026-01` |
| María Camila Restrepo Osorio       | Cédula de Ciudadanía (CC) | 1005678901  | `cc_1005678901`   | maria.restrepo@soy.sena.edu.co  | `Apr#Sena2026-02` |
| Santiago Herrera Quintero          | Tarjeta de Identidad (TI) | 1093456782  | `ti_1093456782`   | santiago.herrera@soy.sena.edu.co | `Apr#Sena2026-03` |
| Valentina Zapata Cardona           | Cédula de Ciudadanía (CC) | 1007890123  | `cc_1007890123`   | valentina.zapata@soy.sena.edu.co | `Apr#Sena2026-04` |
| Nicolás Gómez Betancur             | Cédula de Ciudadanía (CC) | 1002345678  | `cc_1002345678`   | nicolas.gomez@soy.sena.edu.co   | `Apr#Sena2026-05` |
| Isabella Muñoz Arango              | Tarjeta de Identidad (TI) | 1087654321  | `ti_1087654321`   | isabella.munoz@soy.sena.edu.co  | `Apr#Sena2026-06` |
| Daniel Esteban Ospina Loaiza       | Cédula de Ciudadanía (CC) | 1010987654  | `cc_1010987654`   | daniel.ospina@soy.sena.edu.co   | `Apr#Sena2026-07` |
| Laura Sofía Betancur Jaramillo     | Cédula de Ciudadanía (CC) | 1009876543  | `cc_1009876543`   | laura.betancur@soy.sena.edu.co  | `Apr#Sena2026-08` |

## Aprendices — Ficha 2829811 (Gestión de Redes de Datos · presencial · tarde)

| Nombre completo                    | Tipo de documento | Número de documento | Usuario           | Correo                           | Contraseña        |
| ---------------------------------- | ----------------- | ------------------- | ----------------- | -------------------------------- | ----------------- |
| Andrés Santiago Patiño Ríos        | Cédula de Ciudadanía (CC) | 1004567890  | `cc_1004567890`   | andres.patino@soy.sena.edu.co    | `Apr#Sena2026-09` |
| Manuela Cárdenas Vélez             | Cédula de Ciudadanía (CC) | 1006789012  | `cc_1006789012`   | manuela.cardenas@soy.sena.edu.co | `Apr#Sena2026-10` |
| Sebastián Castaño Agudelo          | Cédula de Ciudadanía (CC) | 1001234567  | `cc_1001234567`   | sebastian.castano@soy.sena.edu.co | `Apr#Sena2026-11` |
| Daniela Fernanda Giraldo Serna     | Cédula de Ciudadanía (CC) | 1008765432  | `cc_1008765432`   | daniela.giraldo@soy.sena.edu.co  | `Apr#Sena2026-12` |
| Mateo Álvarez Usuga                | Tarjeta de Identidad (TI) | 1078901234  | `ti_1078901234`   | mateo.alvarez@soy.sena.edu.co    | `Apr#Sena2026-13` |
| Sara Valentina Montoya Duque       | Cédula de Ciudadanía (CC) | 1012345678  | `cc_1012345678`   | sara.montoya@soy.sena.edu.co     | `Apr#Sena2026-14` |
| Tomás Estrada Ramírez              | Cédula de Ciudadanía (CC) | 1003459876  | `cc_1003459876`   | tomas.estrada@soy.sena.edu.co    | `Apr#Sena2026-15` |
| Danna Carolina Villegas Mosquera   | Cédula de Ciudadanía (CC) | 1009871234  | `cc_1009871234`   | danna.villegas@soy.sena.edu.co   | `Apr#Sena2026-16` |

> Danna Carolina queda en estado académico **APLAZADO** (con asistencias solo hasta el 14 de agosto),
> útil para probar los filtros por estado.

---

## Qué incluye la semilla

| Dato                                 | Cantidad | Detalle                                                                 |
| ------------------------------------ | -------- | ----------------------------------------------------------------------- |
| Tipos de documento                   | 4        | CC, TI, CE, PA (activos)                                                |
| Modalidades                          | 3        | Presencial, Virtual, A distancia                                        |
| Jornadas (TimeSlot)                  | 3        | Mañana, Tarde, Noche                                                    |
| Programas                            | 2        | ADSO (228118) y Gestión de Redes de Datos (228105)                      |
| Trimestres                           | 2        | Trimestre 2 - 2026 (cerrado) y Trimestre 3 - 2026 (activo)              |
| Fichas                               | 2        | 2829810 (ADSO) y 2829811 (GRD), ambas activas                           |
| Competencias                         | 6        | 3 por ficha, con horarios en la jornada y 2 excepciones de clase        |
| Asistencias                          | 520      | ~5 semanas (03-ago a 18-sep-2026) con presentes, fallas y justificadas  |
| Justificaciones                      | 4        | Pendiente, aprobada, aprobada fuera de tiempo y rechazada               |
| Alertas                              | 3        | Consecutivas no leída, acumuladas atendida y resuelta automáticamente   |
| Notificaciones                       | 12       | Bandeja de aprendices e instructores                                     |
| Registros de auditoría               | 3        | Correcciones de asistencia con su historial                              |

### Casos listos para demostrar

- **Daniel Esteban Ospina** (`Apr#Sena2026-07`): 3 fallas consecutivas en *Inglés técnico* →
  alerta **NO_LEIDA** para el instructor Jorge.
- **Sebastián Castaño** (`Apr#Sena2026-11`): 5 fallas acumuladas en la ficha GRD →
  alerta **ATENDIDA** con observación para los instructores Andrés y Liliana.
- **María Camila Restrepo** (`Apr#Sena2026-02`): alerta de fallas consecutivas
  **RESUELTA_AUTOMATICAMENTE** al volver a clase, y una justificación **PENDIENTE** de decidir
  en la bandeja de la instructora Liliana.
- **Juan José Rojas** (`Apr#Sena2026-01`): justificación **APROBADA** que convirtió su falla en
  JUSTIFICADA (con registro de auditoría).
- **Santiago Herrera** (`Apr#Sena2026-03`): justificación **RECHAZADA** (sus fallas siguen
  contando).
- **Nicolás Gómez** (`Apr#Sena2026-05`): justificación **APROBADA FUERA DE TIEMPO** con el motivo
  registrado por el instructor.
