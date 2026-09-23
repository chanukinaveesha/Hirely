import { cn } from '../../utils/cn'

const SIZES = {
  sm: 'h-8 w-8 text-small',
  md: 'h-10 w-10 text-body',
  lg: 'h-14 w-14 text-h4',
}

// Deterministic accent picked from the name, so the same person always
// gets the same color without needing to store one.
const PALETTE = [
  'bg-accent-subtle text-accent',
  'bg-secondary-subtle text-secondary',
  'bg-info-subtle text-info',
  'bg-success-subtle text-success',
]

function initialsFor(name) {
  if (!name) return '?'
  const parts = name.trim().split(/\s+/)
  const first = parts[0]?.[0] ?? ''
  const last = parts.length > 1 ? parts[parts.length - 1][0] : ''
  return (first + last).toUpperCase()
}

function colorFor(name) {
  if (!name) return PALETTE[0]
  const hash = Array.from(name).reduce((sum, char) => sum + char.charCodeAt(0), 0)
  return PALETTE[hash % PALETTE.length]
}

export default function Avatar({ name, src, size = 'md', className }) {
  if (src) {
    return (
      <img
        src={src}
        alt={name ?? 'User avatar'}
        className={cn('rounded-full object-cover', SIZES[size], className)}
      />
    )
  }

  return (
    <span
      className={cn(
        'inline-flex select-none items-center justify-center rounded-full font-heading font-semibold',
        SIZES[size],
        colorFor(name),
        className,
      )}
      aria-hidden={!name}
      title={name}
    >
      {initialsFor(name)}
    </span>
  )
}
