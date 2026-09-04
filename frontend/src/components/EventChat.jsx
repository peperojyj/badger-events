import { Client } from '@stomp/stompjs'
import { useEffect, useRef, useState } from 'react'
import {
  getChatMembership,
  getChatMembers,
  getRecentChatMessages,
  joinEventChat,
  leaveEventChat,
} from '../api/chatApi.js'
import useAuth from '../auth/useAuth.js'

const MAX_MESSAGE_LENGTH = 1000

function websocketUrl(path) {
  const protocol =
    window.location.protocol === 'https:'
      ? 'wss:'
      : 'ws:'

  return `${protocol}//${window.location.host}${path}`
}

function compareMessages(left, right) {
  const timeDifference =
    new Date(left.createdAt)
    - new Date(right.createdAt)

  return timeDifference || left.id - right.id
}

function mergeMessages(
  currentMessages,
  incomingMessages,
) {
  const messagesById = new Map(
    currentMessages.map((message) => [
      message.id,
      message,
    ]),
  )

  incomingMessages.forEach((message) => {
    messagesById.set(message.id, message)
  })

  return [...messagesById.values()]
    .sort(compareMessages)
}

function formatMessageTime(value) {
  return new Intl.DateTimeFormat('en-US', {
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value))
}

function sameIdentifier(left, right) {
  return (
    left != null
    && right != null
    && String(left) === String(right)
  )
}

