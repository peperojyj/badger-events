import { useEffect, useState } from 'react'
import { ApiError } from '../api/apiClient.js'
import {
  getCsrfToken,
  getCurrentUser,
  loginUser,
  logoutUser,
  registerUser,
} from '../api/authApi.js'
import AuthContext from './AuthContext.js'

function AuthProvider({ children }) {
  const [authStatus, setAuthStatus] = useState('loading')
  const [user, setUser] = useState(null)
  const [csrfToken, setCsrfToken] = useState(null)
  const [authError, setAuthError] = useState('')
  const [authDialogMode, setAuthDialogMode] = useState(null)
  const [notice, setNotice] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    async function initializeAuth() {
      try {
        const csrf = await getCsrfToken(controller.signal)
        setCsrfToken(csrf.token)

        try {
          const currentUser = await getCurrentUser(controller.signal)
          setUser(currentUser)
          setAuthStatus('authenticated')
        } catch (error) {
          if (error instanceof ApiError && error.status === 401) {
            setUser(null)
            setAuthStatus('anonymous')
            return
          }

          throw error
        }
      } catch (error) {
        if (error.name !== 'AbortError') {
          setAuthError(error.message)
          setAuthStatus('anonymous')
        }
      }
    }

    initializeAuth()

    return () => controller.abort()
  }, [])

  useEffect(() => {
    if (!notice) {
      return undefined
    }

    const timeoutId = window.setTimeout(() => {
      setNotice('')
    }, 2600)

    return () => window.clearTimeout(timeoutId)
  }, [notice])

  async function ensureCsrfToken() {
    if (csrfToken) {
      return csrfToken
    }

    const csrf = await getCsrfToken()
    setCsrfToken(csrf.token)
    return csrf.token
  }

  async function login(credentials) {
    const token = await ensureCsrfToken()
    const authenticatedUser = await loginUser(credentials, token)

    setUser(authenticatedUser)
    setAuthStatus('authenticated')
    setAuthError('')
    setNotice(`Signed in as ${authenticatedUser.displayName}.`)

    const refreshedCsrf = await getCsrfToken()
    setCsrfToken(refreshedCsrf.token)

    return authenticatedUser
  }

  async function register(registration) {
    const token = await ensureCsrfToken()
    await registerUser(registration, token)

    return login({
      email: registration.email,
      password: registration.password,
    })
  }

  async function logout() {
    const token = await ensureCsrfToken()
    await logoutUser(token)

    setUser(null)
    setAuthStatus('anonymous')
    setCsrfToken(null)
    setAuthError('')
    setNotice('Signed out successfully.')
  }

  function openAuth(mode = 'login') {
    setAuthDialogMode(mode)
  }

  function closeAuth() {
    setAuthDialogMode(null)
  }

  function clearNotice() {
    setNotice('')
  }

  return (
    <AuthContext.Provider
      value={{
        authStatus,
        user,
        authError,
        authDialogMode,
        notice,
        ensureCsrfToken,
        login,
        register,
        logout,
        openAuth,
        closeAuth,
        clearNotice,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export default AuthProvider