import dayjs from 'dayjs';

import { IClassSection } from 'app/shared/model/class-section.model';
import { StateAttendance } from 'app/shared/model/enumerations/state-attendance.model';
import { IJustification } from 'app/shared/model/justification.model';
import { IUserProfile } from 'app/shared/model/user-profile.model';

export interface IAttendance {
  id?: string;
  date?: dayjs.Dayjs;
  stateAttendance?: keyof typeof StateAttendance;
  classSection?: IClassSection;
  student?: IUserProfile;
  modifiedByJustification?: IJustification | null;
}

export const defaultValue: Readonly<IAttendance> = {};
