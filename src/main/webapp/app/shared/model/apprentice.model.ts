import { StateAcademic } from 'app/shared/model/enumerations/state-academic.model';
import { IGrade } from 'app/shared/model/grade.model';
import { IUserProfile } from 'app/shared/model/user-profile.model';

export interface IApprentice {
  id?: string;
  stateAcademic?: keyof typeof StateAcademic;
  student?: IUserProfile;
  grade?: IGrade;
}

export const defaultValue: Readonly<IApprentice> = {};
