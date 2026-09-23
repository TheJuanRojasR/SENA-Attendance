import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import axios from 'axios';

import { IDashboard, defaultValue } from 'app/shared/model/dashboard.model';
import { serializeAxiosError } from 'app/shared/reducers/reducer.utils';

const apiUrl = 'api/dashboard';

// Actions

// UC023: un único endpoint para los tres roles; la forma de la respuesta depende del rol
// autenticado, así que cada panel lee de aquí solo los campos de su propio rol.
export const getDashboard = createAsyncThunk(
  'dashboard/fetch_dashboard',
  async () => {
    const requestUrl = `${apiUrl}`;
    return axios.get<IDashboard>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

// Slice

export interface IDashboardState {
  loading: boolean;
  errorMessage: string | null;
  dashboard: IDashboard;
}

const initialState: IDashboardState = {
  loading: false,
  errorMessage: null,
  dashboard: defaultValue,
};

export const DashboardSlice = createSlice({
  name: 'dashboard',
  initialState,
  reducers: {},
  extraReducers(builder) {
    builder
      .addCase(getDashboard.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(getDashboard.fulfilled, (state, action) => {
        state.loading = false;
        state.dashboard = action.payload.data;
      })
      .addCase(getDashboard.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message ?? null;
      });
  },
});

export default DashboardSlice.reducer;
