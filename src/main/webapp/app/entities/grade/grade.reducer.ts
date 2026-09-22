import { createAsyncThunk, isFulfilled, isPending } from '@reduxjs/toolkit';
import axios from 'axios';

import { IGrade, defaultValue } from 'app/shared/model/grade.model';
import { EntityState, IQueryParams, createEntitySlice, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { cleanEntity } from 'app/shared/util/entity-utils';

const initialState: EntityState<IGrade> = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/grades';

// Actions

export const getEntities = createAsyncThunk(
  'grade/fetch_entity_list',
  async ({ page, size, sort }: IQueryParams) => {
    const requestUrl = `${apiUrl}?${sort ? `page=${page}&size=${size}&sort=${sort}&` : ''}cacheBuster=${Date.now()}`;
    return axios.get<IGrade[]>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const getEntity = createAsyncThunk(
  'grade/fetch_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IGrade>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const getActiveEntities = createAsyncThunk(
  'grade/fetch_active_entities',
  async () => {
    const requestUrl = `${apiUrl}/active`;
    return axios.get<IGrade[]>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const createEntity = createAsyncThunk(
  'grade/create_entity',
  async (entity: IGrade, thunkAPI) => {
    const result = await axios.post<IGrade>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const updateEntity = createAsyncThunk(
  'grade/update_entity',
  async (entity: IGrade, thunkAPI) => {
    const result = await axios.put<IGrade>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const partialUpdateEntity = createAsyncThunk(
  'grade/partial_update_entity',
  async (entity: IGrade, thunkAPI) => {
    const result = await axios.patch<IGrade>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const deleteEntity = createAsyncThunk(
  'grade/delete_entity',
  async (id: string | number, thunkAPI) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.delete<IGrade>(requestUrl);
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const postponeGrade = createAsyncThunk(
  'grade/postpone_entity',
  async (id: string, thunkAPI) => {
    const result = await axios.patch<IGrade>(`${apiUrl}/postponed`, { id });
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const resumeGrade = createAsyncThunk(
  'grade/resume_entity',
  async (id: string, thunkAPI) => {
    const result = await axios.patch<IGrade>(`${apiUrl}/resumed`, { id });
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const cancelGrade = createAsyncThunk(
  'grade/cancel_entity',
  async (id: string, thunkAPI) => {
    const result = await axios.patch<IGrade>(`${apiUrl}/cancelled`, { id });
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

// slice

export const GradeSlice = createEntitySlice({
  name: 'grade',
  initialState,
  extraReducers(builder) {
    builder
      .addCase(getEntity.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
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
      .addMatcher(isFulfilled(getActiveEntities), (state, action) => {
        state.loading = false;
        state.entities = action.payload.data;
      })
      .addMatcher(
        isFulfilled(createEntity, updateEntity, partialUpdateEntity, postponeGrade, resumeGrade, cancelGrade),
        (state, action) => {
          state.updating = false;
          state.loading = false;
          state.updateSuccess = true;
          state.entity = action.payload.data;
        },
      )
      .addMatcher(isPending(getEntities, getEntity, getActiveEntities), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(
        isPending(createEntity, updateEntity, partialUpdateEntity, deleteEntity, postponeGrade, resumeGrade, cancelGrade),
        state => {
          state.errorMessage = null;
          state.updateSuccess = false;
          state.updating = true;
        },
      );
  },
});

export const { reset } = GradeSlice.actions;

// Reducer
export default GradeSlice.reducer;
