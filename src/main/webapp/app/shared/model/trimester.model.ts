import dayjs from 'dayjs';

import { State } from 'app/shared/model/enumerations/state.model';

export interface ITrimester {
  id?: string;
  name?: string;
  startDate?: dayjs.Dayjs;
  endDate?: dayjs.Dayjs;
  state?: keyof typeof State;
}

export const defaultValue: Readonly<ITrimester> = {};
