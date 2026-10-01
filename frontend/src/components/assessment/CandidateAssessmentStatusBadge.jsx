import { Badge } from '../common'
import { CANDIDATE_ASSESSMENT_STATUS_BADGE_VARIANT } from '../../utils/assessmentOptions'
import { formatEnumLabel } from '../../utils/vacancyOptions'

export default function CandidateAssessmentStatusBadge({ status }) {
  return <Badge variant={CANDIDATE_ASSESSMENT_STATUS_BADGE_VARIANT[status] ?? 'neutral'}>{formatEnumLabel(status)}</Badge>
}
