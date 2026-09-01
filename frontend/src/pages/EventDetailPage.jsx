import { useEffect, useState } from 'react'
import {
  Link,
  useParams,
} from 'react-router-dom'
import { getEvent } from '../api/eventsApi.js'
import StatusPanel from '../components/StatusPanel.jsx'
import { categoryVisual } from '../utils/categoryVisuals.js'
import {
  formatEventDate,
  formatEventTime,
} from '../utils/dateTime.js'
import { plainText } from '../utils/plainText.js'

function EventDetailPage() {
  const { eventId } = useParams()

  const [event, setEvent] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    async function loadEvent() {
      setLoading(true)
      setError('')

      try {
        const data = await getEvent(
          eventId,
          controller.signal,
        )

        setEvent(data)
      } catch (requestError) {
        if (requestError.name !== 'AbortError') {
          setError(requestError.message)
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    loadEvent()

    return () => controller.abort()
  }, [eventId])

  if (loading) {
    return (
      <div className="detail-state">
        <StatusPanel
          type="loading"
          title="Loading event"
          message="We are retrieving the event details."
        />
      </div>
    )
  }

  if (error || !event) {
    return (
      <div className="detail-state">
        <StatusPanel
          type="error"
          title="Event unavailable"
          message={
            error ||
            'We could not find that event.'
          }
          action={
            <Link
              className="button button--secondary"
              to="/"
            >
              Back to events
            </Link>
          }
        />
      </div>
    )
  }

  const visual = categoryVisual(event.category)

  return (
    <article className="event-detail">
      <div className="event-detail__topbar">
        <Link className="back-link" to="/">
          <span aria-hidden="true">←</span>
          {' '}Back to all events
        </Link>
      </div>

      <header
        className={
          `event-detail__hero ` +
          `event-detail__hero--${visual.tone}`
        }
      >
        <div
          className="event-detail__icon"
          aria-hidden="true"
        >
          {visual.icon}
        </div>

        <div>
          <p className="event-detail__category">
            {visual.label}
          </p>

          <h1>{plainText(event.title)}</h1>
        </div>
      </header>

      <div className="event-detail__layout">
        <section className="event-detail__content">
          <p className="eyebrow eyebrow--red">
            About this event
          </p>

          <h2>What to expect</h2>

          <p className="event-description">
            {plainText(event.description) ||
              'The organizer has not added ' +
                'a description yet.'}
          </p>
        </section>

        <aside
          className="event-facts"
          aria-label="Event details"
        >
          <h2>Event details</h2>

          <dl>
            <div>
              <dt>Date</dt>
              <dd>
                {formatEventDate(event.startTime)}
              </dd>
            </div>

            <div>
              <dt>Time</dt>
              <dd>
                {formatEventTime(
                  event.startTime,
                  event.endTime,
                )}
              </dd>
            </div>

            <div>
              <dt>Location</dt>
              <dd>
                {plainText(event.location) ||
                  'Location to be announced'}
              </dd>
            </div>
          </dl>

          {event.eventUrl && (
            <a
              className={
                'button button--primary button--full'
              }
              href={event.eventUrl}
              target="_blank"
              rel="noreferrer"
            >
              Official event page
              <span aria-hidden="true"> ↗</span>
            </a>
          )}
        </aside>
      </div>
    </article>
  )
}

export default EventDetailPage