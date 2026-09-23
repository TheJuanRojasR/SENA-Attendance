import { createAsyncThunk, createSlice, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';
import axios from 'axios';

import { IClassSection, defaultValue } from 'app/shared/model/class-section.model';
import { EntityState, IQueryParams, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { cleanEntity } from 'app/shared/util/entity-utils';

interface IClassSectionState extends EntityState<IClassSection> {
  mine: IClassSection[];
}

const initialState: IClassSectionState = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: defaultValue,
  mine: [],
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/class-sections';

// Actions

export const getEntities = createAsyncThunk(
  'classSection/fetch_entity_list',
  async ({ page, size, sort }: IQueryParams) => {
    const requestUrl = `${apiUrl}?${sort ? `page=${page}&size=${size}&sort=${sort}&` : ''}cacheBuster=${Date.now()}`;
    return axios.get<IClassSection[]>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

// UC017: las materias del instructor autenticado; no está paginado y admite el filtro
// opcional gradeCode (coincidencia parcial). Reutilizado hoy por UC009 (tomar asistencia)
// para que el instructor elija una de sus materias sin esperar a que exista "Mis fichas".
export const getMine = createAsyncThunk(
  'classSection/fetch_mine',
  async (gradeCode?: string) => {
    const requestUrl = `${apiUrl}/mine${gradeCode ? `?gradeCode=${encodeURIComponent(gradeCode)}` : ''}`;
    return axios.get<IClassSection[]>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const getEntity = createAsyncThunk(
  'classSection/fetch_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IClassSection>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const createEntity = createAsyncThunk(
  'classSection/create_entity',
  async (entity: IClassSection, thunkAPI) => {
    const result = await axios.post<IClassSection>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const updateEntity = createAsyncThunk(
  'classSection/update_entity',
  async (entity: IClassSection, thunkAPI) => {
    const result = await axios.put<IClassSection>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const partialUpdateEntity = createAsyncThunk(
  'classSection/partial_update_entity',
  async (entity: IClassSection, thunkAPI) => {
    const result = await axios.patch<IClassSection>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const deleteEntity = createAsyncThunk(
  'classSection/delete_entity',
  async (id: string | number, thunkAPI) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.delete<IClassSection>(requestUrl);
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

// slice

export const ClassSectionSlice = createSlice({
  name: 'classSection',
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
      .addCase(getMine.fulfilled, (state, action) => {
        state.loading = false;
        state.mine = action.payload.data;
      })
      .addCase(getMine.pending, state => {
        state.loading = true;
      })
      .addCase(deleteEntity.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = {};
      })
      .addMatcher(isFulfilled(getEntities), (state, action) => {
        const { data, headers } = action.payload;

        return {
          ...state,
          loading: false,
          entities: data,
          totalItems: parseInt(headers['x-total-count'], 10),
        };
      })
      .addMatcher(isFulfilled(createEntity, updateEntity, partialUpdateEntity), (state, action) => {
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
      .addMatcher(isPending(createEntity, updateEntity, partialUpdateEntity, deleteEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      })
      .addMatcher(isRejected(getEntities, getEntity, getMine, createEntity, updateEntity, partialUpdateEntity, deleteEntity), state => {
        state.loading = false;
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = null;
      });
  },
});

export const { reset } = ClassSectionSlice.actions;

// Reducer
export default ClassSectionSlice.reducer;
