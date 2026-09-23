import { cn } from '../../utils/cn'

/** Pulsing placeholder block for async content. Compose with utility classes for shape. */
export default function Skeleton({ className }) {
  return <div className={cn('animate-pulse rounded-sm bg-surface', className)} />
}

/** Stacked text-line placeholder, e.g. for a card that's still loading. */
export function SkeletonText({ lines = 3, className }) {
  return (
    <div className={cn('flex flex-col gap-2', className)}>
      {Array.from({ length: lines }).map((_, index) => (
        <Skeleton key={index} className={cn('h-3', index === lines - 1 ? 'w-2/3' : 'w-full')} />
      ))}
    </div>
  )
}
