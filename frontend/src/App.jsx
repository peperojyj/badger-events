import {
  Navigate,
  Route,
  Routes,
} from 'react-router-dom'
import AppHeader from './components/AppHeader.jsx'
import EventDetailPage from './pages/EventDetailPage.jsx'
import EventListPage from './pages/EventListPage.jsx'

function App() {
  return (
    <div className="app-shell">
      <AppHeader />

      <main>
        <Routes>
          <Route
            path="/"
            element={<EventListPage />}
          />

          <Route
            path="/events/:eventId"
            element={<EventDetailPage />}
          />

          <Route
            path="*"
            element={<Navigate to="/" replace />}
          />
        </Routes>
      </main>

      <footer className="site-footer">
        <p>
          Built for the UW–Madison campus community.
        </p>
      </footer>
    </div>
  )
}

export default App