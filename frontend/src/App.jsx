import { Navigate, Route, Routes } from 'react-router-dom'
import useAuth from './auth/useAuth.js'
import AppHeader from './components/AppHeader.jsx'
import AuthDialog from './components/AuthDialog.jsx'
import EventDetailPage from './pages/EventDetailPage.jsx'
import EventListPage from './pages/EventListPage.jsx'

function App() {
  const { authDialogMode, clearNotice, notice } = useAuth()

  return (
    <div className="app-shell">
      <AppHeader />
      {authDialogMode && (
        <AuthDialog key={authDialogMode} />
      )}
      {notice && (
        <div className="session-snackbar" role="status" aria-live="polite">
          <span className="session-snackbar__icon" aria-hidden="true">✓</span>
          <span>{notice}</span>
          <button
            type="button"
            onClick={clearNotice}
            aria-label="Dismiss notification"
          >
            ×
          </button>
        </div>
      )}
      <main>
        <Routes>
          <Route path="/" element={<EventListPage />} />
          <Route path="/events/:eventId" element={<EventDetailPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <footer className="site-footer">
        <p>Built for the UW–Madison campus community.</p>
      </footer>
    </div>
  )
}

export default App