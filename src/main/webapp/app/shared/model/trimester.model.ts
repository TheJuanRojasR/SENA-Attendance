import dayjs from 'dayjs';

import { StateTrimester } from 'app/shared/model/enumerations/state-trimester.model';

export interface ITrimester {
  id?: string;
  name?: string;
  startDate?: dayjs.Dayjs;
  endDate?: dayjs.Dayjs;
  status?: keyof typeof StateTrimester;
}

export const defaultValue: Readonly<ITrimester> = {};
