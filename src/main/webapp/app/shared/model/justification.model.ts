import dayjs from 'dayjs';

import { IJustificationType } from 'app/shared/model/justification-type.model';
import { IUserProfile } from 'app/shared/model/user-profile.model';

export interface IJustification {
  id?: string;
  description?: string;
  startDate?: dayjs.Dayjs;
  endDate?: dayjs.Dayjs;
  evidenceContentType?: string;
  evidence?: string;
  justificationType?: IJustificationType;
  student?: IUserProfile;
}

export const defaultValue: Readonly<IJustification> = {};
