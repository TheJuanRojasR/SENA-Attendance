export interface IDocumentType {
  id?: string;
  name?: string;
  initials?: string;
  isActive?: boolean;
}

export const defaultValue: Readonly<IDocumentType> = {};
