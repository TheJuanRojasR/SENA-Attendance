import { describe, expect, it, vi } from 'vitest';

import axios from 'axios';

import { defaultValue } from 'app/shared/model/dashboard.model';

import reducer, { getDashboard } from './dashboard.reducer';

describe('Dashboard reducer tests', () => {
  const initialState = {
    loading: false,
    errorMessage: null,
    dashboard: defaultValue,
  };

  it('should return the initial state', () => {
    expect(reducer(undefined, { type: '' })).toEqual(initialState);
  });

  it('should set state to loading', () => {
    expect(reducer(initialState, { type: getDashboard.pending.type })).toMatchObject({
      loading: true,
      errorMessage: null,
    });
  });

  it('should store the dashboard payload', () => {
    const payload = { data: { kpis: { totalUsers: 5 } } };
    expect(reducer(initialState, { type: getDashboard.fulfilled.type, payload })).toEqual({
      ...initialState,
      loading: false,
      dashboard: payload.data,
    });
  });

  it('should set a message in errorMessage', () => {
    expect(reducer(initialState, { type: getDashboard.rejected.type, error: { message: 'error message' } })).toMatchObject({
      loading: false,
      errorMessage: 'error message',
    });
  });

  describe('Actions', () => {
    const resolvedObject = { value: 'whatever' };
    const getState = vi.fn();
    const dispatch = vi.fn();
    const extra = {};

    it('dispatches FETCH_DASHBOARD actions', async () => {
      axios.get = vi.fn().mockResolvedValue(resolvedObject);

      const result = await getDashboard()(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: getDashboard.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(getDashboard.fulfilled.match(result)).toBe(true);
    });
  });
});
