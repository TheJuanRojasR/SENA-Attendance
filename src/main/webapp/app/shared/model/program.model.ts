export interface IProgram {
  id?: string;
  name?: string;
  initials?: string;
  code?: string;
  trimesters?: number;
  status?: boolean;
}

export const defaultValue: Readonly<IProgram> = {};
