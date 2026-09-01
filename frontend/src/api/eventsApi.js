async function requestJson(path, { signal } = {}) {
  const response = await fetch(path, {
    headers: {
      Accept: 'application/json',
    },
    signal,
  })

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error('We could not find that event.')
    }

    throw new Error('BadgerEvents could not load event data.')
  }

  return response.json()
}

export function getEvents(keyword = '', signal) {
  const parameters = new URLSearchParams()
  const normalizedKeyword = keyword.trim()

  if (normalizedKeyword) {
    parameters.set('keyword', normalizedKeyword)
  }

  const query = parameters.toString()

  const path = query
    ? `/api/events?${query}`
    : '/api/events'

  return requestJson(path, { signal })
}

export function getEvent(eventId, signal) {
  return requestJson(`/api/events/${eventId}`, { signal })
}