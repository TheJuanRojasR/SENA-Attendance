export interface IModality {
  id?: string;
  name?: string;
  isActive?: boolean;
}

export const defaultValue: Readonly<IModality> = {
  isActive: false,
};
