import { useCallback, useEffect, useState } from 'react'
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

  const ensureCsrfToken = useCallback(async () => {
    if (csrfToken) {
      return csrfToken
    }

    const csrf = await getCsrfToken()
    setCsrfToken(csrf.token)
    return csrf.token
  }, [csrfToken])

  async function login(credentials) {
    const token = await ensureCsrfToken()
    const authenticatedUser = await loginUser(credentials, token)

    // 로그인 완료 후 현재 Session 기준 CSRF token을 먼저 받음
    const refreshedCsrf = await getCsrfToken()

    // 새 token을 저장한 다음 인증 상태를 공개
    setCsrfToken(refreshedCsrf.token)
    setUser(authenticatedUser)
    setAuthStatus('authenticated')
    setAuthError('')
    setNotice(`Signed in as ${authenticatedUser.displayName}.`)

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