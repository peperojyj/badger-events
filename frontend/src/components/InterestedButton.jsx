import { useState } from 'react'
import { addInterest, removeInterest } from '../api/interestsApi.js'
import useAuth from '../auth/useAuth.js'

function InterestedButton({ event, onChanged }) {
  const { ensureCsrfToken, openAuth, user } = useAuth()
  const [pending, setPending] = useState(false)
  const [error, setError] = useState('')

  async function handleClick() {
    if (!user) {
      openAuth('login')
      return
    }

    setPending(true)
    setError('')

    try {
      const csrfToken = await ensureCsrfToken()
      const response = event.interestedByMe
        ? await removeInterest(event.id, csrfToken)
        : await addInterest(event.id, csrfToken)

      onChanged(response)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="interest-control">
      <button
        className={`interest-button${event.interestedByMe ? ' interest-button--active' : ''}`}
        type="button"
        onClick={handleClick}
        disabled={pending}
        aria-pressed={event.interestedByMe}
      >
        <span aria-hidden="true">
          {event.interestedByMe ? '★' : '☆'}
        </span>
        <span>{pending ? 'Saving…' : 'Interested'}</span>
        <span className="interest-button__count">· {event.interestedCount}</span>
      </button>

      {error && <p className="interest-error" role="alert">{error}</p>}
    </div>
  )
}

export default InterestedButton