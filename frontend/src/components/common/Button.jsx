import { forwardRef } from 'react'
import { cn } from '../../utils/cn'
import { Spinner } from './LoadingSpinner'

const VARIANTS = {
  primary:
    'bg-accent text-ink-inverse hover:bg-accent-hover active:bg-accent-active shadow-none hover:shadow-glow',
  secondary:
    'bg-transparent text-secondary border border-secondary/50 hover:bg-secondary-subtle hover:border-secondary',
  ghost: 'bg-transparent text-ink-primary hover:bg-surface',
  destructive: 'bg-error text-ink-inverse hover:brightness-110 active:brightness-95',
}

const SIZES = {
  sm: 'h-8 px-3 text-small gap-1.5',
  md: 'h-10 px-4 text-body gap-2',
  lg: 'h-12 px-6 text-body gap-2',
}

const SPINNER_SIZE = { sm: 'sm', md: 'sm', lg: 'md' }

const Button = forwardRef(function Button(
  {
    variant = 'primary',
    size = 'md',
    loading = false,
    disabled = false,
    fullWidth = false,
    className,
    children,
    type = 'button',
    ...rest
  },
  ref,
) {
  const isDisabled = disabled || loading

  return (
    <button
      ref={ref}
      type={type}
      disabled={isDisabled}
      aria-busy={loading}
      className={cn(
        'inline-flex items-center justify-center rounded-sm font-body font-medium transition-colors duration-150',
        'focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent',
        'disabled:opacity-50 disabled:pointer-events-none',
        VARIANTS[variant],
        SIZES[size],
        fullWidth && 'w-full',
        className,
      )}
      {...rest}
    >
      {loading && <Spinner size={SPINNER_SIZE[size]} color="border-white/25 border-t-current" />}
      {children}
    </button>
  )
})

export default Button
