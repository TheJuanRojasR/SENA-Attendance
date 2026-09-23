import { StateGrade } from 'app/shared/model/enumerations/state-grade.model';

export interface IRecentGrade {
  id?: string;
  code?: string;
  programName?: string;
  instructorName?: string;
  state?: keyof typeof StateGrade;
}

export interface IDashboardClassSession {
  classSectionId?: string;
  subjectName?: string;
  gradeCode?: string;
  date?: string;
  startTime?: string;
  endTime?: string;
}

export interface IAttendanceSummary {
  present?: number;
  failure?: number;
  justified?: number;
  percentage?: number;
}

export interface IGradeFailure {
  gradeId?: string;
  gradeCode?: string;
  unexcusedFailures?: number;
  threshold?: number;
  missingToThreshold?: number;
}

export interface IJustificationDeadline {
  id?: string;
  subjectName?: string;
  deadline?: string;
  remainingBusinessDays?: number;
}

export interface IJustificationSummary {
  pending?: number;
  approved?: number;
  rejected?: number;
  withinCorrectionWindow?: IJustificationDeadline[];
}

export interface IEnrolledGrade {
  gradeId?: string;
  gradeCode?: string;
  programName?: string;
  subjects?: { id?: string; subjectName?: string }[];
}

// UC023: DashboardDTO.role() no se serializa, así que el JSON no trae discriminador de rol; la
// forma real de la respuesta depende del rol autenticado, resuelto en el propio cliente.
export interface IDashboard {
  // Panel de Administrador
  kpis?: {
    totalUsers?: number;
    activeGrades?: number;
    totalPrograms?: number;
    totalModalities?: number;
  };
  recentGrades?: IRecentGrade[];
  // Panel de Instructor
  pendingJustifications?: number;
  activeAlerts?: number;
  assignedSubjects?: number;
  assignedGrades?: number;
  assignedApprentices?: number;
  todayClasses?: IDashboardClassSession[];
  // Panel de Aprendiz
  attendance?: IAttendanceSummary | null;
  failuresByGrade?: IGradeFailure[];
  justifications?: IJustificationSummary;
  grades?: IEnrolledGrade[];
  // Compartidos por Instructor y Aprendiz
  upcomingClasses?: IDashboardClassSession[];
  trimesterMessage?: string | null;
}

export const defaultValue: Readonly<IDashboard> = {};
