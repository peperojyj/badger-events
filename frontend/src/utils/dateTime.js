const CAMPUS_TIME_ZONE = 'America/Chicago'

const dateFormatter = new Intl.DateTimeFormat('en-US', {
  weekday: 'short',
  month: 'short',
  day: 'numeric',
  year: 'numeric',
  timeZone: CAMPUS_TIME_ZONE,
})

const timeFormatter = new Intl.DateTimeFormat('en-US', {
  hour: 'numeric',
  minute: '2-digit',
  timeZone: CAMPUS_TIME_ZONE,
})

export function formatEventDate(startTime) {
  return dateFormatter.format(new Date(startTime))
}

export function formatEventTime(startTime, endTime) {
  const start = timeFormatter.format(new Date(startTime))

  if (!endTime) {
    return start
  }

  const end = timeFormatter.format(new Date(endTime))

  return `${start} – ${end}`
}