import { forwardRef } from 'react'
import { cn } from '../../utils/cn'

const TextArea = forwardRef(function TextArea({ error = false, rows = 4, className, ...rest }, ref) {
  return (
    <textarea
      ref={ref}
      rows={rows}
      className={cn(
        'w-full rounded-sm border bg-surface px-3 py-2 text-body text-ink-primary placeholder:text-ink-muted',
        'transition-colors duration-150 resize-y',
        'focus:outline-none focus:ring-1',
        'disabled:cursor-not-allowed disabled:opacity-50',
        error
          ? 'border-error focus:border-error focus:ring-error/40'
          : 'border-line focus:border-accent focus:ring-accent/40',
        className,
      )}
      {...rest}
    />
  )
})

export default TextArea
