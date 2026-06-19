import dayjs from 'dayjs';

import { StateGrade } from 'app/shared/model/enumerations/state-grade.model';
import { IModality } from 'app/shared/model/modality.model';
import { IProgram } from 'app/shared/model/program.model';
import { ITimeSlot } from 'app/shared/model/time-slot.model';

export interface IGrade {
  id?: string;
  code?: string;
  state?: keyof typeof StateGrade;
  startDate?: dayjs.Dayjs;
  endDate?: dayjs.Dayjs;
  program?: IProgram;
  modality?: IModality;
  timeSlot?: ITimeSlot;
}

export const defaultValue: Readonly<IGrade> = {};
