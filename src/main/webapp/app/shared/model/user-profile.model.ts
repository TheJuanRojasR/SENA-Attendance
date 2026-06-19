import { IDocumentType } from 'app/shared/model/document-type.model';
import { IUser } from 'app/shared/model/user.model';

export interface IUserProfile {
  id?: string;
  firstName?: string;
  middleName?: string | null;
  firstLastName?: string;
  secondLastName?: string | null;
  documentNumber?: string;
  phoneNumber?: string;
  user?: IUser;
  documentType?: IDocumentType;
}

export const defaultValue: Readonly<IUserProfile> = {};
