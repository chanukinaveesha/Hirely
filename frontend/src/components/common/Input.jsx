import { forwardRef } from 'react'
import { cn } from '../../utils/cn'

const Input = forwardRef(function Input({ error = false, className, ...rest }, ref) {
  return (
    <input
      ref={ref}
      className={cn(
        'h-10 w-full rounded-sm border bg-surface px-3 text-body text-ink-primary placeholder:text-ink-muted',
        'transition-colors duration-150',
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

export default Input
