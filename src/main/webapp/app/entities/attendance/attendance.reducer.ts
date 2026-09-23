import { createAsyncThunk, createSlice, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';
import axios from 'axios';

import { IAttendance, IAttendanceSessionRequest, IAttendanceSessionResponse, defaultValue } from 'app/shared/model/attendance.model';
import { IQueryParams, serializeAxiosError } from 'app/shared/reducers/reducer.utils';

export interface IAttendanceQueryParams extends IQueryParams {
  classSectionId?: string;
  date?: string;
  studentId?: string;
  stateAttendance?: string;
}

const initialState = {
  loading: false,
  errorMessage: null as string | null,
  entities: [] as readonly IAttendance[],
  entity: defaultValue,
  session: null as IAttendanceSessionResponse | null,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/attendances';

// Actions

// A1: historial acotado por rol en el propio backend (instructor -> sus materias; aprendiz -> las suyas).
export const getEntities = createAsyncThunk(
  'attendance/fetch_entity_list',
  async ({ page, size, sort, classSectionId, date, studentId, stateAttendance }: IAttendanceQueryParams) => {
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
    if (classSectionId) {
      params.set('classSectionId', classSectionId);
    }
    if (date) {
      params.set('date', date);
    }
    if (studentId) {
      params.set('studentId', studentId);
    }
    if (stateAttendance) {
      params.set('stateAttendance', stateAttendance);
    }
    return axios.get<IAttendance[]>(`${apiUrl}?${params.toString()}`);
  },
  { serializeError: serializeAxiosError },
);

export const getEntity = createAsyncThunk(
  'attendance/fetch_entity',
  async (id: string) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IAttendance>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

// Registra/edita la sesión completa (materia + fecha) de una sola vez; es idempotente.
export const saveSession = createAsyncThunk(
  'attendance/save_session',
  async (session: IAttendanceSessionRequest) => axios.put<IAttendanceSessionResponse>(`${apiUrl}/session`, session),
  { serializeError: serializeAxiosError },
);

// A2: edición puntual de un registro ya guardado (solo PRESENTE/FALLA).
export const updateAttendanceState = createAsyncThunk(
  'attendance/update_state',
  async ({ id, stateAttendance }: { id: string; stateAttendance: 'PRESENTE' | 'FALLA' }, thunkAPI) => {
    const result = await axios.patch<IAttendance>(`${apiUrl}/${id}`, { id, stateAttendance });
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const AttendanceSlice = createSlice({
  name: 'attendance',
  initialState,
  reducers: {
    reset() {
      return initialState;
    },
    clearSession(state) {
      state.session = null;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(getEntity.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addCase(saveSession.fulfilled, (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.session = action.payload.data;
      })
      .addMatcher(isFulfilled(getEntities), (state, action) => {
        const { data, headers } = action.payload;
        state.loading = false;
        state.entities = data;
        state.totalItems = parseInt(headers['x-total-count'], 10);
      })
      .addMatcher(isFulfilled(updateAttendanceState), (state, action) => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isPending(getEntities, getEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(saveSession, updateAttendanceState), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      })
      .addMatcher(isRejected(getEntities, getEntity, saveSession, updateAttendanceState), (state, action) => {
        state.loading = false;
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message!;
      });
  },
});

export const { reset, clearSession } = AttendanceSlice.actions;

// Reducer
export default AttendanceSlice.reducer;
