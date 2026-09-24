// ============================================================================
// SENA Attendance — Semilla de datos realista (desarrollo / demostración)
// ============================================================================
// Qué hace:
//   Reemplaza los datos de prueba por un mundo coherente y realista: catálogos
//   completos, 20 usuarios con nombre y documento reales (ficticios), 2 fichas,
//   6 competencias con horarios, ~5 semanas de asistencias, justificaciones en
//   varios estados, alertas y notificaciones consistentes con el motor del sistema.
//
// Cómo ejecutar:
//   1) MongoDB Compass -> conexión -> pestaña "mongosh" (parte inferior) -> pegar todo.
//   2) O por consola:
//      mongosh --file docs/mongo_populate.js "mongodb://localhost:27017/senaAttendance"
//
// ADVERTENCIA:
//   Borra las colecciones de negocio y los usuarios (user, user_profile, ...).
//   Las credenciales resultantes están documentadas en docs/seed-credentials.md.
//   NO toca authority, mongockChangeLog ni mongockLock.
//
// Nota sobre fechas (IMPORTANTE):
//   El backend corre con la zona America/Bogota (UTC-5, sin horario de verano) y
//   Spring Data guarda java.time.LocalDate como un Date a la medianoche de Bogotá
//   (05:00Z del mismo día) y java.time.LocalTime como un Date con esa hora local.
//   Este script respeta esa convención: si se guardaran a medianoche UTC, la app
//   leería el día anterior y las consultas por igualdad de fecha fallarían.
// ============================================================================

use('senaAttendance');

// ----------------------------------------------------------------------------
// Utilidades
// ----------------------------------------------------------------------------

// java.time.LocalDate -> Date a la medianoche de Bogotá (UTC-5)
function bogoDate(iso) {
  return new Date(iso + 'T05:00:00.000Z');
}

// java.time.LocalTime -> Date con esa hora local de Bogotá (la fecha es indiferente)
function localTime(hh, mm) {
  return new Date(Date.UTC(2026, 8, 21, hh + 5, mm, 0, 0));
}

function instant(iso) {
  return new Date(iso);
}

// Campos de auditoría comunes a todas las entidades (AbstractAuditingEntity)
const SYSTEM = 'system';
function auditing(date) {
  return {
    created_by: SYSTEM,
    created_date: date,
    last_modified_by: SYSTEM,
    last_modified_date: date,
  };
}

// Fechas fijas para los datos de catálogo
const CATALOGS_DATE = instant('2026-07-01T12:00:00.000Z');
const USERS_DATE = instant('2026-07-15T12:00:00.000Z');
const GRADES_DATE = instant('2026-07-16T12:00:00.000Z');
const SECTIONS_DATE = instant('2026-07-20T12:00:00.000Z');

// ----------------------------------------------------------------------------
// 1. Limpieza de colecciones de negocio
//    Se usa deleteMany (no drop) para conservar los índices únicos creados por
//    las migraciones (uk_*_name_ci). Las colecciones authority, mongockChangeLog
//    y mongockLock NO se tocan.
// ----------------------------------------------------------------------------
[
  'user',
  'user_profile',
  'apprentice',
  'grade',
  'class_section',
  'class_schedule',
  'class_exception',
  'attendance',
  'audit_log',
  'justification',
  'justification_details',
  'justification_type',
  'alerta',
  'notificacion',
  'program',
  'modality',
  'time_slot',
  'trimester',
  'document_type',
  'global_configuration',
].forEach(name => db.getCollection(name).deleteMany({}));

// ----------------------------------------------------------------------------
// 2. Autoridades (se aseguran, nunca se borran)
// ----------------------------------------------------------------------------
['ROLE_ADMIN', 'ROLE_USER', 'ROLE_INSTRUCTOR', 'ROLE_APPRENTICE'].forEach(name => {
  db.getCollection('authority').replaceOne(
    { _id: name },
    { _id: name, _class: 'com.mycompany.senaattendance.domain.Authority' },
    { upsert: true },
  );
});

// ----------------------------------------------------------------------------
// 3. Identificadores de todas las entidades
// ----------------------------------------------------------------------------
// Catálogos
const docTypeCcId = new ObjectId();
const docTypeTiId = new ObjectId();
const docTypeCeId = new ObjectId();
const docTypePaId = new ObjectId();
const modPresencialId = new ObjectId();
const modVirtualId = new ObjectId();
const modDistanciaId = new ObjectId();
const tsMananaId = new ObjectId();
const tsTardeId = new ObjectId();
const tsNocheId = new ObjectId();
const progAdsoId = new ObjectId();
const progGrdId = new ObjectId();
const jtIncapacidadId = new ObjectId();
const jtCitaMedicaId = new ObjectId();
const jtCalamidadId = new ObjectId();
const jtDiligenciaId = new ObjectId();

// Trimestres
const trimCerradoId = new ObjectId();
const trimActivoId = new ObjectId();

// Fichas
const gradeAdsoId = new ObjectId();
const gradeGrdId = new ObjectId();

// Competencias
const csConstruccionId = new ObjectId();
const csBasesDatosId = new ObjectId();
const csInglesId = new ObjectId();
const csRedesId = new ObjectId();
const csSeguridadId = new ObjectId();
const csMantenimientoId = new ObjectId();

// Justificaciones
const justificacionIncapacidadMariaId = new ObjectId();
const justificacionCitaId = new ObjectId();
const justificacionCalamidadId = new ObjectId();
const justificacionIncapacidadNicolasId = new ObjectId();
const parteMariaId = new ObjectId();
const parteCitaId = new ObjectId();
const parteCalamidadId = new ObjectId();
const parteNicolasId = new ObjectId();

// Alertas
const alertaDanielId = new ObjectId();
const alertaSebastianId = new ObjectId();
const alertaMariaId = new ObjectId();

// Auditoría
const auditJuanId = new ObjectId();
const auditSebastianId = new ObjectId();
const auditValentinaId = new ObjectId();

