import { useEffect, useState } from 'react'
import useAuth from '../auth/useAuth.js'
import EventCard from '../components/EventCard.jsx'
import SearchBar from '../components/SearchBar.jsx'
import StatusPanel from '../components/StatusPanel.jsx'
import { getEvents } from '../api/eventsApi.js'

function EventListPage() {
  const { user } = useAuth()
  const [events, setEvents] = useState([])
  const [draftKeyword, setDraftKeyword] = useState('')
  const [submittedKeyword, setSubmittedKeyword] = useState('')
  const [requestVersion, setRequestVersion] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    async function loadEvents() {
      setLoading(true)
      setError('')

      try {
        const data = await getEvents(submittedKeyword, controller.signal)
        setEvents(data)
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

    loadEvents()

    return () => controller.abort()
  }, [submittedKeyword, requestVersion, user?.id])

  function handleInterestChanged(response) {
    setEvents((currentEvents) =>
      currentEvents
        .map((event) =>
          event.id === response.eventId
            ? {
                ...event,
                interestedCount: response.interestedCount,
                interestedByMe: response.interestedByMe,
              }
            : event,
        )
        .sort((first, second) => {
          if (first.interestedByMe !== second.interestedByMe) {
            return first.interestedByMe ? -1 : 1
          }

          return new Date(first.startTime) - new Date(second.startTime)
        }),
    )
  }

  function handleSubmit(event) {
    event.preventDefault()
    setSubmittedKeyword(draftKeyword.trim())
  }

  function handleClear() {
    setDraftKeyword('')
    setSubmittedKeyword('')
  }

  function retry() {
    setRequestVersion((current) => current + 1)
  }

  return (
    <>
      <section className="hero">
        <div className="hero__inner">
          <p className="eyebrow">UW–Madison campus events</p>
          <h1>Find something worth showing up for.</h1>
          <p className="hero__lede">
            Discover what is happening across campus today, this week, and beyond.
          </p>
          <SearchBar
            value={draftKeyword}
            onChange={setDraftKeyword}
            onSubmit={handleSubmit}
            onClear={handleClear}
            searching={loading}
          />
        </div>
      </section>

      <section className="events-section" aria-labelledby="events-heading">
        <div className="section-heading">
          <div>
            <p className="eyebrow eyebrow--red">Discover campus</p>
            <h2 id="events-heading">
              {submittedKeyword ? `Results for “${submittedKeyword}”` : 'Upcoming events'}
            </h2>
          </div>
          {!loading && !error && (
            <p className="result-count">
              {events.length} {events.length === 1 ? 'event' : 'events'}
            </p>
          )}
        </div>

        {loading && (
          <StatusPanel
            type="loading"
            title="Finding upcoming events"
            message="We are loading the latest UW–Madison event data."
          />
        )}

        {!loading && error && (
          <StatusPanel
            type="error"
            title="Events are unavailable"
            message={error}
            action={
              <button className="button button--secondary" type="button" onClick={retry}>
                Try again
              </button>
            }
          />
        )}

        {!loading && !error && events.length === 0 && (
          <StatusPanel
            title="No matching events"
            message="Try a broader keyword or clear the search to see every upcoming event."
            action={
              submittedKeyword ? (
                <button className="button button--secondary" type="button" onClick={handleClear}>
                  Clear search
                </button>
              ) : null
            }
          />
        )}

        {!loading && !error && events.length > 0 && (
          <div className="event-grid">
            {events.map((event) => (
              <EventCard
                key={event.id}
                event={event}
                onInterestChanged={handleInterestChanged}
              />
            ))}
          </div>
        )}
      </section>
    </>
  )
}

export default EventListPage