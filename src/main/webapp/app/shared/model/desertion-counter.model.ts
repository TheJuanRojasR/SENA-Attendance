import { ITrimester } from 'app/shared/model/trimester.model';
import { IUserProfile } from 'app/shared/model/user-profile.model';

export interface IDesertionCounter {
  id?: string;
  totalGlobalAbsences?: number | null;
  accumulatedLateArrivals?: number | null;
  workJustificationsUsed?: number | null;
  alertsSent?: boolean | null;
  student?: IUserProfile;
  trimester?: ITrimester;
}

export const defaultValue: Readonly<IDesertionCounter> = {
  alertsSent: false,
};
