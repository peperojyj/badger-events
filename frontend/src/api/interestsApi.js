import { requestJson } from './apiClient.js'

export function addInterest(eventId, csrfToken) {
  return requestJson(`/api/events/${eventId}/interests`, {
    method: 'POST',
    headers: { 'X-CSRF-TOKEN': csrfToken },
    fallbackMessage: 'Could not save your interest.',
  })
}

export function removeInterest(eventId, csrfToken) {
  return requestJson(`/api/events/${eventId}/interests`, {
    method: 'DELETE',
    headers: { 'X-CSRF-TOKEN': csrfToken },
    fallbackMessage: 'Could not remove your interest.',
  })
}