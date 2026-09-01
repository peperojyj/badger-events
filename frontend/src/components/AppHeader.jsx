import { Link } from 'react-router-dom'

function AppHeader() {
  return (
    <header className="site-header">
      <div className="site-header__inner">
        <Link
          className="brand"
          to="/"
          aria-label="BadgerEvents home"
        >
          <span
            className="brand__mark"
            aria-hidden="true"
          >
            W
          </span>

          <span>BadgerEvents</span>
        </Link>

        <nav aria-label="Main navigation">
          <a
            className="nav-link"
            href="/#events-heading"
          >
            Explore events
          </a>
        </nav>
      </div>
    </header>
  )
}

export default AppHeader