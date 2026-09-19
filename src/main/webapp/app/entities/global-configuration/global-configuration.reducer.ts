import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import axios from 'axios';

import { IGlobalConfiguration } from 'app/shared/model/global-configuration.model';
import { isProblemWithMessage } from 'app/shared/jhipster/problem-details';
import { serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { cleanEntity } from 'app/shared/util/entity-utils';

const initialState = {
  loading: false,
  errorMessage: null as string | null,
  entity: null as IGlobalConfiguration | null,
  updating: false,
  updateSuccess: false,
};

const apiUrl = 'api/global-configurations';

// Actions

export const getConfigurations = createAsyncThunk('globalConfiguration/fetch_configurations', async () => {
  const requestUrl = `${apiUrl}`;
  return axios.get<IGlobalConfiguration>(requestUrl);
});

export const partialUpdateEntity = createAsyncThunk(
  'globalConfiguration/partial_update_entity',
  async (entity: IGlobalConfiguration, thunkAPI) => {
    const result = await axios.patch<IGlobalConfiguration>(`${apiUrl}`, cleanEntity(entity));
    thunkAPI.dispatch(getConfigurations());
    return result;
  },
  { serializeError: serializeAxiosError },
);

// slice

export const GlobalConfigurationSlice = createSlice({
  name: 'globalConfiguration',
  initialState,
  reducers: {
    reset() {
      return initialState;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(getConfigurations.pending, state => {
        state.errorMessage = null;
        state.loading = true;
      })
      .addCase(getConfigurations.rejected, state => {
        state.loading = false;
      })
      .addCase(getConfigurations.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addCase(partialUpdateEntity.pending, state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      })
      .addCase(partialUpdateEntity.rejected, (state, action) => {
        const data = (action.error as any)?.response?.data;
        const problem = isProblemWithMessage(data) ? data : null;
        state.updating = false;
        state.updateSuccess = false;
        state.errorMessage = problem?.message ?? action.error?.message ?? null;
      })
      .addCase(partialUpdateEntity.fulfilled, (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      });
  },
});

export const { reset } = GlobalConfigurationSlice.actions;

// Reducer
export default GlobalConfigurationSlice.reducer;
