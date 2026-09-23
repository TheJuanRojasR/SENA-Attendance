import { describe, expect, it, vi } from 'vitest';

import { configureStore } from '@reduxjs/toolkit';
import axios from 'axios';

import { defaultValue } from 'app/shared/model/attendance.model';

import reducer, { getEntities, getEntity, reset, saveSession, updateAttendanceState } from './attendance.reducer';

describe('Entities reducer tests', () => {
  function isEmpty(element): boolean {
    if (Array.isArray(element)) {
      return element.length === 0;
    }
    return Object.keys(element).length === 0;
  }

  const initialState = {
    loading: false,
    errorMessage: null,
    entities: [],
    entity: defaultValue,
    session: null,
    totalItems: 0,
    updating: false,
    updateSuccess: false,
  };

  function testInitialState(state) {
    expect(state).toMatchObject({
      loading: false,
      errorMessage: null,
      updating: false,
      updateSuccess: false,
    });
    expect(isEmpty(state.entities));
    expect(isEmpty(state.entity));
  }

  function testMultipleTypes(types, payload, testFunction, error?) {
    types.forEach(e => {
      testFunction(reducer(undefined, { type: e, payload, error }));
    });
  }

  describe('Common', () => {
    it('should return the initial state', () => {
      testInitialState(reducer(undefined, { type: '' }));
    });
  });

  describe('Requests', () => {
    it('should set state to loading', () => {
      testMultipleTypes([getEntities.pending.type, getEntity.pending.type], {}, state => {
        expect(state).toMatchObject({
          errorMessage: null,
          updateSuccess: false,
          loading: true,
        });
      });
    });

    it('should set state to updating', () => {
      testMultipleTypes([saveSession.pending.type, updateAttendanceState.pending.type], {}, state => {
        expect(state).toMatchObject({
          errorMessage: null,
          updateSuccess: false,
          updating: true,
        });
      });
    });

    it('should reset the state', () => {
      expect(reducer({ ...initialState, loading: true }, reset())).toEqual({
        ...initialState,
      });
    });
  });

  describe('Failures', () => {
    it('should set a message in errorMessage', () => {
      testMultipleTypes(
        [getEntities.rejected.type, getEntity.rejected.type, saveSession.rejected.type, updateAttendanceState.rejected.type],
        'some message',
        state => {
          expect(state).toMatchObject({
            loading: false,
            updateSuccess: false,
            updating: false,
          });
        },
        {
          message: 'error message',
        },
      );
    });
  });

  describe('Successes', () => {
    it('should fetch all entities', () => {
      const payload = { data: [{ 1: 'fake1' }, { 2: 'fake2' }], headers: { 'x-total-count': 123 } };
      expect(
        reducer(undefined, {
          type: getEntities.fulfilled.type,
          payload,
        }),
      ).toEqual({
        ...initialState,
        loading: false,
        totalItems: payload.headers['x-total-count'],
        entities: payload.data,
      });
    });

    it('should fetch a single entity', () => {
      const payload = { data: { 1: 'fake1' } };
      expect(
        reducer(undefined, {
          type: getEntity.fulfilled.type,
          payload,
        }),
      ).toEqual({
        ...initialState,
        loading: false,
        entity: payload.data,
      });
    });

    it('should save a session', () => {
      const payload = { data: { records: [], complete: true, enrolledCount: 0, recordedCount: 0 } };
      expect(
        reducer(undefined, {
          type: saveSession.fulfilled.type,
          payload,
        }),
      ).toEqual({
        ...initialState,
        updating: false,
        updateSuccess: true,
        session: payload.data,
      });
    });

    it('should update the attendance state (A2)', () => {
      const payload = { data: { id: '1', stateAttendance: 'FALLA' } };
      expect(
        reducer(undefined, {
          type: updateAttendanceState.fulfilled.type,
          payload,
        }),
      ).toEqual({
        ...initialState,
        updating: false,
        updateSuccess: true,
        entity: payload.data,
      });
    });
  });

  describe('Actions', () => {
    let store;

    const resolvedObject = { value: 'whatever' };
    const getState = vi.fn();
    const dispatch = vi.fn();
    const extra = {};
    beforeEach(() => {
      store = configureStore({
        reducer: (state = [], action) => [...state, action],
      });
      axios.get = vi.fn().mockResolvedValue(resolvedObject);
      axios.put = vi.fn().mockResolvedValue(resolvedObject);
      axios.patch = vi.fn().mockResolvedValue(resolvedObject);
    });

    it('dispatches FETCH_ATTENDANCE_LIST actions', async () => {
      const arg = {};

      const result = await getEntities(arg)(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: getEntities.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(getEntities.fulfilled.match(result)).toBe(true);
    });

    it('dispatches FETCH_ATTENDANCE actions', async () => {
      const arg = '42666';

      const result = await getEntity(arg)(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: getEntity.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(getEntity.fulfilled.match(result)).toBe(true);
    });

    it('dispatches SAVE_SESSION actions', async () => {
      const arg = { classSection: { id: 'cs1' }, date: '2026-09-15', attendances: [] };

      const result = await saveSession(arg)(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: saveSession.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(saveSession.fulfilled.match(result)).toBe(true);
    });

    it('dispatches UPDATE_ATTENDANCE_STATE actions', async () => {
      const arg = { id: 'e7ad7451-b4db-4803-807a-4c14806448f5', stateAttendance: 'FALLA' as const };

      const result = await updateAttendanceState(arg)(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: updateAttendanceState.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(updateAttendanceState.fulfilled.match(result)).toBe(true);
    });

    it('dispatches RESET actions', async () => {
      await store.dispatch(reset());
      expect(store.getState()).toEqual([expect.any(Object), expect.objectContaining(reset())]);
    });
  });
});
