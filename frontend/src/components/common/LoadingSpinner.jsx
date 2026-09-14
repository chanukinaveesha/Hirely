import { cn } from '../../utils/cn'

const SIZES = {
  sm: 'h-3.5 w-3.5 border-2',
  md: 'h-4 w-4 border-2',
  lg: 'h-6 w-6 border-[3px]',
}

/** Bare spinning indicator, sized to sit inline (e.g. inside a Button). */
export function Spinner({ size = 'md', color = 'border-ink-muted border-t-accent', className }) {
  return (
    <span
      role="status"
      aria-hidden="true"
      className={cn('inline-block animate-spin rounded-full', SIZES[size], color, className)}
    />
  )
}

/** Block-level spinner with a label, for page/section loading states. */
export default function LoadingSpinner({ label = 'Loading...', size = 'lg' }) {
  return (
    <div className="flex items-center justify-center gap-3 py-10 text-ink-secondary">
      <Spinner size={size} />
      <span className="text-small">{label}</span>
    </div>
  )
}
