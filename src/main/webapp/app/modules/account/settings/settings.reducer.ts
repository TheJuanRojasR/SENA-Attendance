import { Storage } from 'react-jhipster';

import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import axios from 'axios';

import { AppThunk } from 'app/config/store';
import { IUserProfile } from 'app/shared/model/user-profile.model';
import { getSession } from 'app/shared/reducers/authentication';
import { isProblemWithMessage } from 'app/shared/jhipster/problem-details';
import { serializeAxiosError } from 'app/shared/reducers/reducer.utils';

const initialState = {
  loading: false,
  profile: null as IUserProfile | null,
  errorMessage: null as string | null,
  successMessage: null as string | null,
  updateSuccess: false,
  updateFailure: false,
};

export type SettingsState = Readonly<typeof initialState>;

// Actions
const apiUrl = 'api/account';

export const getAccountProfile = createAsyncThunk('settings/get_account_profile', async () => {
  const requestUrl = `${apiUrl}/profile`;
  return axios.get<IUserProfile>(requestUrl);
});

export const saveAccountSettings: (account: any) => AppThunk = account => async dispatch => {
  await dispatch(updateAccount(account));

  if (Storage.session.get(`locale`)) {
    Storage.session.remove(`locale`);
  }

  dispatch(getSession());
};

export const updateAccount = createAsyncThunk('settings/update_account', async (account: any) => axios.patch<any>(apiUrl, account), {
  serializeError: serializeAxiosError,
});

export const SettingsSlice = createSlice({
  name: 'settings',
  initialState: initialState as SettingsState,
  reducers: {
    reset() {
      return initialState;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(getAccountProfile.pending, state => {
        state.loading = true;
      })
      .addCase(getAccountProfile.rejected, state => {
        state.loading = false;
      })
      .addCase(getAccountProfile.fulfilled, (state, action) => {
        state.loading = false;
        state.profile = action.payload.data;
      })
      .addCase(updateAccount.pending, state => {
        state.loading = true;
        state.errorMessage = null;
        state.updateSuccess = false;
      })
      .addCase(updateAccount.rejected, (state, action) => {
        const data = (action.error as any)?.response?.data;
        const problem = isProblemWithMessage(data) ? data : null;
        state.loading = false;
        state.updateSuccess = false;
        state.updateFailure = true;
        state.errorMessage = problem?.message ?? action.error?.message ?? null;
      })
      .addCase(updateAccount.fulfilled, state => {
        state.loading = false;
        state.updateSuccess = true;
        state.updateFailure = false;
        state.successMessage = 'settings.messages.success';
      });
  },
});

export const { reset } = SettingsSlice.actions;

// Reducer
export default SettingsSlice.reducer;
