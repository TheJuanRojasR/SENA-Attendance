import { StateGrade } from 'app/shared/model/enumerations/state-grade.model';

export interface IRecentGrade {
  id?: string;
  code?: string;
  programName?: string;
  instructorName?: string;
  state?: keyof typeof StateGrade;
}

export interface IDashboard {
  kpis?: {
    totalUsers?: number;
    activeGrades?: number;
    totalPrograms?: number;
    totalModalities?: number;
  };
  recentGrades?: IRecentGrade[];
}

export const defaultValue: Readonly<IDashboard> = {};