// ----------------------------------------------------------------------------
// 4. Catálogos base
// ----------------------------------------------------------------------------
db.getCollection('document_type').insertMany([
  { _id: docTypeCcId, name: 'Cédula de Ciudadanía', initials: 'CC', is_active: true, ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.DocumentType' },
  { _id: docTypeTiId, name: 'Tarjeta de Identidad', initials: 'TI', is_active: true, ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.DocumentType' },
  { _id: docTypeCeId, name: 'Cédula de Extranjería', initials: 'CE', is_active: true, ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.DocumentType' },
  { _id: docTypePaId, name: 'Pasaporte', initials: 'PA', is_active: true, ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.DocumentType' },
]);

db.getCollection('modality').insertMany([
  { _id: modPresencialId, name: 'Presencial', is_active: true, ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.Modality' },
  { _id: modVirtualId, name: 'Virtual', is_active: true, ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.Modality' },
  { _id: modDistanciaId, name: 'A distancia', is_active: true, ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.Modality' },
]);

db.getCollection('time_slot').insertMany([
  { _id: tsMananaId, name: 'Mañana', is_active: true, start_time: localTime(6, 0), end_time: localTime(12, 0), ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.TimeSlot' },
  { _id: tsTardeId, name: 'Tarde', is_active: true, start_time: localTime(12, 0), end_time: localTime(18, 0), ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.TimeSlot' },
  { _id: tsNocheId, name: 'Noche', is_active: true, start_time: localTime(18, 0), end_time: localTime(22, 0), ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.TimeSlot' },
]);

db.getCollection('program').insertMany([
  { _id: progAdsoId, name: 'Análisis y Desarrollo de Software', initials: 'ADSO', code: '228118', trimesters: 6, status: true, ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.Program' },
  { _id: progGrdId, name: 'Gestión de Redes de Datos', initials: 'GRD', code: '228105', trimesters: 6, status: true, ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.Program' },
]);

db.getCollection('justification_type').insertMany([
  { _id: jtIncapacidadId, name: 'Incapacidad médica', limit_per_trimester: 2, status: 'ACTIVO', ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.JustificationType' },
  { _id: jtCitaMedicaId, name: 'Cita médica', limit_per_trimester: 2, status: 'ACTIVO', ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.JustificationType' },
  { _id: jtCalamidadId, name: 'Calamidad doméstica', limit_per_trimester: 2, status: 'ACTIVO', ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.JustificationType' },
  { _id: jtDiligenciaId, name: 'Diligencia judicial', limit_per_trimester: 1, status: 'ACTIVO', ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.JustificationType' },
]);

db.getCollection('global_configuration').insertOne({
  _id: 'global-configuration',
  student_justification_days: 5,
  instructor_response_days: 2,
  consecutive_absence_alert_threshold: 3,
  accumulated_absence_alert_threshold: 5,
  ...auditing(CATALOGS_DATE),
  _class: 'com.mycompany.senaattendance.domain.GlobalConfiguration',
});

// ----------------------------------------------------------------------------
// 5. Trimestres
// ----------------------------------------------------------------------------
db.getCollection('trimester').insertMany([
  { _id: trimCerradoId, name: 'Trimestre 2 - 2026', start_date: bogoDate('2026-04-06'), end_date: bogoDate('2026-07-10'), status: 'CERRADO', ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.Trimester' },
  { _id: trimActivoId, name: 'Trimestre 3 - 2026', start_date: bogoDate('2026-07-13'), end_date: bogoDate('2026-11-27'), status: 'ACTIVO', ...auditing(CATALOGS_DATE), _class: 'com.mycompany.senaattendance.domain.Trimester' },
]);

// ----------------------------------------------------------------------------
// 6. Usuarios y perfiles
//    El login se deriva del tipo y número de documento: <iniciales>_<numero>.
//    Las contraseñas en claro están documentadas en docs/seed-credentials.md.
// ----------------------------------------------------------------------------
const usersSeed = [
  // Administración
  { login: 'cc_43567890', role: 'ADMIN', firstName: 'Claudia', middleName: 'Marcela', firstLastName: 'Ríos', secondLastName: 'Peña', documentType: 'CC', documentNumber: '43567890', phone: '3104567890', email: 'claudia.rios@sena.edu.co', passwordHash: '$2a$10$newiz0qHenpZwmkEpRpgOe/aS087r1K2f1PvwggMTylLxQq9nPwuG' },
  // Instructores
  { login: 'cc_79654321', role: 'INSTRUCTOR', firstName: 'Jorge', middleName: 'Andrés', firstLastName: 'Ramírez', secondLastName: 'Ortiz', documentType: 'CC', documentNumber: '79654321', phone: '3204567801', email: 'jorge.ramirez@sena.edu.co', passwordHash: '$2a$10$UTPBUJHD/Pq5xkxZNoAGvusQRjUUCnHeKkM0oJPx/OtiFdw7WPFhi' },
  { login: 'cc_52890417', role: 'INSTRUCTOR', firstName: 'Liliana', middleName: 'Marcela', firstLastName: 'Torres', secondLastName: 'Gómez', documentType: 'CC', documentNumber: '52890417', phone: '3156789012', email: 'liliana.torres@sena.edu.co', passwordHash: '$2a$10$v4w/izdjWrHkZ6yFsnf7O.zrfuLrno/hZPxB283FBgDfQUTXSau6.' },
  { login: 'cc_1023456789', role: 'INSTRUCTOR', firstName: 'Andrés', middleName: 'Felipe', firstLastName: 'Cárdenas', secondLastName: 'Muñoz', documentType: 'CC', documentNumber: '1023456789', phone: '3189012345', email: 'andres.cardenas@sena.edu.co', passwordHash: '$2a$10$hozbTZHD55TZW1XxcOWysuOKizYdSeUUbp22dzD5qgN0OYYRZ0Rl.' },
  // Aprendices ficha ADSO 2829810
  { login: 'cc_1003456789', role: 'APPRENTICE', ficha: 'ADSO', firstName: 'Juan', middleName: 'José', firstLastName: 'Rojas', secondLastName: 'Marín', documentType: 'CC', documentNumber: '1003456789', phone: '3123456789', email: 'juan.rojas@soy.sena.edu.co', passwordHash: '$2a$10$x3Vr0x7FQYtMTr15p3C2H.X6Ijj7goMhBxZhZwLT.IT9Iohw3KyXu' },
  { login: 'cc_1005678901', role: 'APPRENTICE', ficha: 'ADSO', firstName: 'María', middleName: 'Camila', firstLastName: 'Restrepo', secondLastName: 'Osorio', documentType: 'CC', documentNumber: '1005678901', phone: '3134567890', email: 'maria.restrepo@soy.sena.edu.co', passwordHash: '$2a$10$Dk5GR9FaQCN0VGiK1MMORubk3d6OA5xmtK72Aai0gVPpNbrMZQt0q' },
  { login: 'ti_1093456782', role: 'APPRENTICE', ficha: 'ADSO', firstName: 'Santiago', firstLastName: 'Herrera', secondLastName: 'Quintero', documentType: 'TI', documentNumber: '1093456782', phone: '3145678901', email: 'santiago.herrera@soy.sena.edu.co', passwordHash: '$2a$10$EiCs1cK9kjqH4aK2Urb6uu.d9wQzEvBSKAcLc2oDAd7oVDxirAL.a' },
  { login: 'cc_1007890123', role: 'APPRENTICE', ficha: 'ADSO', firstName: 'Valentina', firstLastName: 'Zapata', secondLastName: 'Cardona', documentType: 'CC', documentNumber: '1007890123', phone: '3156789023', email: 'valentina.zapata@soy.sena.edu.co', passwordHash: '$2a$10$DcDp5RQTwhTBr8kyaGQzfeoXIt9sGaXGFFB0W9qYu0d4gYl1S/UnG' },
  { login: 'cc_1002345678', role: 'APPRENTICE', ficha: 'ADSO', firstName: 'Nicolás', firstLastName: 'Gómez', secondLastName: 'Betancur', documentType: 'CC', documentNumber: '1002345678', phone: '3167890123', email: 'nicolas.gomez@soy.sena.edu.co', passwordHash: '$2a$10$5NFNkdqTjTSly7gbawLLn.JDKWksL9iwhm1SjmNw.B2SHcC1AgGG2' },
  { login: 'ti_1087654321', role: 'APPRENTICE', ficha: 'ADSO', firstName: 'Isabella', firstLastName: 'Muñoz', secondLastName: 'Arango', documentType: 'TI', documentNumber: '1087654321', phone: '3178901234', email: 'isabella.munoz@soy.sena.edu.co', passwordHash: '$2a$10$7D9y5/EpOPrMKwtky0up3elMOHHyfdnCYgGGlJ.OJ2Yi1hTsEKTOS' },
  { login: 'cc_1010987654', role: 'APPRENTICE', ficha: 'ADSO', firstName: 'Daniel', middleName: 'Esteban', firstLastName: 'Ospina', secondLastName: 'Loaiza', documentType: 'CC', documentNumber: '1010987654', phone: '3189012345', email: 'daniel.ospina@soy.sena.edu.co', passwordHash: '$2a$10$Th34.EU6p0bUUkguy5HeCOTDSZaU4mMIJ2cKPAlbWVfGZzzi8N92m' },
  { login: 'cc_1009876543', role: 'APPRENTICE', ficha: 'ADSO', firstName: 'Laura', middleName: 'Sofía', firstLastName: 'Betancur', secondLastName: 'Jaramillo', documentType: 'CC', documentNumber: '1009876543', phone: '3190123456', email: 'laura.betancur@soy.sena.edu.co', passwordHash: '$2a$10$mNpHPy8xQdsE58bX4jmoh.3GmKMeqRGptrzXdnJ6dYVFCjP9YQqEO' },
  // Aprendices ficha GRD 2829811
  { login: 'cc_1004567890', role: 'APPRENTICE', ficha: 'GRD', firstName: 'Andrés', middleName: 'Santiago', firstLastName: 'Patiño', secondLastName: 'Ríos', documentType: 'CC', documentNumber: '1004567890', phone: '3201234567', email: 'andres.patino@soy.sena.edu.co', passwordHash: '$2a$10$zvOXDTrhhiUj14CDqGtN9.M3RZdofwVtp5wh9YDyojvMCDnlm9bhO' },
  { login: 'cc_1006789012', role: 'APPRENTICE', ficha: 'GRD', firstName: 'Manuela', firstLastName: 'Cárdenas', secondLastName: 'Vélez', documentType: 'CC', documentNumber: '1006789012', phone: '3212345678', email: 'manuela.cardenas@soy.sena.edu.co', passwordHash: '$2a$10$J2UAbE6kAYxFG6SCe0sNWujKnWP0eku5vCCCdf4LwcIGRt7jTzRS2' },
  { login: 'cc_1001234567', role: 'APPRENTICE', ficha: 'GRD', firstName: 'Sebastián', firstLastName: 'Castaño', secondLastName: 'Agudelo', documentType: 'CC', documentNumber: '1001234567', phone: '3123456780', email: 'sebastian.castano@soy.sena.edu.co', passwordHash: '$2a$10$gn5Y1wtv.bYGQF6/iTLgiuwJWhQXEHlRsRiAzu.gBzHKbG2Yr.ATq' },
  { login: 'cc_1008765432', role: 'APPRENTICE', ficha: 'GRD', firstName: 'Daniela', middleName: 'Fernanda', firstLastName: 'Giraldo', secondLastName: 'Serna', documentType: 'CC', documentNumber: '1008765432', phone: '3134567891', email: 'daniela.giraldo@soy.sena.edu.co', passwordHash: '$2a$10$tlQC/CMELSTVx.KObH7ew.Ug6YN45a3ZyN7Fp7T5uy9jT0WEpD2OG' },
  { login: 'ti_1078901234', role: 'APPRENTICE', ficha: 'GRD', firstName: 'Mateo', firstLastName: 'Álvarez', secondLastName: 'Usuga', documentType: 'TI', documentNumber: '1078901234', phone: '3145678902', email: 'mateo.alvarez@soy.sena.edu.co', passwordHash: '$2a$10$eWcMcgZmuo.AVpoMh5U1We5ttjHJJCLFCUJjMT3n4ChbCabuM0BWG' },
  { login: 'cc_1012345678', role: 'APPRENTICE', ficha: 'GRD', firstName: 'Sara', middleName: 'Valentina', firstLastName: 'Montoya', secondLastName: 'Duque', documentType: 'CC', documentNumber: '1012345678', phone: '3156789013', email: 'sara.montoya@soy.sena.edu.co', passwordHash: '$2a$10$rfyLIW0cUColszdQdwpwq.2gN1FwB3.WAXLgkDqfLw5X3PcoDlFNC' },
  { login: 'cc_1003459876', role: 'APPRENTICE', ficha: 'GRD', firstName: 'Tomás', firstLastName: 'Estrada', secondLastName: 'Ramírez', documentType: 'CC', documentNumber: '1003459876', phone: '3167890124', email: 'tomas.estrada@soy.sena.edu.co', passwordHash: '$2a$10$b.4V9It0TfJsho8XuN/Et.LlWG0ofNnwFx7DJFANztpHG3.cQrZUW' },
  { login: 'cc_1009871234', role: 'APPRENTICE', ficha: 'GRD', firstName: 'Danna', middleName: 'Carolina', firstLastName: 'Villegas', secondLastName: 'Mosquera', documentType: 'CC', documentNumber: '1009871234', phone: '3178901235', email: 'danna.villegas@soy.sena.edu.co', passwordHash: '$2a$10$YmdQWiXB8mnPklKutATHOet5bLtWWFCbUrDtWoxrmgf.XDfZ5kSk.' },
];

const authoritiesByRole = {
  ADMIN: ['ROLE_ADMIN', 'ROLE_USER'],
  INSTRUCTOR: ['ROLE_INSTRUCTOR', 'ROLE_USER'],
  APPRENTICE: ['ROLE_APPRENTICE', 'ROLE_USER'],
};

const documentTypeIdByInitials = {
  CC: docTypeCcId,
  TI: docTypeTiId,
  CE: docTypeCeId,
  PA: docTypePaId,
};

const userIdByLogin = {};
const profileIdByLogin = {};
const userDocs = [];
const profileDocs = [];

usersSeed.forEach(user => {
  const userId = new ObjectId();
  userIdByLogin[user.login] = userId;
  userDocs.push({
    _id: userId,
    login: user.login,
    password: user.passwordHash,
    email: user.email,
    activated: true,
    mustChangePassword: false,
    lang_key: 'es',
    authorities: authoritiesByRole[user.role].map(authority => ({ _id: authority })),
    ...auditing(USERS_DATE),
    _class: 'com.mycompany.senaattendance.domain.User',
  });

  const profileId = new ObjectId();
  profileIdByLogin[user.login] = profileId;
  const profile = {
    _id: profileId,
    first_name: user.firstName,
    first_last_name: user.firstLastName,
    document_number: user.documentNumber,
    phone_number: user.phone,
    user: DBRef('user', userId),
    documentType: DBRef('document_type', documentTypeIdByInitials[user.documentType]),
    ...auditing(USERS_DATE),
    _class: 'com.mycompany.senaattendance.domain.UserProfile',
  };
  if (user.middleName) {
    profile.middle_name = user.middleName;
  }
  if (user.secondLastName) {
    profile.second_last_name = user.secondLastName;
  }
  profileDocs.push(profile);
});

db.getCollection('user').insertMany(userDocs);
db.getCollection('user_profile').insertMany(profileDocs);

// ----------------------------------------------------------------------------
// 7. Fichas (Grades)
// ----------------------------------------------------------------------------
db.getCollection('grade').insertMany([
  {
    _id: gradeAdsoId,
    code: '2829810',
    state: 'ACTIVA',
    start_date: bogoDate('2026-02-02'),
    end_date: bogoDate('2028-02-01'),
    program: DBRef('program', progAdsoId),
    modality: DBRef('modality', modPresencialId),
    timeSlot: DBRef('time_slot', tsMananaId),
    ...auditing(GRADES_DATE),
    _class: 'com.mycompany.senaattendance.domain.Grade',
  },
  {
    _id: gradeGrdId,
    code: '2829811',
    state: 'ACTIVA',
    start_date: bogoDate('2026-02-02'),
    end_date: bogoDate('2028-02-01'),
    program: DBRef('program', progGrdId),
    modality: DBRef('modality', modPresencialId),
    timeSlot: DBRef('time_slot', tsTardeId),
    ...auditing(GRADES_DATE),
    _class: 'com.mycompany.senaattendance.domain.Grade',
  },
]);

// ----------------------------------------------------------------------------
// 8. Matrículas (Apprentice)
//    Todos MATRICULADO salvo Danna (APLAZADO).
// ----------------------------------------------------------------------------
const apprenticeDocs = usersSeed
  .filter(user => user.role === 'APPRENTICE')
  .map(user => ({
    _id: new ObjectId(),
    state_academic: user.login === 'cc_1009871234' ? 'APLAZADO' : 'MATRICULADO',
    student: DBRef('user_profile', profileIdByLogin[user.login]),
    grade: DBRef('grade', user.ficha === 'ADSO' ? gradeAdsoId : gradeGrdId),
    ...auditing(GRADES_DATE),
    _class: 'com.mycompany.senaattendance.domain.Apprentice',
  }));
db.getCollection('apprentice').insertMany(apprenticeDocs);

// ----------------------------------------------------------------------------
// 9. Competencias (ClassSection), horarios (ClassSchedule) y excepciones
// ----------------------------------------------------------------------------
const sections = [
  { key: 'csConstruccionId', id: csConstruccionId, ficha: 'ADSO', days: [1, 3], excluded: [] },
  { key: 'csBasesDatosId', id: csBasesDatosId, ficha: 'ADSO', days: [2, 4], excluded: [] },
  { key: 'csInglesId', id: csInglesId, ficha: 'ADSO', days: [5], excluded: [] },
  { key: 'csRedesId', id: csRedesId, ficha: 'GRD', days: [1, 3], excluded: [] },
  { key: 'csSeguridadId', id: csSeguridadId, ficha: 'GRD', days: [2, 4], excluded: [] },
  { key: 'csMantenimientoId', id: csMantenimientoId, ficha: 'GRD', days: [5], excluded: [] },
];

const sectionByKey = {};
sections.forEach(section => {
  section.scheduleRefs = [];
  section.exceptionRefs = [];
  sectionByKey[section.key] = section;
});

const scheduleDocs = [];
function addSchedule(sectionKey, dayOfWeek, startH, startM, endH, endM) {
  const section = sectionByKey[sectionKey];
  const scheduleId = new ObjectId();
  scheduleDocs.push({
    _id: scheduleId,
    day_of_week: dayOfWeek,
    start_time: localTime(startH, startM),
    end_time: localTime(endH, endM),
    trimester: DBRef('trimester', trimActivoId),
    classSection: DBRef('class_section', section.id),
    ...auditing(SECTIONS_DATE),
    _class: 'com.mycompany.senaattendance.domain.ClassSchedule',
  });
  section.scheduleRefs.push(DBRef('class_schedule', scheduleId));
}

const exceptionDocs = [];
function addException(sectionKey, iso, reason) {
  const section = sectionByKey[sectionKey];
  section.excluded.push(iso);
  const exceptionId = new ObjectId();
  exceptionDocs.push({
    _id: exceptionId,
    date: bogoDate(iso),
    reason,
    classSection: DBRef('class_section', section.id),
    ...auditing(SECTIONS_DATE),
    _class: 'com.mycompany.senaattendance.domain.ClassException',
  });
  section.exceptionRefs.push(DBRef('class_exception', exceptionId));
}

// Ficha ADSO (jornada mañana)
addSchedule('csConstruccionId', 'LUNES', 7, 0, 10, 0);
addSchedule('csConstruccionId', 'MIERCOLES', 7, 0, 10, 0);
addSchedule('csBasesDatosId', 'MARTES', 7, 0, 10, 0);
addSchedule('csBasesDatosId', 'JUEVES', 7, 0, 10, 0);
addSchedule('csInglesId', 'VIERNES', 7, 0, 10, 0);
// Ficha GRD (jornada tarde)
addSchedule('csRedesId', 'LUNES', 14, 0, 17, 0);
addSchedule('csRedesId', 'MIERCOLES', 14, 0, 17, 0);
addSchedule('csSeguridadId', 'MARTES', 14, 0, 17, 0);
addSchedule('csSeguridadId', 'JUEVES', 14, 0, 17, 0);
addSchedule('csMantenimientoId', 'VIERNES', 14, 0, 17, 0);

// Excepciones (días sin clase)
addException('csConstruccionId', '2026-09-07', 'Jornada pedagógica institucional: no hubo clase');
addException('csRedesId', '2026-09-14', 'Mantenimiento programado del aula de redes: no hubo clase');

const classSectionDocs = [
  {
    _id: csConstruccionId,
    subject_name: 'Construcción de software',
    is_active: true,
    schedules: sectionByKey['csConstruccionId'].scheduleRefs,
    exceptions: sectionByKey['csConstruccionId'].exceptionRefs,
    instructor: DBRef('user_profile', profileIdByLogin['cc_79654321']),
    grade: DBRef('grade', gradeAdsoId),
    ...auditing(SECTIONS_DATE),
    _class: 'com.mycompany.senaattendance.domain.ClassSection',
  },
  {
    _id: csBasesDatosId,
    subject_name: 'Bases de datos',
    is_active: true,
    schedules: sectionByKey['csBasesDatosId'].scheduleRefs,
    exceptions: sectionByKey['csBasesDatosId'].exceptionRefs,
    instructor: DBRef('user_profile', profileIdByLogin['cc_52890417']),
    grade: DBRef('grade', gradeAdsoId),
    ...auditing(SECTIONS_DATE),
    _class: 'com.mycompany.senaattendance.domain.ClassSection',
  },
  {
    _id: csInglesId,
    subject_name: 'Inglés técnico',
    is_active: true,
    schedules: sectionByKey['csInglesId'].scheduleRefs,
    exceptions: sectionByKey['csInglesId'].exceptionRefs,
    instructor: DBRef('user_profile', profileIdByLogin['cc_79654321']),
    grade: DBRef('grade', gradeAdsoId),
    ...auditing(SECTIONS_DATE),
    _class: 'com.mycompany.senaattendance.domain.ClassSection',
  },
  {
    _id: csRedesId,
    subject_name: 'Configuración de redes de datos',
    is_active: true,
    schedules: sectionByKey['csRedesId'].scheduleRefs,
    exceptions: sectionByKey['csRedesId'].exceptionRefs,
    instructor: DBRef('user_profile', profileIdByLogin['cc_1023456789']),
    grade: DBRef('grade', gradeGrdId),
    ...auditing(SECTIONS_DATE),
    _class: 'com.mycompany.senaattendance.domain.ClassSection',
  },
  {
    _id: csSeguridadId,
    subject_name: 'Seguridad informática',
    is_active: true,
    schedules: sectionByKey['csSeguridadId'].scheduleRefs,
    exceptions: sectionByKey['csSeguridadId'].exceptionRefs,
    instructor: DBRef('user_profile', profileIdByLogin['cc_1023456789']),
    grade: DBRef('grade', gradeGrdId),
    ...auditing(SECTIONS_DATE),
    _class: 'com.mycompany.senaattendance.domain.ClassSection',
  },
  {
    _id: csMantenimientoId,
    subject_name: 'Mantenimiento de equipos de cómputo',
    is_active: true,
    schedules: sectionByKey['csMantenimientoId'].scheduleRefs,
    exceptions: sectionByKey['csMantenimientoId'].exceptionRefs,
    instructor: DBRef('user_profile', profileIdByLogin['cc_52890417']),
    grade: DBRef('grade', gradeGrdId),
    ...auditing(SECTIONS_DATE),
    _class: 'com.mycompany.senaattendance.domain.ClassSection',
  },
];

db.getCollection('class_section').insertMany(classSectionDocs);
db.getCollection('class_schedule').insertMany(scheduleDocs);
db.getCollection('class_exception').insertMany(exceptionDocs);

// ----------------------------------------------------------------------------
// 10. Asistencias
//     Ventana: lunes 2026-08-03 a viernes 2026-09-18, según los días de cada
//     competencia, sin las fechas de excepción. Estado por defecto PRESENTE y
//     un mapa explícito de novedades (tardanzas, fallas y justificadas).
// ----------------------------------------------------------------------------
function sessionDates(dayIndex, fromISO, toISO, excluded) {
  const dates = [];
  const cursor = new Date(fromISO + 'T12:00:00.000Z');
  const end = new Date(toISO + 'T12:00:00.000Z');
  while (cursor <= end) {
    if (cursor.getUTCDay() === dayIndex) {
      const iso = cursor.toISOString().slice(0, 10);
      if (!excluded.includes(iso)) {
        dates.push(iso);
      }
    }
    cursor.setUTCDate(cursor.getUTCDate() + 1);
  }
  return dates;
}

const loginsByFicha = { ADSO: [], GRD: [] };
usersSeed
  .filter(user => user.role === 'APPRENTICE')
  .forEach(user => loginsByFicha[user.ficha].push(user.login));

// Novedades: login|competencia|fecha -> estado.
// El dominio actual solo define PRESENTE, FALLA y JUSTIFICADA: una llegada tarde
// se registra como PRESENTE, por eso no hay entradas de tardanza aquí.
const attendanceOverrides = {
  'cc_1003456789|csConstruccionId|2026-08-19': 'JUSTIFICADA',
  'cc_1005678901|csBasesDatosId|2026-08-25': 'FALLA',
  'cc_1005678901|csBasesDatosId|2026-08-27': 'FALLA',
  'cc_1005678901|csBasesDatosId|2026-09-01': 'FALLA',
  'cc_1005678901|csBasesDatosId|2026-09-10': 'FALLA',
  'ti_1093456782|csInglesId|2026-08-21': 'FALLA',
  'ti_1093456782|csInglesId|2026-08-28': 'FALLA',
  'cc_1002345678|csBasesDatosId|2026-09-08': 'JUSTIFICADA',
  'cc_1010987654|csInglesId|2026-09-04': 'FALLA',
  'cc_1010987654|csInglesId|2026-09-11': 'FALLA',
  'cc_1010987654|csInglesId|2026-09-18': 'FALLA',
  'cc_1001234567|csRedesId|2026-08-05': 'FALLA',
  'cc_1001234567|csRedesId|2026-08-19': 'FALLA',
  'cc_1001234567|csSeguridadId|2026-08-11': 'FALLA',
  'cc_1001234567|csSeguridadId|2026-08-25': 'FALLA',
  'cc_1001234567|csMantenimientoId|2026-09-04': 'FALLA',
};

// Fallas cubiertas por una justificación aprobada
const justifiedByKey = {
  'cc_1003456789|csConstruccionId|2026-08-19': justificacionCitaId,
  'cc_1002345678|csBasesDatosId|2026-09-08': justificacionIncapacidadNicolasId,
};

const attendanceDocs = [];
const attendanceByKey = {};
sections.forEach(section => {
  section.days.forEach(dayIndex => {
    sessionDates(dayIndex, '2026-08-03', '2026-09-18', section.excluded).forEach(iso => {
      loginsByFicha[section.ficha].forEach(login => {
        // Danna está APLAZADA: solo tiene asistencias hasta el 14 de agosto
        if (login === 'cc_1009871234' && iso > '2026-08-14') {
          return;
        }
        const key = login + '|' + section.key + '|' + iso;
        const state = attendanceOverrides[key] || 'PRESENTE';
        const attendance = {
          _id: new ObjectId(),
          date: bogoDate(iso),
          state_attendance: state,
          auditLogs: [],
          classSection: DBRef('class_section', section.id),
          student: DBRef('user_profile', profileIdByLogin[login]),
          ...auditing(instant(iso + 'T23:30:00.000Z')),
          _class: 'com.mycompany.senaattendance.domain.Attendance',
        };
        if (state === 'JUSTIFICADA') {
          attendance.modifiedByJustification = DBRef('justification', justifiedByKey[key]);
        }
        attendanceDocs.push(attendance);
        attendanceByKey[key] = attendance;
      });
    });
  });
});

// ----------------------------------------------------------------------------
// 11. Registros de auditoría (cambios de estado sobre una asistencia)
// ----------------------------------------------------------------------------
const auditDocs = [];
function addAudit(auditId, login, sectionKey, iso, previousState, newState, editIso, instructorLogin) {
  const attendance = attendanceByKey[login + '|' + sectionKey + '|' + iso];
  auditDocs.push({
    _id: auditId,
    previous_state: previousState,
    new_state: newState,
    edit_date: instant(editIso),
    modifiedBy: DBRef('user_profile', profileIdByLogin[instructorLogin]),
    attendance: DBRef('attendance', attendance._id),
    ...auditing(instant(editIso)),
    _class: 'com.mycompany.senaattendance.domain.AuditLog',
  });
  attendance.auditLogs.push(DBRef('audit_log', auditId));
}

addAudit(auditJuanId, 'cc_1003456789', 'csConstruccionId', '2026-08-19', 'FALLA', 'JUSTIFICADA', '2026-08-20T15:35:00.000Z', 'cc_79654321');
addAudit(auditSebastianId, 'cc_1001234567', 'csMantenimientoId', '2026-09-04', 'PRESENTE', 'FALLA', '2026-09-04T22:15:00.000Z', 'cc_52890417');
addAudit(auditValentinaId, 'cc_1007890123', 'csConstruccionId', '2026-08-12', 'FALLA', 'PRESENTE', '2026-08-12T15:20:00.000Z', 'cc_79654321');

db.getCollection('attendance').insertMany(attendanceDocs);
db.getCollection('audit_log').insertMany(auditDocs);

// ----------------------------------------------------------------------------
// 12. Justificaciones y sus partes (JustificationDetails)
//     Evidencia: PDF pequeño de ejemplo embebido como BinData.
// ----------------------------------------------------------------------------
const EVIDENCE_PDF_BASE64 =
  'JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCjIgMCBvYmoKPDwgL1R5cGUgL1BhZ2VzIC9LaWRzIFszIDAgUl0gL0NvdW50IDEgPj4KZW5kb2JqCjMgMCBvYmoKPDwgL1R5cGUgL1BhZ2UgL1BhcmVudCAyIDAgUiAvTWVkaWFCb3ggWzAgMCA2MTIgNzkyXSAvQ29udGVudHMgNCAwIFIgL1Jlc291cmNlcyA8PCAvRm9udCA8PCAvRjEgNSAwIFIgPj4gPj4gPj4KZW5kb2JqCjQgMCBvYmoKPDwgL0xlbmd0aCA3OCA+PgpzdHJlYW0KQlQgL0YxIDEyIFRmIDcyIDcyMCBUZCAoU0VOQSBBdHRlbmRhbmNlIC0gZXZpZGVuY2lhIGRlIGp1c3RpZmljYWNpb24pIFRqIEVUCmVuZHN0cmVhbQplbmRvYmoKNSAwIG9iago8PCAvVHlwZSAvRm9udCAvU3VidHlwZSAvVHlwZTEgL0Jhc2VGb250IC9IZWx2ZXRpY2EgPj4KZW5kb2JqCnRyYWlsZXIKPDwgL1Jvb3QgMSAwIFIgPj4KJSVFT0YK';

const justificationDetailsDocs = [
  {
    _id: parteMariaId,
    state_justification: 'PENDIENTE',
    rejection_reason: '',
    correction_text: '',
    correction_file_url_content_type: '',
    classSection: DBRef('class_section', csBasesDatosId),
    justification: DBRef('justification', justificacionIncapacidadMariaId),
    ...auditing(instant('2026-09-10T14:00:00.000Z')),
    _class: 'com.mycompany.senaattendance.domain.JustificationDetails',
  },
  {
    _id: parteCitaId,
    state_justification: 'ACEPTADA',
    rejection_reason: '',
    correction_text: '',
    correction_file_url_content_type: '',
    response_date: instant('2026-08-20T15:30:00.000Z'),
    out_of_time_reason: null,
    late_decision: false,
    classSection: DBRef('class_section', csConstruccionId),
    justification: DBRef('justification', justificacionCitaId),
    ...auditing(instant('2026-08-19T14:00:00.000Z')),
    _class: 'com.mycompany.senaattendance.domain.JustificationDetails',
  },
  {
    _id: parteCalamidadId,
    state_justification: 'RECHAZADA',
    rejection_reason: 'La evidencia no acredita la calamidad en las fechas reportadas.',
    correction_text: '',
    correction_file_url_content_type: '',
    response_date: instant('2026-08-31T14:20:00.000Z'),
    out_of_time_reason: null,
    late_decision: false,
    classSection: DBRef('class_section', csInglesId),
    justification: DBRef('justification', justificacionCalamidadId),
    ...auditing(instant('2026-08-21T14:00:00.000Z')),
    _class: 'com.mycompany.senaattendance.domain.JustificationDetails',
  },
  {
    _id: parteNicolasId,
    state_justification: 'ACEPTADA',
    rejection_reason: '',
    correction_text: '',
    correction_file_url_content_type: '',
    response_date: instant('2026-09-11T16:10:00.000Z'),
    out_of_time_reason: 'El aprendiz radicó la incapacidad después de su hospitalización; se aprueba por caso fortuito.',
    late_decision: false,
    classSection: DBRef('class_section', csBasesDatosId),
    justification: DBRef('justification', justificacionIncapacidadNicolasId),
    ...auditing(instant('2026-09-08T14:00:00.000Z')),
    _class: 'com.mycompany.senaattendance.domain.JustificationDetails',
  },
];

const justificationDocs = [
  {
    _id: justificacionIncapacidadMariaId,
    description: 'Incapacidad médica expedida por la EPS por un día.',
    start_date: bogoDate('2026-09-10'),
    end_date: bogoDate('2026-09-10'),
    evidence: BinData(0, EVIDENCE_PDF_BASE64),
    evidence_content_type: 'application/pdf',
    on_time: true,
    details: [DBRef('justification_details', parteMariaId)],
    justificationType: DBRef('justification_type', jtIncapacidadId),
    student: DBRef('user_profile', profileIdByLogin['cc_1005678901']),
    ...auditing(instant('2026-09-10T14:00:00.000Z')),
    _class: 'com.mycompany.senaattendance.domain.Justification',
  },
  {
    _id: justificacionCitaId,
    description: 'Cita médica programada con especialista.',
    start_date: bogoDate('2026-08-19'),
    end_date: bogoDate('2026-08-19'),
    evidence: BinData(0, EVIDENCE_PDF_BASE64),
    evidence_content_type: 'application/pdf',
    on_time: true,
    details: [DBRef('justification_details', parteCitaId)],
    justificationType: DBRef('justification_type', jtCitaMedicaId),
    student: DBRef('user_profile', profileIdByLogin['cc_1003456789']),
    ...auditing(instant('2026-08-19T14:00:00.000Z')),
    _class: 'com.mycompany.senaattendance.domain.Justification',
  },
  {
    _id: justificacionCalamidadId,
    description: 'Calamidad doméstica durante la semana.',
    start_date: bogoDate('2026-08-21'),
    end_date: bogoDate('2026-08-28'),
    evidence: BinData(0, EVIDENCE_PDF_BASE64),
    evidence_content_type: 'application/pdf',
    on_time: true,
    details: [DBRef('justification_details', parteCalamidadId)],
    justificationType: DBRef('justification_type', jtCalamidadId),
    student: DBRef('user_profile', profileIdByLogin['ti_1093456782']),
    ...auditing(instant('2026-08-21T14:00:00.000Z')),
    _class: 'com.mycompany.senaattendance.domain.Justification',
  },
  {
    _id: justificacionIncapacidadNicolasId,
    description: 'Incapacidad médica radicada después de la hospitalización.',
    start_date: bogoDate('2026-09-08'),
    end_date: bogoDate('2026-09-08'),
    evidence: BinData(0, EVIDENCE_PDF_BASE64),
    evidence_content_type: 'application/pdf',
    on_time: false,
    details: [DBRef('justification_details', parteNicolasId)],
    justificationType: DBRef('justification_type', jtIncapacidadId),
    student: DBRef('user_profile', profileIdByLogin['cc_1002345678']),
    ...auditing(instant('2026-09-08T14:00:00.000Z')),
    _class: 'com.mycompany.senaattendance.domain.Justification',
  },
];

db.getCollection('justification_details').insertMany(justificationDetailsDocs);
db.getCollection('justification').insertMany(justificationDocs);

// ----------------------------------------------------------------------------
// 13. Alertas del motor de deserción
//     - Daniel: 3 fallas consecutivas en Inglés técnico (NO_LEIDA)
//     - Sebastián: 5 fallas acumuladas en la ficha GRD (ATENDIDA)
//     - María Camila: alerta de fallas consecutivas resuelta al volver a clase
// ----------------------------------------------------------------------------
db.getCollection('alerta').insertMany([
  {
    _id: alertaDanielId,
    student: DBRef('user_profile', profileIdByLogin['cc_1010987654']),
    classSection: DBRef('class_section', csInglesId),
    grade: DBRef('grade', gradeAdsoId),
    trimester: DBRef('trimester', trimActivoId),
    type: 'CONSECUTIVAS',
    state: 'NO_LEIDA',
    absence_count: 3,
    threshold: 3,
    generated_at: instant('2026-09-18T15:10:00.000Z'),
    ...auditing(instant('2026-09-18T15:10:00.000Z')),
    _class: 'com.mycompany.senaattendance.domain.Alerta',
  },
  {
    _id: alertaSebastianId,
    student: DBRef('user_profile', profileIdByLogin['cc_1001234567']),
    grade: DBRef('grade', gradeGrdId),
    trimester: DBRef('trimester', trimActivoId),
    type: 'ACUMULADAS',
    state: 'ATENDIDA',
    absence_count: 5,
    threshold: 5,
    generated_at: instant('2026-09-04T22:10:00.000Z'),
    observation: 'El instructor contactó al aprendiz; se acordó plan de nivelación y seguimiento.',
    ...auditing(instant('2026-09-04T22:10:00.000Z')),
    _class: 'com.mycompany.senaattendance.domain.Alerta',
  },
  {
    _id: alertaMariaId,
    student: DBRef('user_profile', profileIdByLogin['cc_1005678901']),
    classSection: DBRef('class_section', csBasesDatosId),
    grade: DBRef('grade', gradeAdsoId),
    trimester: DBRef('trimester', trimActivoId),
    type: 'CONSECUTIVAS',
    state: 'RESUELTA_AUTOMATICAMENTE',
    absence_count: 3,
    threshold: 3,
    generated_at: instant('2026-09-01T15:05:00.000Z'),
    resolved_at: instant('2026-09-03T15:05:00.000Z'),
    ...auditing(instant('2026-09-01T15:05:00.000Z')),
    _class: 'com.mycompany.senaattendance.domain.Alerta',
  },
]);

// ----------------------------------------------------------------------------
// 14. Notificaciones (bandeja de entrada)
// ----------------------------------------------------------------------------
const notificationDocs = [
  // Alerta de Daniel (consecutivas, sin leer)
  { user: 'cc_1010987654', tipo: 'ALERTA', estado: 'ENVIADA', read: false, referenceType: 'ALERT', referenceId: alertaDanielId, mensaje: 'Se generó una alerta por fallas consecutivas en una de tus materias.', createdAt: instant('2026-09-18T15:10:00.000Z') },
  { user: 'cc_79654321', tipo: 'ALERTA', estado: 'ENVIADA', read: false, referenceType: 'ALERT', referenceId: alertaDanielId, mensaje: 'Se generó una alerta de inasistencia en una de tus materias.', createdAt: instant('2026-09-18T15:10:00.000Z') },
  // Alerta de Sebastián (acumuladas, atendida)
  { user: 'cc_1001234567', tipo: 'ALERTA', estado: 'ENVIADA', read: true, referenceType: 'ALERT', referenceId: alertaSebastianId, mensaje: 'Se generó una alerta por fallas acumuladas en tu ficha.', createdAt: instant('2026-09-04T22:10:00.000Z') },
  { user: 'cc_1023456789', tipo: 'ALERTA', estado: 'ENVIADA', read: true, referenceType: 'ALERT', referenceId: alertaSebastianId, mensaje: 'Se generó una alerta de inasistencia en tu ficha.', createdAt: instant('2026-09-04T22:10:00.000Z') },
  { user: 'cc_52890417', tipo: 'ALERTA', estado: 'ENVIADA', read: true, referenceType: 'ALERT', referenceId: alertaSebastianId, mensaje: 'Se generó una alerta de inasistencia en tu ficha.', createdAt: instant('2026-09-04T22:10:00.000Z') },
  // Alerta de María Camila (resuelta automáticamente)
  { user: 'cc_1005678901', tipo: 'ALERTA', estado: 'ENVIADA', read: true, referenceType: 'ALERT', referenceId: alertaMariaId, mensaje: 'Tu alerta de inasistencia fue resuelta automáticamente: tus fallas bajaron del umbral.', createdAt: instant('2026-09-03T15:05:00.000Z') },
  { user: 'cc_52890417', tipo: 'ALERTA', estado: 'ENVIADA', read: true, referenceType: 'ALERT', referenceId: alertaMariaId, mensaje: 'Una alerta de inasistencia de tu materia fue resuelta automáticamente: las fallas del aprendiz bajaron del umbral.', createdAt: instant('2026-09-03T15:05:00.000Z') },
  // Justificación pendiente de María Camila
  { user: 'cc_1005678901', tipo: 'JUSTIFICACION', estado: 'ENVIADA', read: false, referenceType: 'JUSTIFICATION', referenceId: justificacionIncapacidadMariaId, mensaje: 'Tu justificación quedó registrada y está pendiente de revisión.', createdAt: instant('2026-09-10T14:00:00.000Z') },
  { user: 'cc_52890417', tipo: 'JUSTIFICACION', estado: 'ENVIADA', read: false, referenceType: 'JUSTIFICATION', referenceId: justificacionIncapacidadMariaId, mensaje: 'Recibiste una nueva justificación pendiente de revisión en una de tus materias.', createdAt: instant('2026-09-10T14:00:00.000Z') },
  // Justificación aprobada de Juan José
  { user: 'cc_1003456789', tipo: 'JUSTIFICACION', estado: 'ENVIADA', read: true, referenceType: 'JUSTIFICATION', referenceId: justificacionCitaId, mensaje: 'Tu justificación fue aprobada.', createdAt: instant('2026-08-20T15:30:00.000Z') },
  // Justificación rechazada de Santiago
  { user: 'ti_1093456782', tipo: 'JUSTIFICACION', estado: 'ENVIADA', read: false, referenceType: 'JUSTIFICATION', referenceId: justificacionCalamidadId, mensaje: 'Tu justificación fue rechazada.', createdAt: instant('2026-08-31T14:20:00.000Z') },
  // Justificación aprobada de Nicolás
  { user: 'cc_1002345678', tipo: 'JUSTIFICACION', estado: 'ENVIADA', read: true, referenceType: 'JUSTIFICATION', referenceId: justificacionIncapacidadNicolasId, mensaje: 'Tu justificación fue aprobada.', createdAt: instant('2026-09-11T16:10:00.000Z') },
].map(row => ({
  _id: new ObjectId(),
  user: DBRef('user', userIdByLogin[row.user]),
  tipo: row.tipo,
  estado: row.estado,
  mensaje: row.mensaje,
  read: row.read,
  reference_type: row.referenceType,
  reference_id: row.referenceId.toHexString(),
  ...auditing(row.createdAt),
  _class: 'com.mycompany.senaattendance.domain.Notificacion',
}));

db.getCollection('notificacion').insertMany(notificationDocs);

// ----------------------------------------------------------------------------
// 15. Resumen
// ----------------------------------------------------------------------------
print('Semilla aplicada correctamente:');
print('  - Usuarios:               ' + userDocs.length);
print('  - Perfiles:               ' + profileDocs.length);
print('  - Fichas:                 ' + 2);
print('  - Matrículas:             ' + apprenticeDocs.length);
print('  - Competencias:           ' + classSectionDocs.length);
print('  - Horarios:               ' + scheduleDocs.length);
print('  - Excepciones:            ' + exceptionDocs.length);
print('  - Asistencias:            ' + attendanceDocs.length);
print('  - Justificaciones:        ' + justificationDocs.length);
print('  - Alertas:                ' + 3);
print('  - Notificaciones:         ' + notificationDocs.length);
print('  - Registros de auditoría: ' + auditDocs.length);
print('Consulta las credenciales en docs/seed-credentials.md');
