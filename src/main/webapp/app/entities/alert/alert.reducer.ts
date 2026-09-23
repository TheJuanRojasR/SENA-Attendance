import { createAsyncThunk, createSlice, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';
import axios from 'axios';

import { IAlerta, defaultValue } from 'app/shared/model/alerta.model';
import { IQueryParams, serializeAxiosError } from 'app/shared/reducers/reducer.utils';

export interface IAlertQueryParams extends IQueryParams {
  type?: string;
  state?: string;
  gradeId?: string;
  studentId?: string;
  from?: string;
  to?: string;
}

const initialState = {
  loading: false,
  errorMessage: null as string | null,
  entities: [] as readonly IAlerta[],
  entity: defaultValue,
  studentHistory: [] as readonly IAlerta[],
  studentHistoryTotalItems: 0,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/alerts';

// Actions

// UC013, A1: bandeja acotada por rol en el propio backend (instructor -> sus materias/fichas;
// Admin -> todas). Por defecto ordenada por generatedAt,desc.
export const getEntities = createAsyncThunk(
  'alert/fetch_entity_list',
  async ({ page, size, sort, type, state, gradeId, studentId, from, to }: IAlertQueryParams) => {
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
    if (type) {
      params.set('type', type);
    }
    if (state) {
      params.set('state', state);
    }
    if (gradeId) {
      params.set('gradeId', gradeId);
    }
    if (studentId) {
      params.set('studentId', studentId);
    }
    if (from) {
      params.set('from', from);
    }
    if (to) {
      params.set('to', to);
    }
    return axios.get<IAlerta[]>(`${apiUrl}?${params.toString()}`);
  },
  { serializeError: serializeAxiosError },
);

export const getEntity = createAsyncThunk(
  'alert/fetch_entity',
  async (id: string) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IAlerta>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

// UC013, A5: historial de un aprendiz, acotado al alcance de lectura del solicitante.
export const getStudentHistory = createAsyncThunk(
  'alert/fetch_student_history',
  async ({ studentId, page, size, sort }: { studentId: string } & IQueryParams) => {
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
    return axios.get<IAlerta[]>(`${apiUrl}/students/${studentId}?${params.toString()}`);
  },
  { serializeError: serializeAxiosError },
);

// UC013, A2: idempotente (solo aplica desde NO_LEIDA).
export const markAsRead = createAsyncThunk('alert/mark_read', async (id: string) => axios.patch<IAlerta>(`${apiUrl}/${id}/read`), {
  serializeError: serializeAxiosError,
});

// UC013, A3: exige la observación del seguimiento; una alerta resuelta responde error.alertAlreadyResolved.
export const attend = createAsyncThunk(
  'alert/attend',
  async ({ id, observation }: { id: string; observation: string }) => axios.patch<IAlerta>(`${apiUrl}/${id}/attend`, { observation }),
  { serializeError: serializeAxiosError },
);

export const AlertSlice = createSlice({
  name: 'alert',
  initialState,
  reducers: {
    reset() {
      return initialState;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(getEntity.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addMatcher(isFulfilled(getEntities), (state, action) => {
        const { data, headers } = action.payload;
        state.loading = false;
        state.entities = data;
        state.totalItems = parseInt(headers['x-total-count'], 10);
      })
      .addMatcher(isFulfilled(getStudentHistory), (state, action) => {
        const { data, headers } = action.payload;
        state.loading = false;
        state.studentHistory = data;
        state.studentHistoryTotalItems = parseInt(headers['x-total-count'], 10);
      })
      .addMatcher(isFulfilled(markAsRead, attend), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isPending(getEntities, getEntity, getStudentHistory), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(markAsRead, attend), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      })
      .addMatcher(isRejected(getEntities, getEntity, getStudentHistory, markAsRead, attend), (state, action) => {
        state.loading = false;
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message!;
      });
  },
});

export const { reset } = AlertSlice.actions;

// Reducer
export default AlertSlice.reducer;
