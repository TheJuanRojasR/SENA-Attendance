import { describe, expect, it, vi } from 'vitest';

import { configureStore } from '@reduxjs/toolkit';
import axios from 'axios';

import { IJustificationDetails, defaultValue } from 'app/shared/model/justification-details.model';

import reducer, { decideEntity, getEntity, getPending, partialUpdateEntity, reset } from './justification-details.reducer';

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
    entities: [] as readonly IJustificationDetails[],
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
      testMultipleTypes([getPending.pending.type, getEntity.pending.type], {}, state => {
        expect(state).toMatchObject({
          errorMessage: null,
          updateSuccess: false,
          loading: true,
        });
      });
    });

    it('should set state to updating', () => {
      testMultipleTypes([partialUpdateEntity.pending.type, decideEntity.pending.type], {}, state => {
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
        [getPending.rejected.type, getEntity.rejected.type, partialUpdateEntity.rejected.type, decideEntity.rejected.type],
        'some message',
        state => {
          expect(state).toMatchObject({
            errorMessage: 'error message',
            updateSuccess: false,
            updating: false,
            loading: false,
          });
        },
        {
          message: 'error message',
        },
      );
    });
  });

  describe('Successes', () => {
    it('should fetch pending entities', () => {
      const payload = { data: [{ 1: 'fake1' }, { 2: 'fake2' }], headers: { 'x-total-count': 123 } };
      expect(
        reducer(undefined, {
          type: getPending.fulfilled.type,
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

    it('should correct/decide entity', () => {
      const payload = { data: 'fake payload' };
      expect(
        reducer(undefined, {
          type: partialUpdateEntity.fulfilled.type,
          payload,
        }),
      ).toEqual({
        ...initialState,
        updating: false,
        updateSuccess: true,
        entity: payload.data,
      });
    });

    it('should apply a decision', () => {
      const payload = { data: { id: '1', stateJustification: 'ACEPTADA' } };
      expect(
        reducer(undefined, {
          type: decideEntity.fulfilled.type,
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
      axios.patch = vi.fn().mockResolvedValue(resolvedObject);
    });

    it('dispatches FETCH_PENDING actions', async () => {
      const arg = {};

      const result = await getPending(arg)(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: getPending.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(getPending.fulfilled.match(result)).toBe(true);
    });

    it('dispatches FETCH_JUSTIFICATIONDETAILS actions', async () => {
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

    it('dispatches PARTIAL_UPDATE_JUSTIFICATIONDETAILS actions', async () => {
      const arg = { id: 'ABC' };

      const result = await partialUpdateEntity(arg)(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: partialUpdateEntity.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(partialUpdateEntity.fulfilled.match(result)).toBe(true);
    });

    it('dispatches DECIDE actions', async () => {
      const arg: Parameters<typeof decideEntity>[0] = { id: 'ABC', stateJustification: 'ACEPTADA' };

      const result = await decideEntity(arg)(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: decideEntity.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(decideEntity.fulfilled.match(result)).toBe(true);
    });

    it('dispatches RESET actions', async () => {
      await store.dispatch(reset());
      expect(store.getState()).toEqual([expect.any(Object), expect.objectContaining(reset())]);
    });
  });
});
