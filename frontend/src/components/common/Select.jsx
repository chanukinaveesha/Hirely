import { forwardRef } from 'react'
import { cn } from '../../utils/cn'

// Inlined chevron so the native select arrow reads correctly on a dark background.
const CHEVRON_BG =
  "url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20' fill='none' stroke='%23A6A29B' stroke-width='1.5'%3E%3Cpath d='M5 7.5L10 12.5L15 7.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E\")"

const Select = forwardRef(function Select({ error = false, className, children, ...rest }, ref) {
  return (
    <select
      ref={ref}
      className={cn(
        'h-10 w-full appearance-none rounded-sm border bg-surface bg-no-repeat px-3 pr-9 text-body text-ink-primary',
        'transition-colors duration-150',
        'focus:outline-none focus:ring-1',
        'disabled:cursor-not-allowed disabled:opacity-50',
        error
          ? 'border-error focus:border-error focus:ring-error/40'
          : 'border-line focus:border-accent focus:ring-accent/40',
        className,
      )}
      style={{ backgroundImage: CHEVRON_BG, backgroundPosition: 'right 0.75rem center', backgroundSize: '1rem' }}
      {...rest}
    >
      {children}
    </select>
  )
})

export default Select
