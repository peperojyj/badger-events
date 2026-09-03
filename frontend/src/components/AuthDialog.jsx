import { useState } from 'react'
import useAuth from '../auth/useAuth.js'

function AuthDialog() {
  const {
    authDialogMode,
    closeAuth,
    login,
    openAuth,
    register,
  } = useAuth()

  const [email, setEmail] = useState('')
  const [password, setPassword] =
    useState('')
  const [displayName, setDisplayName] =
    useState('')
  const [submitting, setSubmitting] =
    useState(false)
  const [error, setError] = useState('')

  const registering =
    authDialogMode === 'register'

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    setError('')

    try {
      if (registering) {
        await register({
          email,
          password,
          displayName,
        })
      } else {
        await login({
          email,
          password,
        })
      }

      closeAuth()
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div
      className="auth-backdrop"
      role="presentation"
    >
      <section
        className="auth-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="auth-title"
      >
        <button
          className="auth-dialog__close"
          type="button"
          onClick={closeAuth}
          aria-label="Close authentication dialog"
        >
          ×
        </button>

        <p className="eyebrow eyebrow--red">
          BadgerEvents account
        </p>

        <h2 id="auth-title">
          {registering
            ? 'Create your account'
            : 'Welcome back'}
        </h2>

        <p className="auth-dialog__lede">
          {registering
            ? 'Save events and join event-specific conversations.'
            : 'Log in to mark events as Interested.'}
        </p>

        <form
          className="auth-form"
          onSubmit={handleSubmit}
        >
          {registering && (
            <label>
              Display name
              <input
                type="text"
                value={displayName}
                onChange={(event) =>
                  setDisplayName(
                    event.target.value,
                  )
                }
                minLength="2"
                maxLength="100"
                autoComplete="name"
                required
              />
            </label>
          )}

          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(event) =>
                setEmail(event.target.value)
              }
              maxLength="320"
              autoComplete="email"
              required
            />
          </label>

          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) =>
                setPassword(event.target.value)
              }
              minLength="8"
              maxLength="72"
              autoComplete={
                registering
                  ? 'new-password'
                  : 'current-password'
              }
              required
            />
          </label>

          {error && (
            <p
              className="form-error"
              role="alert"
            >
              {error}
            </p>
          )}

          <button
            className={
              'button button--primary ' +
              'button--full'
            }
            type="submit"
            disabled={submitting}
          >
            {submitting
              ? 'Please wait…'
              : registering
                ? 'Create account'
                : 'Log in'}
          </button>
        </form>

        <p className="auth-dialog__switch">
          {registering
            ? 'Already have an account?'
            : 'New to BadgerEvents?'}{' '}

          <button
            type="button"
            onClick={() =>
              openAuth(
                registering
                  ? 'login'
                  : 'register',
              )
            }
          >
            {registering
              ? 'Log in'
              : 'Create an account'}
          </button>
        </p>
      </section>
    </div>
  )
}

export default AuthDialog