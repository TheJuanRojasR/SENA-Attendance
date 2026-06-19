export interface ITimeSlot {
  id?: string;
  name?: string;
  isActive?: boolean;
  startTime?: string;
  endTime?: string;
}

export const defaultValue: Readonly<ITimeSlot> = {
  isActive: false,
};
