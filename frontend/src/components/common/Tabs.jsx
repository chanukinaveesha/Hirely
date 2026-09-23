import { cn } from '../../utils/cn'

/**
 * Controlled tab list for switching between sections of a page.
 * tabs: [{ key, label }], activeKey: string, onChange: (key) => void
 */
export default function Tabs({ tabs, activeKey, onChange, className }) {
  return (
    <div className={cn('flex gap-6 border-b border-line', className)} role="tablist">
      {tabs.map((tab) => {
        const isActive = tab.key === activeKey
        return (
          <button
            key={tab.key}
            role="tab"
            aria-selected={isActive}
            onClick={() => onChange(tab.key)}
            className={cn(
              'relative -mb-px border-b-2 px-1 py-3 text-small font-medium transition-colors',
              isActive
                ? 'border-accent text-ink-primary'
                : 'border-transparent text-ink-secondary hover:text-ink-primary',
            )}
          >
            {tab.label}
          </button>
        )
      })}
    </div>
  )
}
