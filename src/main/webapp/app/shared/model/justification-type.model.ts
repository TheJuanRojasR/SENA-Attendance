import { State } from 'app/shared/model/enumerations/state.model';

export interface IJustificationType {
  id?: string;
  name?: string;
  limitPerTrimester?: number | null;
  state?: keyof typeof State;
}

export const defaultValue: Readonly<IJustificationType> = {};
