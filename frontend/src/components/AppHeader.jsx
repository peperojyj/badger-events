import { useState } from 'react'
import { Link } from 'react-router-dom'
import useAuth from '../auth/useAuth.js'

function AppHeader() {
  const { authStatus, logout, openAuth, user } = useAuth()
  const [loggingOut, setLoggingOut] = useState(false)
  const [logoutError, setLogoutError] = useState('')

  async function handleLogout() {
    setLoggingOut(true)
    setLogoutError('')

    try {
      await logout()
    } catch (error) {
      setLogoutError(error.message)
    } finally {
      setLoggingOut(false)
    }
  }

  const userInitial = user?.displayName
    ?.trim()
    .charAt(0)
    .toUpperCase()

  return (
    <header className="site-header">
      <div className="site-header__inner">
        <Link className="brand" to="/" aria-label="BadgerEvents home">
          <span className="brand__mark" aria-hidden="true">
            W
          </span>
          <span>BadgerEvents</span>
        </Link>

        <nav className="header-actions" aria-label="Main navigation">
          <a className="nav-link" href="/#events-heading">
            Explore events
          </a>

          {authStatus === 'loading' && (
            <span className="session-status">Checking session…</span>
          )}

          {authStatus === 'anonymous' && (
            <>
              <button
                className="header-login"
                type="button"
                onClick={() => openAuth('login')}
              >
                Log in
              </button>
              <button
                className="button button--primary header-signup"
                type="button"
                onClick={() => openAuth('register')}
              >
                Sign up
              </button>
            </>
          )}

          {authStatus === 'authenticated' && user && (
            <>
              <div
                className="account-pill"
                aria-label={`Signed in as ${user.displayName}`}
              >
                <span className="account-pill__avatar" aria-hidden="true">
                  {userInitial || '?'}
                </span>
                <span className="account-pill__copy">
                  <span className="account-pill__status">
                    <span className="account-pill__dot" aria-hidden="true" />
                    Signed in
                  </span>
                  <strong>{user.displayName}</strong>
                </span>
              </div>
              <button
                className="header-logout"
                type="button"
                onClick={handleLogout}
                disabled={loggingOut}
              >
                {loggingOut ? 'Logging out…' : 'Log out'}
              </button>
            </>
          )}
        </nav>

        {logoutError && (
          <p className="header-error" role="alert">{logoutError}</p>
        )}
      </div>
    </header>
  )
}

export default AppHeader