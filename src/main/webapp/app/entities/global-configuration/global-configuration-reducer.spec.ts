import { beforeEach, describe, expect, it, vi } from 'vitest';

import { configureStore } from '@reduxjs/toolkit';
import axios from 'axios';

import reducer, { getConfigurations, partialUpdateEntity, reset } from './global-configuration.reducer';

describe('Entities reducer tests', () => {
  const initialState = {
    loading: false,
    errorMessage: null,
    entity: null,
    updating: false,
    updateSuccess: false,
  };

  function testMultipleTypes(types, payload, testFunction, error?) {
    types.forEach(e => {
      testFunction(reducer(undefined, { type: e, payload, error }));
    });
  }

  describe('Common', () => {
    it('should return the initial state', () => {
      expect(reducer(undefined, { type: '' })).toEqual(initialState);
    });
  });

  describe('Requests', () => {
    it('should set state to loading', () => {
      testMultipleTypes([getConfigurations.pending.type], {}, state => {
        expect(state).toMatchObject({
          errorMessage: null,
          loading: true,
        });
      });
    });

    it('should set state to updating', () => {
      testMultipleTypes([partialUpdateEntity.pending.type], {}, state => {
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
    it('should stop loading on getConfigurations failure', () => {
      testMultipleTypes(
        [getConfigurations.rejected.type],
        undefined,
        state => {
          expect(state).toMatchObject({
            loading: false,
          });
        },
        { message: 'error message' },
      );
    });

    it('should set a message in errorMessage on partialUpdateEntity failure', () => {
      testMultipleTypes(
        [partialUpdateEntity.rejected.type],
        undefined,
        state => {
          expect(state).toMatchObject({
            updateSuccess: false,
            updating: false,
          });
        },
        { message: 'error message' },
      );
    });
  });

  describe('Successes', () => {
    it('should fetch the configuration', () => {
      const payload = { data: { limitPerTrimester: 3 } };
      expect(
        reducer(undefined, {
          type: getConfigurations.fulfilled.type,
          payload,
        }),
      ).toEqual({
        ...initialState,
        loading: false,
        entity: payload.data,
      });
    });

    it('should partially update the configuration', () => {
      const payload = { data: { limitPerTrimester: 5 } };
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

    it('dispatches FETCH_GLOBALCONFIGURATION actions', async () => {
      const result = await getConfigurations()(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: getConfigurations.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(getConfigurations.fulfilled.match(result)).toBe(true);
    });

    it('dispatches PARTIAL_UPDATE_GLOBALCONFIGURATION actions', async () => {
      const arg = { limitPerTrimester: 3 };

      const result = await partialUpdateEntity(arg)(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: partialUpdateEntity.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(partialUpdateEntity.fulfilled.match(result)).toBe(true);
    });

    it('dispatches RESET actions', async () => {
      await store.dispatch(reset());
      expect(store.getState()).toEqual([expect.any(Object), expect.objectContaining(reset())]);
    });
  });
});
