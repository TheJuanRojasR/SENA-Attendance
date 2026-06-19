import { IGrade } from 'app/shared/model/grade.model';
import { IUserProfile } from 'app/shared/model/user-profile.model';

export interface IClassSection {
  id?: string;
  subjectName?: string;
  isActive?: boolean;
  instructor?: IUserProfile;
  grade?: IGrade;
}

export const defaultValue: Readonly<IClassSection> = {
  isActive: false,
};
