import apprentice from 'app/entities/apprentice/apprentice.reducer';
import classException from 'app/entities/class-exception/class-exception.reducer';
import justificationType from 'app/entities/justification-type/justification-type.reducer';
import attendance from 'app/entities/attendance/attendance.reducer';
import auditLog from 'app/entities/audit-log/audit-log.reducer';
import classSchedule from 'app/entities/class-schedule/class-schedule.reducer';
import classSection from 'app/entities/class-section/class-section.reducer';
import desertionCounter from 'app/entities/desertion-counter/desertion-counter.reducer';
import documentType from 'app/entities/document-type/document-type.reducer';
import globalConfiguration from 'app/entities/global-configuration/global-configuration.reducer';
import grade from 'app/entities/grade/grade.reducer';
import justification from 'app/entities/justification/justification.reducer';
import justificationDetails from 'app/entities/justification-details/justification-details.reducer';
import modality from 'app/entities/modality/modality.reducer';
import program from 'app/entities/program/program.reducer';
import timeSlot from 'app/entities/time-slot/time-slot.reducer';
import trimester from 'app/entities/trimester/trimester.reducer';
import userProfile from 'app/entities/user-profile/user-profile.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  globalConfiguration,
  timeSlot,
  modality,
  program,
  grade,
  documentType,
  userProfile,
  apprentice,
  trimester,
  classSection,
  classSchedule,
  classException,
  justificationType,
  attendance,
  auditLog,
  justification,
  justificationDetails,
  desertionCounter,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
