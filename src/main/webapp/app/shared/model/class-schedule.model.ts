import { IClassSection } from 'app/shared/model/class-section.model';
import { DayOfWeek } from 'app/shared/model/enumerations/day-of-week.model';
import { ITrimester } from 'app/shared/model/trimester.model';

export interface IClassSchedule {
  id?: string;
  dayOfWeek?: keyof typeof DayOfWeek | null;
  startTime?: string;
  endTime?: string;
  trimester?: ITrimester;
  classSection?: IClassSection;
}

export const defaultValue: Readonly<IClassSchedule> = {};
