import { Badge } from '../common'
import { APPLICATION_STATUS_BADGE_VARIANT } from '../../utils/applicationOptions'
import { formatEnumLabel } from '../../utils/vacancyOptions'

export default function ApplicationStatusBadge({ status }) {
  return <Badge variant={APPLICATION_STATUS_BADGE_VARIANT[status] ?? 'neutral'}>{formatEnumLabel(status)}</Badge>
}
