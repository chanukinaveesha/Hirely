import { Badge } from '../common'
import { VACANCY_STATUS_BADGE_VARIANT, formatEnumLabel } from '../../utils/vacancyOptions'

export default function VacancyStatusBadge({ status }) {
  return <Badge variant={VACANCY_STATUS_BADGE_VARIANT[status] ?? 'neutral'}>{formatEnumLabel(status)}</Badge>
}
