// Mirrors com.recruitsystem.entity.application.ApplicationStatus.
export const APPLICATION_STATUS_BADGE_VARIANT = {
  SUBMITTED: 'info',
  UNDER_REVIEW: 'warning',
  SHORTLISTED: 'accent',
  INTERVIEWING: 'secondary',
  ASSESSED: 'secondary',
  SELECTED: 'success',
  REJECTED: 'error',
  WITHDRAWN: 'neutral',
}

export const WITHDRAWABLE_STATUSES = ['SUBMITTED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEWING', 'ASSESSED']
