import { Link } from 'react-router-dom'
import InterestedButton from './InterestedButton.jsx'
import { categoryVisual } from '../utils/categoryVisuals.js'
import { formatEventDate, formatEventTime } from '../utils/dateTime.js'
import { plainText } from '../utils/plainText.js'

function EventCard({ event, onInterestChanged }) {
  const visual = categoryVisual(event.category)

  return (
    <article className="event-card">
      <div className={`event-card__visual event-card__visual--${visual.tone}`}>
        <span aria-hidden="true">{visual.icon}</span>
        <span className="event-card__category">{visual.label}</span>
      </div>

      <div className="event-card__body">
        <p className="event-card__date">{formatEventDate(event.startTime)}</p>
        <h2>
          <Link to={`/events/${event.id}`}>{plainText(event.title)}</Link>
        </h2>
        <dl className="event-card__metadata">
          <div>
            <dt aria-label="Time">◷</dt>
            <dd>{formatEventTime(event.startTime, event.endTime)}</dd>
          </div>
          <div>
            <dt aria-label="Location">⌖</dt>
            <dd>{plainText(event.location) || 'Location to be announced'}</dd>
          </div>
        </dl>

        <InterestedButton
          event={event}
          onChanged={onInterestChanged}
        />

        <Link className="event-card__link" to={`/events/${event.id}`}>
          View event <span aria-hidden="true">→</span>
        </Link>
      </div>
    </article>
  )
}

export default EventCard