import { Card, CardHeader } from '../../components/common'
import { formatEnumLabel } from '../../utils/vacancyOptions'

function StatTile({ label, value }) {
  return (
    <div className="rounded-sm border border-line bg-base px-4 py-3">
      <p className="text-h3 text-ink-primary">{value}</p>
      <p className="text-small text-ink-secondary">{label}</p>
    </div>
  )
}

export default function AdminView({ data }) {
  const roleEntries = Object.entries(data.usersByRole ?? {})

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card>
        <CardHeader title="Totals" />
        <div className="grid grid-cols-2 gap-3">
          <StatTile label="Total users" value={data.totalUsers} />
          <StatTile label="Total vacancies" value={data.totalVacancies} />
        </div>
      </Card>

      <Card>
        <CardHeader title="Users by role" />
        <div className="grid grid-cols-2 gap-3">
          {roleEntries.map(([role, count]) => (
            <StatTile key={role} label={formatEnumLabel(role)} value={count} />
          ))}
        </div>
      </Card>
    </div>
  )
}
