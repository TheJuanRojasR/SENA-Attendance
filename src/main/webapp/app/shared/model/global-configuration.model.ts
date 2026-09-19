export interface IGlobalConfiguration {
  studentJustificationDays?: number;
  instructorResponseDays?: number;
  consecutiveAbsenceAlertThreshold?: number;
  accumulatedAbsenceAlertThreshold?: number;
}

export const defaultValue: Readonly<IGlobalConfiguration> = {};
