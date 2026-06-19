export interface IGlobalConfiguration {
  id?: string;
  studentJustificationDays?: number;
  instructorResponseDays?: number;
  lateArrivalsToFail?: number;
  maxPostponementJustifications?: number;
  standardTrimesterMonths?: number;
}

export const defaultValue: Readonly<IGlobalConfiguration> = {};
