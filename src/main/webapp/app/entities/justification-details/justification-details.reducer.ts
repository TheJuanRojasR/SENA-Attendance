import { createAsyncThunk, createSlice, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';
import axios from 'axios';

import { IJustificationDetails, defaultValue } from 'app/shared/model/justification-details.model';
import { IQueryParams, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { cleanEntity } from 'app/shared/util/entity-utils';

export interface IPendingQueryParams extends IQueryParams {
  stateJustification?: string;
  classSectionId?: string;
  createdFrom?: string;
  createdTo?: string;
}

const initialState = {
  loading: false,
  errorMessage: null as string | null,
  entities: [] as readonly IJustificationDetails[],
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/justification-details';

// Actions

// UC010: bandeja del instructor (solo sus materias) o del Admin (todas); por defecto solo las
// partes PENDIENTE, más reciente primero.
export const getPending = createAsyncThunk(
  'justificationDetails/fetch_pending',
  async ({ page, size, sort, stateJustification, classSectionId, createdFrom, createdTo }: IPendingQueryParams) => {
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
    if (stateJustification) {
      params.set('stateJustification', stateJustification);
    }
    if (classSectionId) {
      params.set('classSectionId', classSectionId);
    }
    if (createdFrom) {
      params.set('createdFrom', createdFrom);
    }
    if (createdTo) {
      params.set('createdTo', createdTo);
    }
    return axios.get<IJustificationDetails[]>(`${apiUrl}/pending?${params.toString()}`);
  },
  { serializeError: serializeAxiosError },
);

// Detalle con el soporte de la cabecera (justification.evidence); acotado por rol en el backend.
export const getEntity = createAsyncThunk(
  'justificationDetails/fetch_entity',
  async (id: string) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IJustificationDetails>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

// A5 de UC011: el aprendiz subsana una parte rechazada (copia texto/archivo de corrección).
export const partialUpdateEntity = createAsyncThunk(
  'justificationDetails/partial_update_entity',
  async (entity: IJustificationDetails) => axios.patch<IJustificationDetails>(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  { serializeError: serializeAxiosError },
);

// Flujo básico de UC010: aprobar o rechazar una parte pendiente de una de las materias del
// instructor autenticado (o cualquiera, si es Admin).
export const decideEntity = createAsyncThunk(
  'justificationDetails/decide',
  async ({
    id,
    stateJustification,
    rejectionReason,
    outOfTimeReason,
  }: {
    id: string;
    stateJustification: 'ACEPTADA' | 'RECHAZADA';
    rejectionReason?: string;
    outOfTimeReason?: string;
  }) => axios.patch<IJustificationDetails>(`${apiUrl}/${id}/decision`, { stateJustification, rejectionReason, outOfTimeReason }),
  { serializeError: serializeAxiosError },
);

export const JustificationDetailsSlice = createSlice({
  name: 'justificationDetails',
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
      .addMatcher(isFulfilled(getPending), (state, action) => {
        const { data, headers } = action.payload;
        state.loading = false;
        state.entities = data;
        state.totalItems = parseInt(headers['x-total-count'], 10);
      })
      .addMatcher(isFulfilled(partialUpdateEntity, decideEntity), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isPending(getPending, getEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(partialUpdateEntity, decideEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      })
      .addMatcher(isRejected(getPending, getEntity, partialUpdateEntity, decideEntity), (state, action) => {
        state.loading = false;
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = action.error.message!;
      });
  },
});

export const { reset } = JustificationDetailsSlice.actions;

// Reducer
export default JustificationDetailsSlice.reducer;