function ChatMemberSummary({
  interestedCount,
  members,
}) {
  return (
    <div className="event-chat__member-summary">
      <p>
        <strong>{interestedCount}</strong> interested
        <span aria-hidden="true"> · </span>
        <strong>{members.length}</strong>{' '}
        conversation members
      </p>

      {members.length > 0 && (
        <ul aria-label="Conversation members">
          {members.map((member) => (
            <li key={member.userId}>
              <span aria-hidden="true">
                {member.displayName
                  .charAt(0)
                  .toUpperCase()}
              </span>

              {member.displayName}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

function EventChat({
  eventId,
  interestedCount = 0,
}) {
  const {
    authStatus,
    user,
    ensureCsrfToken,
    openAuth,
  } = useAuth()

  const clientRef = useRef(null)
  const messageListRef = useRef(null)

  const [messages, setMessages] = useState([])
  const [draft, setDraft] = useState('')

  const [
    membershipStatus,
    setMembershipStatus,
  ] = useState('checking')

  const [joining, setJoining] = useState(false)
  const [leaving, setLeaving] = useState(false)

  const [
    membershipError,
    setMembershipError,
  ] = useState('')

  const [members, setMembers] = useState([])

  const [
    connectionStatus,
    setConnectionStatus,
  ] = useState('idle')

  const [error, setError] = useState('')

  const currentUserId = user?.id

  useEffect(() => {
    if (
      authStatus !== 'authenticated'
      || !currentUserId
    ) {
      return undefined
    }

    let active = true
    const controller = new AbortController()

    getChatMembership(
      eventId,
      controller.signal,
    )
      .then((membership) => {
        if (active) {
          setMembershipStatus(
            membership.joined
              ? 'joined'
              : 'not-joined',
          )
        }
      })
      .catch((requestError) => {
        if (
          active
          && requestError.name !== 'AbortError'
        ) {
          setMembershipStatus('error')
          setMembershipError(requestError.message)
        }
      })

    getChatMembers(eventId, controller.signal)
      .then((loadedMembers) => {
        if (active) {
          setMembers(loadedMembers)
        }
      })
      .catch((requestError) => {
        if (
          active
          && requestError.name !== 'AbortError'
        ) {
          setMembershipError(requestError.message)
        }
      })

    return () => {
      active = false
      controller.abort()
    }
  }, [
    authStatus,
    currentUserId,
    eventId,
  ])

  useEffect(() => {
    if (
      authStatus !== 'authenticated'
      || !currentUserId
      || membershipStatus !== 'joined'
    ) {
      return undefined
    }

    let active = true
    const historyController =
      new AbortController()

    async function loadHistory() {
      try {
        const history =
          await getRecentChatMessages(
            eventId,
            historyController.signal,
          )

        if (active) {
          setMessages((currentMessages) =>
            mergeMessages(
              currentMessages,
              history,
            ),
          )
        }
      } catch (requestError) {
        if (
          active
          && requestError.name !== 'AbortError'
        ) {
          setError(requestError.message)
        }
      }
    }

    async function connect() {
      setMessages([])
      setConnectionStatus('connecting')
      setError('')

      loadHistory()

      try {
        const csrfToken =
          await ensureCsrfToken()

        if (!active) {
          return
        }

        const client = new Client({
          brokerURL: websocketUrl('/ws'),

          connectHeaders: {
            'X-CSRF-TOKEN': csrfToken,
          },

          reconnectDelay: 5000,
          heartbeatIncoming: 10000,
          heartbeatOutgoing: 10000,

          onConnect: () => {
            if (!active) {
              return
            }

            setConnectionStatus('connected')
            setError('')

            client.subscribe(
              `/topic/events/${eventId}`,
              (frame) => {
                try {
                  const message =
                    JSON.parse(frame.body)

                  setMessages((currentMessages) =>
                    mergeMessages(
                      currentMessages,
                      [message],
                    ),
                  )
                } catch {
                  setError(
                    'A live message could not be displayed.',
                  )
                }
              },
            )

            loadHistory()
          },

          onStompError: () => {
            if (active) {
              setConnectionStatus('error')
              setError(
                'The chat server rejected the connection.',
              )
            }
          },

          onWebSocketError: () => {
            if (active) {
              setConnectionStatus('error')
              setError(
                'Could not connect to live chat.',
              )
            }
          },

          onWebSocketClose: () => {
            if (active) {
              setConnectionStatus('connecting')
            }
          },
        })

        clientRef.current = client
        client.activate()
      } catch (connectionError) {
        if (active) {
          setConnectionStatus('error')
          setError(connectionError.message)
        }
      }
    }

    connect()

    return () => {
      active = false
      historyController.abort()

      const client = clientRef.current
      clientRef.current = null

      if (client) {
        client.deactivate()
      }
    }
  }, [
    authStatus,
    currentUserId,
    ensureCsrfToken,
    eventId,
    membershipStatus,
  ])

  useEffect(() => {
    const messageList =
      messageListRef.current

    if (messageList) {
      messageList.scrollTop =
        messageList.scrollHeight
    }
  }, [messages])

  function handleSubmit(event) {
    event.preventDefault()

    const content = draft.trim()
    const client = clientRef.current

    if (!content || !client?.connected) {
      return
    }

    client.publish({
      destination:
        `/app/events/${eventId}/messages`,
      body: JSON.stringify({ content }),
    })

    setDraft('')
  }

  async function handleJoin() {
    setJoining(true)
    setMembershipError('')

    try {
      const csrfToken =
        await ensureCsrfToken()

      const membership =
        await joinEventChat(
          eventId,
          csrfToken,
        )

      if (membership.joined) {
        setMembershipStatus('joined')

        getChatMembers(eventId)
          .then(setMembers)
          .catch((memberError) => {
            setMembershipError(
              memberError.message,
            )
          })
      }
    } catch (joinError) {
      setMembershipStatus('error')
      setMembershipError(joinError.message)
    } finally {
      setJoining(false)
    }
  }

  async function handleLeave() {
    setLeaving(true)
    setMembershipError('')

    try {
      const csrfToken =
        await ensureCsrfToken()

      await leaveEventChat(
        eventId,
        csrfToken,
      )

      setMessages([])
      setDraft('')
      setConnectionStatus('idle')
      setMembershipStatus('not-joined')

      getChatMembers(eventId)
        .then(setMembers)
        .catch((memberError) => {
          setMembershipError(
            memberError.message,
          )
        })
    } catch (leaveError) {
      setMembershipError(leaveError.message)
    } finally {
      setLeaving(false)
    }
  }

  if (authStatus === 'loading') {
    return (
      <section
        className="event-chat event-chat--locked"
        aria-label="Event chat"
      >
        <p>Checking your session…</p>
      </section>
    )
  }

  if (
    authStatus !== 'authenticated'
    || !user
  ) {
    return (
      <section
        className="event-chat event-chat--locked"
        aria-labelledby="chat-heading"
      >
        <span
          className="event-chat__locked-icon"
          aria-hidden="true"
        >
          💬
        </span>

        <div>
          <p className="eyebrow eyebrow--red">
            Event conversation
          </p>

          <h2 id="chat-heading">
            See who else is interested
          </h2>

          <p>
            Sign in to read and join this
            event-specific conversation.
          </p>
        </div>

        <button
          className="button button--primary"
          type="button"
          onClick={() => openAuth('login')}
        >
          Sign in to chat
        </button>
      </section>
    )
  }

  if (membershipStatus === 'checking') {
    return (
      <section
        className="event-chat event-chat--locked"
        aria-label="Event chat"
      >
        <p>
          Checking whether you joined this
          conversation…
        </p>
      </section>
    )
  }

  if (membershipStatus !== 'joined') {
    return (
      <section
        className="event-chat event-chat--locked"
        aria-labelledby="chat-heading"
      >
        <span
          className="event-chat__locked-icon"
          aria-hidden="true"
        >
          👋
        </span>

        <div>
          <p className="eyebrow eyebrow--red">
            Event conversation
          </p>

          <h2 id="chat-heading">
            Join the conversation
          </h2>

          <p>
            Join once to read and participate
            in this event-specific chat.
            We will remember your choice.
          </p>

          <ChatMemberSummary
            interestedCount={interestedCount}
            members={members}
          />

          {membershipError && (
            <p
              className="event-chat__join-error"
              role="alert"
            >
              {membershipError}
            </p>
          )}
        </div>

        <button
          className="button button--primary"
          type="button"
          disabled={joining}
          onClick={handleJoin}
        >
          {joining
            ? 'Joining…'
            : 'Join conversation'}
        </button>
      </section>
    )
  }

  const isConnected =
    connectionStatus === 'connected'

  return (
    <section
      className="event-chat"
      aria-labelledby="chat-heading"
    >
      <div className="event-chat__heading">
        <div>
          <p className="eyebrow eyebrow--red">
            Event conversation
          </p>

          <h2 id="chat-heading">
            Talk before you show up
          </h2>
        </div>

        <div className="event-chat__heading-actions">
          <span
            className={
              `connection-badge `
              + `connection-badge--${connectionStatus}`
            }
          >
            <span aria-hidden="true" />

            {isConnected
              ? 'Live'
              : connectionStatus === 'error'
                ? 'Offline'
                : 'Connecting'}
          </span>

          <button
            className="event-chat__leave-button"
            type="button"
            disabled={leaving}
            onClick={handleLeave}
          >
            {leaving
              ? 'Leaving…'
              : 'Leave chat'}
          </button>
        </div>
      </div>

      <ChatMemberSummary
        interestedCount={interestedCount}
        members={members}
      />

      <div
        className="event-chat__messages"
        ref={messageListRef}
        aria-live="polite"
        aria-label="Chat messages"
      >
        {messages.length === 0 ? (
          <div className="event-chat__empty">
            <span aria-hidden="true">
              👋
            </span>

            <strong>
              Start the conversation
            </strong>

            <p>
              Ask who is attending or share
              what you are looking forward to.
            </p>
          </div>
        ) : (
          messages.map((message) => {
            const sentByMe =
              sameIdentifier(
                message.authorId,
                user.id,
              )

            return (
              <article
                className={
                  `chat-message`
                  + `${sentByMe
                    ? ' chat-message--mine'
                    : ''}`
                }
                key={message.id}
              >
                <div className="chat-message__meta">
                  <strong>
                    {sentByMe
                      ? 'You'
                      : message.authorDisplayName}
                  </strong>

                  <time
                    dateTime={message.createdAt}
                  >
                    {formatMessageTime(
                      message.createdAt,
                    )}
                  </time>
                </div>

                <p>{message.content}</p>
              </article>
            )
          })
        )}
      </div>

      {error && (
        <p
          className="event-chat__error"
          role="alert"
        >
          {error}
        </p>
      )}

      {membershipError && (
        <p
          className="event-chat__error"
          role="alert"
        >
          {membershipError}
        </p>
      )}

      <form
        className="event-chat__composer"
        onSubmit={handleSubmit}
      >
        <label
          className="sr-only"
          htmlFor={`chat-message-${eventId}`}
        >
          Message
        </label>

        <input
          id={`chat-message-${eventId}`}
          type="text"
          value={draft}
          maxLength={MAX_MESSAGE_LENGTH}
          placeholder={
            isConnected
              ? 'Ask who is going…'
              : 'Connecting to chat…'
          }
          disabled={!isConnected}
          onChange={(event) =>
            setDraft(event.target.value)}
        />

        <button
          className="button button--primary"
          type="submit"
          disabled={
            !isConnected || !draft.trim()
          }
        >
          Send
        </button>
      </form>
    </section>
  )
}

export default EventChat