import { createPortal } from 'react-dom'
import { useToastStore } from './toastStore'
import { cn } from '../../utils/cn'

const VARIANT_STYLES = {
  success: 'border-success/40 bg-success-subtle text-success',
  error: 'border-error/40 bg-error-subtle text-error',
  warning: 'border-warning/40 bg-warning-subtle text-warning',
  info: 'border-info/40 bg-info-subtle text-info',
}

/** Mount once near the app root. Toasts are triggered via `toast.success(...)` etc. */
export default function ToastContainer() {
  const toasts = useToastStore((state) => state.toasts)
  const removeToast = useToastStore((state) => state.removeToast)

  if (toasts.length === 0) return null

  return createPortal(
    <div className="fixed bottom-4 right-4 z-[100] flex w-full max-w-sm flex-col gap-2">
      {toasts.map((item) => (
        <div
          key={item.id}
          role="status"
          className={cn(
            'flex items-start justify-between gap-3 rounded-md border bg-elevated px-4 py-3 shadow-modal',
            VARIANT_STYLES[item.variant],
          )}
        >
          <div>
            {item.title && <p className="text-small font-semibold">{item.title}</p>}
            <p className="text-small text-ink-primary">{item.message}</p>
          </div>
          <button
            onClick={() => removeToast(item.id)}
            aria-label="Dismiss notification"
            className="text-ink-muted hover:text-ink-primary"
          >
            ×
          </button>
        </div>
      ))}
    </div>,
    document.body,
  )
}
