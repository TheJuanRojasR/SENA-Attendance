import { describe, expect, it, vi } from 'vitest';

import { configureStore } from '@reduxjs/toolkit';
import axios from 'axios';

import { INotificacion, defaultValue } from 'app/shared/model/notificacion.model';

import reducer, { getEntities, markAllAsRead, markAsRead, reset } from './notification.reducer';

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
    entities: [] as readonly INotificacion[],
    entity: defaultValue,
    unreadCount: 0,
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
      testMultipleTypes([getEntities.pending.type], {}, state => {
        expect(state).toMatchObject({
          errorMessage: null,
          updateSuccess: false,
          loading: true,
        });
      });
    });

    it('should set state to updating', () => {
      testMultipleTypes([markAsRead.pending.type, markAllAsRead.pending.type], {}, state => {
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
        [getEntities.rejected.type, markAsRead.rejected.type, markAllAsRead.rejected.type],
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
    it('should fetch all entities and the unread count', () => {
      const payload = {
        data: [{ id: '1', read: false }],
        headers: { 'x-total-count': 3, 'x-unread-count': 2 },
      };
      expect(
        reducer(undefined, {
          type: getEntities.fulfilled.type,
          payload,
        }),
      ).toEqual({
        ...initialState,
        loading: false,
        totalItems: payload.headers['x-total-count'],
        unreadCount: payload.headers['x-unread-count'],
        entities: payload.data,
      });
    });

    it('should mark one notification as read and decrement the unread count', () => {
      const payload = { data: { id: '1', read: true } };
      const state = reducer(
        { ...initialState, entities: [{ id: '1', read: false }], unreadCount: 1 },
        {
          type: markAsRead.fulfilled.type,
          payload,
        },
      );
      expect(state).toMatchObject({
        updating: false,
        updateSuccess: true,
        unreadCount: 0,
        entities: [{ id: '1', read: true }],
      });
    });

    it('should mark every notification as read and reset the unread count', () => {
      const state = reducer(
        {
          ...initialState,
          entities: [
            { id: '1', read: false },
            { id: '2', read: false },
          ],
          unreadCount: 2,
        },
        { type: markAllAsRead.fulfilled.type },
      );
      expect(state).toMatchObject({
        updating: false,
        updateSuccess: true,
        unreadCount: 0,
        entities: [
          { id: '1', read: true },
          { id: '2', read: true },
        ],
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

    it('dispatches FETCH_NOTIFICATION_LIST actions', async () => {
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

    it('dispatches MARK_READ actions', async () => {
      const arg = 'abc';

      const result = await markAsRead(arg)(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: markAsRead.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(markAsRead.fulfilled.match(result)).toBe(true);
    });

    it('dispatches MARK_ALL_READ actions', async () => {
      const result = await markAllAsRead()(dispatch, getState, extra);

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({
          type: markAllAsRead.pending.type,
          meta: expect.objectContaining({ requestStatus: 'pending' }),
        }),
      );
      expect(markAllAsRead.fulfilled.match(result)).toBe(true);
    });

    it('dispatches RESET actions', async () => {
      await store.dispatch(reset());
      expect(store.getState()).toEqual([expect.any(Object), expect.objectContaining(reset())]);
    });
  });
});
