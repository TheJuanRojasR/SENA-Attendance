import { createAsyncThunk, createSlice, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';
import axios from 'axios';

import { IApprentice, defaultValue } from 'app/shared/model/apprentice.model';
import { IQueryParams, serializeAxiosError } from 'app/shared/reducers/reducer.utils';

export interface IApprenticeQueryParams extends IQueryParams {
  gradeId?: string;
  documentNumber?: string;
  name?: string;
  stateAcademic?: string;
}

const initialState = {
  loading: false,
  errorMessage: null as string | null,
  entities: [] as readonly IApprentice[],
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/apprentices';

// Actions

export const getEntities = createAsyncThunk(
  'apprentice/fetch_entity_list',
  async ({ page, size, sort, gradeId, documentNumber, name, stateAcademic }: IApprenticeQueryParams) => {
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
    if (gradeId) {
      params.set('gradeId', gradeId);
    }
    if (documentNumber) {
      params.set('documentNumber', documentNumber);
    }
    if (name) {
      params.set('name', name);
    }
    if (stateAcademic) {
      params.set('stateAcademic', stateAcademic);
    }
    return axios.get<IApprentice[]>(`${apiUrl}?${params.toString()}`);
  },
  { serializeError: serializeAxiosError },
);

export const getEntity = createAsyncThunk(
  'apprentice/fetch_entity',
  async (id: string) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IApprentice>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

// UC008: el alta identifica al aprendiz por documento (el servidor resuelve el perfil) y siempre
// matricula; no existe PUT/PATCH/DELETE genérico para esta entidad, solo alta, baja y consulta.
export const enrollApprentice = createAsyncThunk(
  'apprentice/enroll',
  async ({ documentNumber, gradeId }: { documentNumber: string; gradeId: string }, thunkAPI) => {
    const result = await axios.post<IApprentice>(apiUrl, { documentNumber, grade: { id: gradeId } });
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

// El id es el del vínculo (Apprentice), no el del aprendiz. El backend responde 204 sin cuerpo
// cuando el registro se elimina (sin asistencias) y 200 con el DTO cuando se conserva con el
// motivo como stateAcademic (con asistencias) — ambos casos son un enroll/unlink fulfilled normal.
export const unlinkApprentice = createAsyncThunk(
  'apprentice/unlink',
  async ({ id, reason }: { id: string; reason: string }, thunkAPI) => {
    const result = await axios.patch<IApprentice | ''>(`${apiUrl}/unlinked`, { id, reason });
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const ApprenticeSlice = createSlice({
  name: 'apprentice',
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
      .addMatcher(isFulfilled(enrollApprentice, unlinkApprentice), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data || {};
      })
      .addMatcher(isPending(getEntities, getEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(enrollApprentice, unlinkApprentice), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      })
      .addMatcher(isRejected(getEntities, getEntity, enrollApprentice, unlinkApprentice), (state, action) => {
        state.loading = false;
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message!;
      });
  },
});

export const { reset } = ApprenticeSlice.actions;

// Reducer
export default ApprenticeSlice.reducer;
