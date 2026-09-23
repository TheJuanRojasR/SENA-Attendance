import { createAsyncThunk, createSlice, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';
import axios from 'axios';

import { IJustification, defaultValue } from 'app/shared/model/justification.model';
import { IQueryParams, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { cleanEntity } from 'app/shared/util/entity-utils';

const initialState = {
  loading: false,
  errorMessage: null as string | null,
  entities: [] as readonly IJustification[],
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/justifications';

// Actions

// Acotado por rol en el propio backend: el aprendiz solo ve las suyas, el Admin ve todas.
export const getEntities = createAsyncThunk(
  'justification/fetch_entity_list',
  async ({ page, size, sort }: IQueryParams) => {
    const requestUrl = `${apiUrl}?${sort ? `page=${page}&size=${size}&sort=${sort}&` : ''}cacheBuster=${Date.now()}`;
    return axios.get<IJustification[]>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const getEntity = createAsyncThunk(
  'justification/fetch_entity',
  async (id: string) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IJustification>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

// Crea la cabecera y una parte PENDIENTE por materia afectada (flujo básico UC011).
export const createEntity = createAsyncThunk(
  'justification/create_entity',
  async (entity: IJustification) => axios.post<IJustification>(apiUrl, cleanEntity(entity)),
  { serializeError: serializeAxiosError },
);

// A3: edición mientras todas las partes estén PENDIENTE; recalcula plazo y cupo en el servidor.
export const updateEntity = createAsyncThunk(
  'justification/update_entity',
  async (entity: IJustification) => axios.put<IJustification>(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  { serializeError: serializeAxiosError },
);

// A4: cancelación de una justificación pendiente; libera el cupo reservado.
export const cancelJustification = createAsyncThunk(
  'justification/cancel',
  async (id: string) => axios.patch<IJustification>(`${apiUrl}/cancelled`, { id }),
  { serializeError: serializeAxiosError },
);

export const JustificationSlice = createSlice({
  name: 'justification',
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
      .addMatcher(isFulfilled(createEntity, updateEntity, cancelJustification), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isPending(getEntities, getEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(createEntity, updateEntity, cancelJustification), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      })
      .addMatcher(isRejected(getEntities, getEntity, createEntity, updateEntity, cancelJustification), (state, action) => {
        state.loading = false;
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message!;
      });
  },
});

export const { reset } = JustificationSlice.actions;

// Reducer
export default JustificationSlice.reducer;
