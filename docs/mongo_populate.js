```javascript
// ====== SCRIPT DE SIMULACIÓN PARA MONGODB (SENA ATTENDANCE) ======
// Instrucciones de uso:
// 1. Abre tu terminal donde tienes mongosh (MongoDB Shell) o abre MongoDB Compass (opción de mongosh en la parte inferior).
// 2. Copia todo este código y pégalo.
// 3. Presiona Enter.

// 1. Seleccionamos la base de datos
use('senaAttendance');

// 2. Limpiamos las colecciones involucradas (Opcional, para evitar duplicados si lo corres varias veces)
db.document_type.deleteMany({});
db.program.deleteMany({});
db.modality.deleteMany({});
db.time_slot.deleteMany({});
db.user_profile.deleteMany({});
db.grade.deleteMany({});
db.apprentice.deleteMany({});
db.class_section.deleteMany({});
db.trimester.deleteMany({});
db.attendance.deleteMany({});

// 3. Generamos ObjectIDs para mantener las referencias (DBRefs) correctas
const dtCcId = new ObjectId();
const dtTiId = new ObjectId();

const progAdsoId = new ObjectId();
const progTsiId = new ObjectId();

const modPresId = new ObjectId();
const modVirtId = new ObjectId();

const tsDiurnaId = new ObjectId();
const tsNocturnaId = new ObjectId();

const trim1Id = new ObjectId();

// Usuarios
const instJuanId = new ObjectId();
const aprMariaId = new ObjectId();
const aprCarlosId = new ObjectId();
const aprAnaId = new ObjectId();
const aprLuisId = new ObjectId();

// Fichas
const fichaAdsoId = new ObjectId();
const fichaTsiId = new ObjectId();

// Pivot Apprentices
const pivotMariaId = new ObjectId();
const pivotCarlosId = new ObjectId();
const pivotAnaId = new ObjectId();
const pivotLuisId = new ObjectId();

// Secciones de clase (Competencias)
const compProgId = new ObjectId();
const compDbId = new ObjectId();

// 4. INSERTAR CATÁLOGOS BASE
db.document_type.insertMany([
  { _id: dtCcId, name: "Cédula de Ciudadanía", initials: "CC", createdDate: new Date(), lastModifiedDate: new Date() },
  { _id: dtTiId, name: "Tarjeta de Identidad", initials: "TI", createdDate: new Date(), lastModifiedDate: new Date() }
]);

db.program.insertMany([
  { _id: progAdsoId, name: "Análisis y Desarrollo de Software", initials: "ADSO", code: "228118", trimesters: 6, createdDate: new Date(), lastModifiedDate: new Date() },
  { _id: progTsiId, name: "Tecnología en Sistemas de Información", initials: "TSI", code: "228119", trimesters: 6, createdDate: new Date(), lastModifiedDate: new Date() }
]);

db.modality.insertMany([
  { _id: modPresId, name: "Presencial", isActive: true, createdDate: new Date(), lastModifiedDate: new Date() },
  { _id: modVirtId, name: "Virtual", isActive: true, createdDate: new Date(), lastModifiedDate: new Date() }
]);

db.time_slot.insertMany([
  { _id: tsDiurnaId, name: "Diurna", isActive: true, startTime: "06:00:00", endTime: "18:00:00", createdDate: new Date(), lastModifiedDate: new Date() },
  { _id: tsNocturnaId, name: "Nocturna", isActive: true, startTime: "18:00:00", endTime: "22:00:00", createdDate: new Date(), lastModifiedDate: new Date() }
]);

db.trimester.insertMany([
  { _id: trim1Id, name: "Trimestre 1", startDate: new Date("2024-02-01"), endDate: new Date("2024-05-01"), state: "ACTIVO", createdDate: new Date(), lastModifiedDate: new Date() }
]);

// 5. INSERTAR PERFILES DE USUARIO (1 Instructor, 4 Aprendices)
db.user_profile.insertMany([
  { _id: instJuanId, firstName: "Juan", firstLastName: "Rojas", documentNumber: "10001000", phoneNumber: "3000000000", documentType: DBRef("document_type", dtCcId), createdDate: new Date(), lastModifiedDate: new Date() },
  { _id: aprMariaId, firstName: "Maria", firstLastName: "Perez", documentNumber: "10002001", phoneNumber: "3000000001", documentType: DBRef("document_type", dtCcId), createdDate: new Date(), lastModifiedDate: new Date() },
  { _id: aprCarlosId, firstName: "Carlos", firstLastName: "Gomez", documentNumber: "10002002", phoneNumber: "3000000002", documentType: DBRef("document_type", dtCcId), createdDate: new Date(), lastModifiedDate: new Date() },
  { _id: aprAnaId, firstName: "Ana", firstLastName: "Silva", documentNumber: "10002003", phoneNumber: "3000000003", documentType: DBRef("document_type", dtCcId), createdDate: new Date(), lastModifiedDate: new Date() },
  { _id: aprLuisId, firstName: "Luis", firstLastName: "Vargas", documentNumber: "10002004", phoneNumber: "3000000004", documentType: DBRef("document_type", dtCcId), createdDate: new Date(), lastModifiedDate: new Date() }
]);

// 6. INSERTAR FICHAS (Grades)
db.grade.insertMany([
  { _id: fichaAdsoId, code: "2829810", state: "ACTIVA", startDate: new Date("2024-02-01"), endDate: new Date("2025-12-01"), program: DBRef("program", progAdsoId), modality: DBRef("modality", modPresId), timeSlot: DBRef("time_slot", tsDiurnaId), createdDate: new Date(), lastModifiedDate: new Date() },
  { _id: fichaTsiId, code: "2829811", state: "ACTIVA", startDate: new Date("2024-02-01"), endDate: new Date("2025-12-01"), program: DBRef("program", progTsiId), modality: DBRef("modality", modPresId), timeSlot: DBRef("time_slot", tsNocturnaId), createdDate: new Date(), lastModifiedDate: new Date() }
]);

// 7. ASOCIAR APRENDICES A FICHAS (Apprentice)
// Maria y Carlos a ADSO
// Ana y Luis a TSI
db.apprentice.insertMany([
  { _id: pivotMariaId, stateAcademic: "MATRICULADO", student: DBRef("user_profile", aprMariaId), grade: DBRef("grade", fichaAdsoId), createdDate: new Date(), lastModifiedDate: new Date() },
  { _id: pivotCarlosId, stateAcademic: "MATRICULADO", student: DBRef("user_profile", aprCarlosId), grade: DBRef("grade", fichaAdsoId), createdDate: new Date(), lastModifiedDate: new Date() },
  { _id: pivotAnaId, stateAcademic: "MATRICULADO", student: DBRef("user_profile", aprAnaId), grade: DBRef("grade", fichaTsiId), createdDate: new Date(), lastModifiedDate: new Date() },
  { _id: pivotLuisId, stateAcademic: "MATRICULADO", student: DBRef("user_profile", aprLuisId), grade: DBRef("grade", fichaTsiId), createdDate: new Date(), lastModifiedDate: new Date() }
]);

// 8. INSERTAR COMPETENCIAS / CLASES (ClassSection)
// Programación Básica para Ficha ADSO
// Bases de Datos para Ficha TSI
db.class_section.insertMany([
  { 
    _id: compProgId, 
    subjectName: "Programación Básica", 
    isActive: true, 
    instructor: DBRef("user_profile", instJuanId), 
    grade: DBRef("grade", fichaAdsoId),
    schedules: [
      { dayOfWeek: "LUNES", startTime: "08:00:00", endTime: "12:00:00", trimester: DBRef("trimester", trim1Id) }
    ],
    exceptions: [],
    createdDate: new Date(), lastModifiedDate: new Date() 
  },
  { 
    _id: compDbId, 
    subjectName: "Bases de Datos", 
    isActive: true, 
    instructor: DBRef("user_profile", instJuanId), 
    grade: DBRef("grade", fichaTsiId),
    schedules: [
      { dayOfWeek: "MARTES", startTime: "18:00:00", endTime: "22:00:00", trimester: DBRef("trimester", trim1Id) }
    ],
    exceptions: [],
    createdDate: new Date(), lastModifiedDate: new Date() 
  }
]);

// 9. INSERTAR ASISTENCIAS SIMULADAS (Attendance)
db.attendance.insertMany([
  // Asistencias ADSO - Clase Programación Básica (Lunes)
  { date: new Date("2024-03-04"), stateAttendance: "PRESENTE", classSection: DBRef("class_section", compProgId), student: DBRef("user_profile", aprMariaId), auditLogs: [], createdDate: new Date(), lastModifiedDate: new Date() },
  { date: new Date("2024-03-04"), stateAttendance: "FALLA", classSection: DBRef("class_section", compProgId), student: DBRef("user_profile", aprCarlosId), auditLogs: [], createdDate: new Date(), lastModifiedDate: new Date() },
  
  // Asistencias TSI - Clase Bases de Datos (Martes)
  { date: new Date("2024-03-05"), stateAttendance: "TARDE", classSection: DBRef("class_section", compDbId), student: DBRef("user_profile", aprAnaId), auditLogs: [], createdDate: new Date(), lastModifiedDate: new Date() },
  { date: new Date("2024-03-05"), stateAttendance: "PRESENTE", classSection: DBRef("class_section", compDbId), student: DBRef("user_profile", aprLuisId), auditLogs: [], createdDate: new Date(), lastModifiedDate: new Date() }
]);

print("¡Simulación completada con éxito! Se han insertado 2 programas, 2 fichas, 4 aprendices y sus asistencias asociadas.");
```
