export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export async function requestJson(
  path,
  {
    method = 'GET',
    body,
    headers = {},
    signal,
    fallbackMessage =
      'BadgerEvents could not complete the request.',
  } = {},
) {
  const requestHeaders = {
    Accept: 'application/json',
    ...headers,
  }

  if (body !== undefined) {
    requestHeaders['Content-Type'] =
      'application/json'
  }

  const response = await fetch(path, {
    method,
    credentials: 'include',
    headers: requestHeaders,
    body:
      body === undefined
        ? undefined
        : JSON.stringify(body),
    signal,
  })

  if (!response.ok) {
    throw new ApiError(
      fallbackMessage,
      response.status,
    )
  }

  if (response.status === 204) {
    return null
  }

  return response.json()
}