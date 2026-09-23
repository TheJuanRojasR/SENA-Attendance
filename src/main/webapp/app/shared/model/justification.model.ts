import dayjs from 'dayjs';

import { IJustificationDetails } from 'app/shared/model/justification-details.model';
import { IJustificationType } from 'app/shared/model/justification-type.model';
import { IUserProfile } from 'app/shared/model/user-profile.model';

export interface IJustification {
  id?: string;
  description?: string;
  startDate?: dayjs.Dayjs;
  endDate?: dayjs.Dayjs;
  evidenceContentType?: string;
  evidence?: string;
  onTime?: boolean;
  justificationType?: IJustificationType;
  student?: IUserProfile;
  detailses?: IJustificationDetails[];
}

export const defaultValue: Readonly<IJustification> = {};
