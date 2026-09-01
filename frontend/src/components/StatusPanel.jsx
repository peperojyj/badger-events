function StatusPanel({
  type = 'empty',
  title,
  message,
  action,
}) {
  const role =
    type === 'error' ? 'alert' : 'status'

  const icon =
    type === 'error'
      ? '!'
      : type === 'loading'
        ? '…'
        : '⌕'

  return (
    <section
      className={
        `status-panel status-panel--${type}`
      }
      role={role}
    >
      <span
        className="status-panel__icon"
        aria-hidden="true"
      >
        {icon}
      </span>

      <h2>{title}</h2>
      <p>{message}</p>

      {action}
    </section>
  )
}

export default StatusPanel