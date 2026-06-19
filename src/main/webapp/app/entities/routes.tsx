import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Apprentice from './apprentice';
import ClassException from './class-exception';
import JustificationType from './justification-type';
import Attendance from './attendance';
import AuditLog from './audit-log';
import ClassSchedule from './class-schedule';
import ClassSection from './class-section';
import DesertionCounter from './desertion-counter';
import DocumentType from './document-type';
import GlobalConfiguration from './global-configuration';
import Grade from './grade';
import Justification from './justification';
import JustificationDetails from './justification-details';
import Modality from './modality';
import Program from './program';
import TimeSlot from './time-slot';
import Trimester from './trimester';
import UserProfile from './user-profile';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="/global-configuration/*" element={<GlobalConfiguration />} />
        <Route path="/time-slot/*" element={<TimeSlot />} />
        <Route path="/modality/*" element={<Modality />} />
        <Route path="/program/*" element={<Program />} />
        <Route path="/grade/*" element={<Grade />} />
        <Route path="/document-type/*" element={<DocumentType />} />
        <Route path="/user-profile/*" element={<UserProfile />} />
        <Route path="/apprentice/*" element={<Apprentice />} />
        <Route path="/trimester/*" element={<Trimester />} />
        <Route path="/class-section/*" element={<ClassSection />} />
        <Route path="/class-schedule/*" element={<ClassSchedule />} />
        <Route path="/class-exception/*" element={<ClassException />} />
        <Route path="/justification-type/*" element={<JustificationType />} />
        <Route path="/attendance/*" element={<Attendance />} />
        <Route path="/audit-log/*" element={<AuditLog />} />
        <Route path="/justification/*" element={<Justification />} />
        <Route path="/justification-details/*" element={<JustificationDetails />} />
        <Route path="/desertion-counter/*" element={<DesertionCounter />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
