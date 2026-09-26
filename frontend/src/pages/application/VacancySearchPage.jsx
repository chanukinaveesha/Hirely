import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { searchVacancies } from '../../api/vacancyApi'
import { VACANCY_CATEGORIES, formatEnumLabel } from '../../utils/vacancyOptions'
import { Badge, Button, Card, FormField, Input, LoadingSpinner, Select, toast } from '../../components/common'

const emptyFilters = { title: '', category: '', location: '', keyword: '' }

export default function VacancySearchPage() {
  const [filters, setFilters] = useState(emptyFilters)
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(true)

  function runSearch(activeFilters) {
    setLoading(true)
    searchVacancies(activeFilters)
      .then(setResults)
      .catch((err) => toast.error(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    runSearch(emptyFilters)
  }, [])

  function handleSubmit(event) {
    event.preventDefault()
    runSearch(filters)
  }

  return (
    <div className="flex flex-col gap-6">
      <Card>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <FormField label="Title" htmlFor="search-title">
            <Input
              id="search-title"
              value={filters.title}
              onChange={(e) => setFilters({ ...filters, title: e.target.value })}
            />
          </FormField>

          <FormField label="Category" htmlFor="search-category">
            <Select
              id="search-category"
              value={filters.category}
              onChange={(e) => setFilters({ ...filters, category: e.target.value })}
            >
              <option value="">All categories</option>
              {VACANCY_CATEGORIES.map((category) => (
                <option key={category} value={category}>
                  {formatEnumLabel(category)}
                </option>
              ))}
            </Select>
          </FormField>

          <FormField label="Location" htmlFor="search-location">
            <Input
              id="search-location"
              value={filters.location}
              onChange={(e) => setFilters({ ...filters, location: e.target.value })}
            />
          </FormField>

          <FormField label="Keyword" htmlFor="search-keyword" helperText="Searches title, description, requirements">
            <Input
              id="search-keyword"
              value={filters.keyword}
              onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            />
          </FormField>

          <div className="sm:col-span-2 lg:col-span-4">
            <Button type="submit">Search</Button>
          </div>
        </form>
      </Card>

      {loading ? (
        <LoadingSpinner label="Searching vacancies..." />
      ) : results.length === 0 ? (
        <Card>
          <p className="text-body text-ink-secondary">No vacancies match your search.</p>
        </Card>
      ) : (
        <div className="flex flex-col gap-3">
          {results.map((vacancy) => (
            <Link key={vacancy.id} to={`/vacancies/${vacancy.id}`}>
              <Card className="transition-colors hover:border-line-strong">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h3 className="text-h4 text-ink-primary">{vacancy.title}</h3>
                    <p className="mt-1 text-small text-ink-secondary">
                      {vacancy.clientCompanyName} · {vacancy.location || 'Location not specified'}
                    </p>
                  </div>
                  <Badge variant="accent">{formatEnumLabel(vacancy.category)}</Badge>
                </div>
                <p className="mt-3 text-small text-ink-muted">Apply by {vacancy.deadline}</p>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
