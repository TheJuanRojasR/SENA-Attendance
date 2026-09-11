import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import axios from 'axios';

import { IDashboard, defaultValue } from 'app/shared/model/dashboard.model';
import { serializeAxiosError } from 'app/shared/reducers/reducer.utils';

const apiUrl = 'api/dashboard';

// Actions

export const getAdminDashboard = createAsyncThunk(
  'dashboard/fetch_kpis',
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
      .addCase(getAdminDashboard.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(getAdminDashboard.fulfilled, (state, action) => {
        state.loading = false;
        state.dashboard = action.payload.data;
      })
      .addCase(getAdminDashboard.rejected, (state, action) => {
        state.loading = false;
        state.errorMessage = action.error.message ?? null;
      });
  },
});

export default DashboardSlice.reducer;
