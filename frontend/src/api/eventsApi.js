import { ApiError, requestJson } from './apiClient.js'

export function getEvents(keyword = '', signal) {
  const parameters = new URLSearchParams()
  const normalizedKeyword = keyword.trim()

  if (normalizedKeyword) {
    parameters.set('keyword', normalizedKeyword)
  }

  const query = parameters.toString()
  const path = query ? `/api/events?${query}` : '/api/events'

  return requestJson(path, {
    signal,
    fallbackMessage: 'BadgerEvents could not load event data.',
  })
}

export async function getEvent(eventId, signal) {
  try {
    return await requestJson(`/api/events/${eventId}`, {
      signal,
      fallbackMessage: 'BadgerEvents could not load event data.',
    })
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      throw new Error('We could not find that event.', { cause: error })
    }

    throw error
  }
}