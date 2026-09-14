import { cn } from '../../utils/cn'

/**
 * Label + control + helper/error message wrapper. Pass `htmlFor` matching
 * the child input's `id`, and the same `error` to both FormField and the
 * input (so the input gets the error border and this renders the message).
 */
export default function FormField({ label, htmlFor, error, helperText, required = false, children, className }) {
  return (
    <div className={cn('flex flex-col gap-1.5', className)}>
      {label && (
        <label htmlFor={htmlFor} className="text-label text-ink-secondary">
          {label}
          {required && <span className="text-accent"> *</span>}
        </label>
      )}
      {children}
      {error ? (
        <p className="text-small text-error">{error}</p>
      ) : (
        helperText && <p className="text-small text-ink-muted">{helperText}</p>
      )}
    </div>
  )
}
