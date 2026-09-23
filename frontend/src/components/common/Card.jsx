import { cn } from '../../utils/cn'

const PADDING = {
  none: '',
  sm: 'p-4',
  md: 'p-6',
  lg: 'p-8',
}

/**
 * Base surface for grouped content. Depth comes from the surface/border
 * color shift against the page background, not a drop shadow.
 */
export default function Card({ elevated = false, padding = 'md', className, children, ...rest }) {
  return (
    <div
      className={cn(
        'rounded-lg border',
        elevated ? 'bg-elevated border-line-strong' : 'bg-surface border-line',
        PADDING[padding],
        className,
      )}
      {...rest}
    >
      {children}
    </div>
  )
}

export function CardHeader({ title, description, action, className }) {
  return (
    <div className={cn('mb-5 flex items-start justify-between gap-4', className)}>
      <div>
        <h3 className="text-h4 text-ink-primary">{title}</h3>
        {description && <p className="mt-1 text-small text-ink-secondary">{description}</p>}
      </div>
      {action}
    </div>
  )
}
