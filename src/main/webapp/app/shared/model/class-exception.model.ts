import dayjs from 'dayjs';

import { IClassSection } from 'app/shared/model/class-section.model';

export interface IClassException {
  id?: string;
  date?: dayjs.Dayjs;
  reason?: string;
  classSection?: IClassSection;
}

export const defaultValue: Readonly<IClassException> = {};
