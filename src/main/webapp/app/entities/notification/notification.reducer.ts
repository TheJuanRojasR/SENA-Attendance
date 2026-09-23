import { createAsyncThunk, createSlice, isPending, isRejected } from '@reduxjs/toolkit';
import axios from 'axios';

import { INotificacion, defaultValue } from 'app/shared/model/notificacion.model';
import { IQueryParams, serializeAxiosError } from 'app/shared/reducers/reducer.utils';

export interface INotificationQueryParams extends IQueryParams {
  read?: boolean;
  type?: string;
  from?: string;
  to?: string;
}

const initialState = {
  loading: false,
  errorMessage: null as string | null,
  entities: [] as readonly INotificacion[],
  entity: defaultValue,
  unreadCount: 0,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/notifications';
const UNREAD_COUNT_HEADER = 'x-unread-count';

// Actions

// UC018, flujo básico paso 2: bandeja del usuario autenticado, más reciente primero; la
// cabecera X-Unread-Count viaja en cada respuesta de la lista (A3).
export const getEntities = createAsyncThunk(
  'notification/fetch_entity_list',
  async ({ page, size, sort, read, type, from, to }: INotificationQueryParams) => {
    const params = new URLSearchParams();
    if (page !== undefined) {
      params.set('page', `${page}`);
    }
    if (size !== undefined) {
      params.set('size', `${size}`);
    }
    if (sort) {
      params.set('sort', sort);
    }
    if (read !== undefined) {
      params.set('read', `${read}`);
    }
    if (type) {
      params.set('type', type);
    }
    if (from) {
      params.set('from', from);
    }
    if (to) {
      params.set('to', to);
    }
    return axios.get<INotificacion[]>(`${apiUrl}?${params.toString()}`);
  },
  { serializeError: serializeAxiosError },
);

// UC018, flujo básico paso 3: idempotente, individual.
export const markAsRead = createAsyncThunk(
  'notification/mark_read',
  async (id: string) => axios.patch<INotificacion>(`${apiUrl}/${id}/read`),
  { serializeError: serializeAxiosError },
);

// UC018, A1: marca todas las no leídas del usuario; sin cuerpo en la respuesta.
export const markAllAsRead = createAsyncThunk('notification/mark_all_read', async () => axios.patch<void>(`${apiUrl}/read-all`), {
  serializeError: serializeAxiosError,
});

export const NotificationSlice = createSlice({
  name: 'notification',
  initialState,
  reducers: {
    reset() {
      return initialState;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(getEntities.fulfilled, (state, action) => {
        const { data, headers } = action.payload;
        state.loading = false;
        state.entities = data;
        state.totalItems = parseInt(headers['x-total-count'], 10);
        state.unreadCount = parseInt(headers[UNREAD_COUNT_HEADER] ?? '0', 10);
      })
      .addCase(markAsRead.fulfilled, (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.entities = state.entities.map(notification =>
          notification.id === action.payload.data.id ? action.payload.data : notification,
        );
        if (state.unreadCount > 0) {
          state.unreadCount -= 1;
        }
      })
      .addCase(markAllAsRead.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
        state.entities = state.entities.map(notification => ({ ...notification, read: true }));
        state.unreadCount = 0;
      })
      .addMatcher(isPending(getEntities), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(markAsRead, markAllAsRead), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      })
      .addMatcher(isRejected(getEntities, markAsRead, markAllAsRead), (state, action) => {
        state.loading = false;
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message!;
      });
  },
});

export const { reset } = NotificationSlice.actions;

// Reducer
export default NotificationSlice.reducer;
