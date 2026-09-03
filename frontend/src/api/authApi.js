import { requestJson } from './apiClient.js'

export function getCsrfToken(signal) {
  return requestJson('/api/auth/csrf', {
    signal,
    fallbackMessage:
      'Could not initialize a secure session.',
  })
}

export function getCurrentUser(signal) {
  return requestJson('/api/auth/me', {
    signal,
    fallbackMessage:
      'Could not restore the current session.',
  })
}

export function registerUser(
  registration,
  csrfToken,
) {
  return requestJson('/api/auth/register', {
    method: 'POST',
    body: registration,
    headers: {
      'X-CSRF-TOKEN': csrfToken,
    },
    fallbackMessage:
      'Could not create that account.',
  })
}

export function loginUser(
  credentials,
  csrfToken,
) {
  return requestJson('/api/auth/login', {
    method: 'POST',
    body: credentials,
    headers: {
      'X-CSRF-TOKEN': csrfToken,
    },
    fallbackMessage:
      'Invalid email or password.',
  })
}

export function logoutUser(csrfToken) {
  return requestJson('/api/auth/logout', {
    method: 'POST',
    headers: {
      'X-CSRF-TOKEN': csrfToken,
    },
    fallbackMessage:
      'Could not log out. Please try again.',
  })
}