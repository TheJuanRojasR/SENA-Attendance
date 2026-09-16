import { Storage } from 'react-jhipster';

import axios, { type AxiosError } from 'axios';

import { AUTHENTICATION_TOKEN_KEY } from 'app/shared/jhipster/constants';

const TIMEOUT = 1 * 60 * 1000;
axios.defaults.timeout = TIMEOUT;
axios.defaults.baseURL = SERVER_API_URL;

// `api/account` and `api/authenticate` handle their own 401s in the authentication slice
// (login failure, session-check failure); routing those through onUnauthenticated too would
// fire a second, conflicting state reset for the same request.
const URLS_HANDLED_ELSEWHERE = ['api/account', 'api/authenticate'];

const isTokenExpired = () => {
  const token = Storage.local.get(AUTHENTICATION_TOKEN_KEY) || Storage.session.get(AUTHENTICATION_TOKEN_KEY);
  if (!token) {
    return false;
  }
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return typeof payload.exp === 'number' && Date.now() >= payload.exp * 1000;
  } catch {
    return false;
  }
};

const setupAxiosInterceptors = onUnauthenticated => {
  const onRequestSuccess = config => {
    const token = Storage.local.get(AUTHENTICATION_TOKEN_KEY) || Storage.session.get(AUTHENTICATION_TOKEN_KEY);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  };
  const onResponseSuccess = response => response;
  const onResponseError = (err: AxiosError) => {
    const status = err.status ?? (err.response ? err.response.status : 0);
    const url = err.config?.url ?? '';
    const isHandledElsewhere = URLS_HANDLED_ELSEWHERE.some(handledUrl => url.endsWith(handledUrl));
    if (status === 401 && !isHandledElsewhere) {
      onUnauthenticated(isTokenExpired() ? 'login.error.sessionexpired' : 'login.error.unauthorized');
    }
    return Promise.reject(err);
  };
  axios.interceptors.request.use(onRequestSuccess);
  axios.interceptors.response.use(onResponseSuccess, onResponseError);
};

export default setupAxiosInterceptors;
