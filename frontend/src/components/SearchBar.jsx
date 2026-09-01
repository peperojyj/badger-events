function SearchBar({
  value,
  onChange,
  onSubmit,
  onClear,
  searching,
}) {
  return (
    <form
      className="search"
      role="search"
      onSubmit={onSubmit}
    >
      <label
        className="sr-only"
        htmlFor="event-search"
      >
        Search campus events
      </label>

      <span
        className="search__icon"
        aria-hidden="true"
      >
        ⌕
      </span>

      <input
        id="event-search"
        type="search"
        placeholder="Search by title, description, or location"
        value={value}
        onChange={(event) =>
          onChange(event.target.value)
        }
      />

      {value && (
        <button
          className="search__clear"
          type="button"
          onClick={onClear}
          aria-label="Clear search"
        >
          ×
        </button>
      )}

      <button
        className="button button--primary"
        type="submit"
        disabled={searching}
      >
        {searching ? 'Searching…' : 'Search'}
      </button>
    </form>
  )
}

export default SearchBar