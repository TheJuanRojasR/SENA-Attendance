export const MESSAGE_ALERT_HEADER_NAME = 'x-senaattendanceapp-alert';
export const MESSAGE_ERROR_HEADER_NAME = 'x-senaattendanceapp-error';
export const MESSAGE_PARAM_HEADER_NAME = 'x-senaattendanceapp-params';

export const AUTHENTICATION_TOKEN_KEY = 'project-authenticationToken';

export enum Authority {
  ADMIN = 'ROLE_ADMIN',
  USER = 'ROLE_USER',
  COORDINATOR = 'ROLE_COORDINATOR',
  INSTRUCTOR = 'ROLE_INSTRUCTOR',
  APPRENTICE = 'ROLE_APPRENTICE',
}
