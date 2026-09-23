/** Banner-level error, e.g. a failed form submission. For field errors use FormField's `error` prop. */
export default function ErrorMessage({ message }) {
  if (!message) return null

  return (
    <div className="rounded-sm border border-error/40 bg-error-subtle px-4 py-2 text-small text-error">
      {message}
    </div>
  )
}
