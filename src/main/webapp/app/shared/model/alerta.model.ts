import dayjs from 'dayjs';

import { AlertaState } from 'app/shared/model/enumerations/alerta-state.model';
import { AlertaType } from 'app/shared/model/enumerations/alerta-type.model';
import { IClassSection } from 'app/shared/model/class-section.model';
import { IGrade } from 'app/shared/model/grade.model';
import { ITrimester } from 'app/shared/model/trimester.model';
import { IUserProfile } from 'app/shared/model/user-profile.model';

export interface IAlerta {
  id?: string;
  student?: IUserProfile;
  classSection?: IClassSection | null;
  grade?: IGrade;
  trimester?: ITrimester;
  type?: keyof typeof AlertaType;
  state?: keyof typeof AlertaState;
  absenceCount?: number;
  threshold?: number;
  generatedAt?: dayjs.Dayjs;
  resolvedAt?: dayjs.Dayjs | null;
  observation?: string | null;
}

export const defaultValue: Readonly<IAlerta> = {};
