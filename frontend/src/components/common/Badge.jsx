import { cn } from '../../utils/cn'

const VARIANTS = {
  neutral: 'bg-surface text-ink-secondary border border-line',
  accent: 'bg-accent-subtle text-accent border border-accent/30',
  secondary: 'bg-secondary-subtle text-secondary border border-secondary/30',
  success: 'bg-success-subtle text-success border border-success/30',
  error: 'bg-error-subtle text-error border border-error/30',
  warning: 'bg-warning-subtle text-warning border border-warning/30',
  info: 'bg-info-subtle text-info border border-info/30',
}

/** Status/tag indicator. Deliberately sharp corners — the one place we avoid rounding. */
export default function Badge({ variant = 'neutral', children, className }) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-xs px-2 py-0.5 text-label',
        VARIANTS[variant],
        className,
      )}
    >
      {children}
    </span>
  )
}
