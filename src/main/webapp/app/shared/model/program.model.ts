export interface IProgram {
  id?: string;
  name?: string;
  initials?: string;
  code?: string;
  trimesters?: number;
}

export const defaultValue: Readonly<IProgram> = {};
