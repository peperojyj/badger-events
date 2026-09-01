export function plainText(value) {
  if (!value) {
    return ''
  }

  const document = new DOMParser().parseFromString(
    value,
    'text/html',
  )

  return document.body.textContent || ''
}