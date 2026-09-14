// Joins conditional class names without pulling in a dependency.
// Usage: cn('base-class', condition && 'conditional-class', props.className)
export function cn(...values) {
  return values.filter(Boolean).join(' ')
}
