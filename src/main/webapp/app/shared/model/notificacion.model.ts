import dayjs from 'dayjs';

import { NotificacionEstado } from 'app/shared/model/enumerations/notificacion-estado.model';
import { NotificacionTipo } from 'app/shared/model/enumerations/notificacion-tipo.model';

export interface INotificacion {
  id?: string;
  tipo?: keyof typeof NotificacionTipo;
  mensaje?: string;
  estado?: keyof typeof NotificacionEstado;
  read?: boolean;
  referenceType?: string | null;
  referenceId?: string | null;
  createdDate?: dayjs.Dayjs;
}

export const defaultValue: Readonly<INotificacion> = {};
