import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import axios from 'axios';

import { FieldErrorVM, isProblemWithMessage } from 'app/shared/jhipster/problem-details';
import { serializeAxiosError } from 'app/shared/reducers/reducer.utils';

const initialState = {
  loading: false,
  registrationSuccess: false,
  registrationFailure: false,
  showModalRegister: false,
  errorMessage: null as string | null,
  fieldErrors: null as FieldErrorVM[] | null,
  successMessage: null as string | null,
};

export type RegisterState = Readonly<typeof initialState>;

// Actions

export const handleRegister = createAsyncThunk(
  'register/create_account',
  async (data: {
    email: string;
    password: string;
    langKey?: string;
    firstName: string;
    middleName?: string;
    firstLastName: string;
    secondLastName?: string;
    documentNumber: string;
    phoneNumber: string;
    documentTypeId: string;
  }) => axios.post<any>('api/register', data),
  { serializeError: serializeAxiosError },
);

export const RegisterSlice = createSlice({
  name: 'register',
  initialState: initialState as RegisterState,
  reducers: {
    reset() {
      return initialState;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(handleRegister.pending, state => {
        state.loading = true;
      })
      .addCase(handleRegister.rejected, (state, action) => {
        const data = (action.error as any)?.response?.data;
        const problem = isProblemWithMessage(data) ? data : null;
        return {
          ...initialState,
          registrationFailure: true,
          errorMessage: problem?.message ?? action.error.message!,
          fieldErrors: problem?.fieldErrors ?? null,
        };
      })
      .addCase(handleRegister.fulfilled, () => ({
        ...initialState,
        registrationSuccess: true,
        successMessage: 'register.messages.success',
      }));
  },
});

export const { reset } = RegisterSlice.actions;

// Reducer
export default RegisterSlice.reducer;
