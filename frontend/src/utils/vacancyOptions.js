// Mirrors com.recruitsystem.entity.vacancy.VacancyCategory / VacancyStatus.
export const VACANCY_CATEGORIES = [
  'IT',
  'ENGINEERING',
  'FINANCE',
  'MARKETING',
  'SALES',
  'HUMAN_RESOURCES',
  'CUSTOMER_SERVICE',
  'DESIGN',
  'OPERATIONS',
  'OTHER',
]

export const VACANCY_STATUS_BADGE_VARIANT = {
  DRAFT: 'neutral',
  PUBLISHED: 'success',
  CLOSED: 'error',
}

export function formatEnumLabel(value) {
  return value.replaceAll('_', ' ')
}
