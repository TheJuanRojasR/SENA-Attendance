import dayjs from 'dayjs';

import { IAttendance } from 'app/shared/model/attendance.model';
import { StateAttendance } from 'app/shared/model/enumerations/state-attendance.model';
import { IUserProfile } from 'app/shared/model/user-profile.model';

export interface IAuditLog {
  id?: string;
  previousState?: keyof typeof StateAttendance;
  newState?: keyof typeof StateAttendance;
  editDate?: dayjs.Dayjs;
  modifiedBy?: IUserProfile;
  attendance?: IAttendance;
}

export const defaultValue: Readonly<IAuditLog> = {};
