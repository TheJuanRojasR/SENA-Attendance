import { describe, expect, it, vi } from 'vitest';

import { configureStore } from '@reduxjs/toolkit';
import axios from 'axios';

import { defaultValue } from 'app/shared/model/apprentice.model';

import reducer, { enrollApprentice, getEntities, getEntity, reset, unlinkApprentice } from './apprentice.reducer';

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
      testMultipleTypes([enrollApprentice.pending.type, unlinkApprentice.pending.type], {}, state => {
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
        [getEntities.rejected.type, getEntity.rejected.type, enrollApprentice.rejected.type, unlinkApprentice.rejected.type],
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

    it('should enroll an apprentice', () => {
      const payload = { data: 'fake payload' };
      expect(
        reducer(undefined, {
          type: enrollApprentice.fulfilled.type,
          payload,
        }),
      ).toEqual({
        ...initialState,
        updating: false,
        updateSuccess: true,
        entity: payload.data,
      });
    });

    it('should unlink an apprentice and keep the returned entity when the history is kept (200)', () => {
      const payload = { data: { id: '1', stateAcademic: 'RETIRO_VOLUNTARIO' } };
      expect(
        reducer(undefined, {
          type: unlinkApprentice.fulfilled.type,
          payload,
        }),
      ).toEqual({
        ...initialState,
        updating: false,
        updateSuccess: true,
        entity: payload.data,
      });
    });

    it('should unlink an apprentice with no body when the record was deleted (204)', () => {
      const payload = { data: '' };
      const toTest = reducer(undefined, {
        type: unlinkApprentice.fulfilled.type,
        payload,
      });
      expect(toTest).toMatchObject({
        updating: false,
        updateSuccess: true,
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
      axios.post = vi.fn().mockResolvedValue(resolvedObject);
      axios.patch = vi.fn().mockResolvedValue(resolvedObject);
    });

    it('dispatches FETCH_APPRENTICE_LIST actions', async () => {
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

    it('dispatches FETCH_APPRENTICE_LIST actions with filters', async () => {
      const arg = { page: 0, size: 20, sort: 'id,asc', gradeId: 'g1', documentNumber: '123', name: 'ana', stateAcademic: 'MATRICULADO' };

      const result = await getEntities(arg)(dispatch, getState, extra);

      expect(getEntities.fulfilled.match(result)).toBe(true);
    });

    it('dispatches FETCH_APPRENTICE actions', async () => {
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

    it('dispatches ENROLL_APPRENTICE actions', async () => {
      const arg = { documentNumber: '1029384756', gradeId: 'f1537a32-8765-4478-a7fa-674e3f6ae8c1' };

      const result = await enrollApprentice(arg)(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: enrollApprentice.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(enrollApprentice.fulfilled.match(result)).toBe(true);
    });

    it('dispatches UNLINK_APPRENTICE actions', async () => {
      const arg = { id: 'f1537a32-8765-4478-a7fa-674e3f6ae8c1', reason: 'RETIRO_VOLUNTARIO' };

      const result = await unlinkApprentice(arg)(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: unlinkApprentice.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(unlinkApprentice.fulfilled.match(result)).toBe(true);
    });

    it('dispatches RESET actions', async () => {
      await store.dispatch(reset());
      expect(store.getState()).toEqual([expect.any(Object), expect.objectContaining(reset())]);
    });
  });
});
