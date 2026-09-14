import { cn } from '../../utils/cn'

export default function Table({ children, className }) {
  return (
    <div className={cn('overflow-x-auto rounded-lg border border-line', className)}>
      <table className="w-full border-collapse text-left text-body">{children}</table>
    </div>
  )
}

export function TableHead({ children }) {
  return <thead className="bg-elevated">{children}</thead>
}

export function TableBody({ children }) {
  return <tbody className="divide-y divide-line">{children}</tbody>
}

export function TableRow({ children, className, ...rest }) {
  return (
    <tr className={cn('transition-colors hover:bg-surface', className)} {...rest}>
      {children}
    </tr>
  )
}

export function TableHeaderCell({ children, className }) {
  return (
    <th className={cn('px-4 py-3 text-label text-ink-secondary', className)} scope="col">
      {children}
    </th>
  )
}

export function TableCell({ children, className }) {
  return <td className={cn('px-4 py-3 text-ink-primary', className)}>{children}</td>
}

export function TableEmpty({ colSpan = 1, message = 'No records found.' }) {
  return (
    <tr>
      <td colSpan={colSpan} className="px-4 py-10 text-center text-small text-ink-muted">
        {message}
      </td>
    </tr>
  )
}
