import { requestJson } from './apiClient.js'

export function getChatMembership(eventId, signal) {
  return requestJson(
    `/api/events/${eventId}/chat-membership`,
    {
      signal,
      fallbackMessage:
        'Could not check your chat membership.',
    },
  )
}

export function joinEventChat(eventId, csrfToken) {
  return requestJson(
    `/api/events/${eventId}/chat-membership`,
    {
      method: 'PUT',
      headers: {
        'X-CSRF-TOKEN': csrfToken,
      },
      fallbackMessage:
        'Could not join this event conversation.',
    },
  )
}

export function leaveEventChat(eventId, csrfToken) {
  return requestJson(
    `/api/events/${eventId}/chat-membership`,
    {
      method: 'DELETE',
      headers: {
        'X-CSRF-TOKEN': csrfToken,
      },
      fallbackMessage:
        'Could not leave this event conversation.',
    },
  )
}

export function getChatMembers(eventId, signal) {
  return requestJson(
    `/api/events/${eventId}/chat-membership/members`,
    {
      signal,
      fallbackMessage:
        'Could not load conversation members.',
    },
  )
}

export function getRecentChatMessages(eventId, signal) {
  return requestJson(
    `/api/events/${eventId}/messages`,
    {
      signal,
      fallbackMessage:
        'Could not load the event conversation.',
    },
  )
}