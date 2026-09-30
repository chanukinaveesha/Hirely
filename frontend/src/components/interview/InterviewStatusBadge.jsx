import { Badge } from '../common'
import { INTERVIEW_STATUS_BADGE_VARIANT } from '../../utils/interviewOptions'
import { formatEnumLabel } from '../../utils/vacancyOptions'

export default function InterviewStatusBadge({ status }) {
  return <Badge variant={INTERVIEW_STATUS_BADGE_VARIANT[status] ?? 'neutral'}>{formatEnumLabel(status)}</Badge>
}
