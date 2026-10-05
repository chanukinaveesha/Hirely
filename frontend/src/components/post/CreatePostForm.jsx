import { useEffect, useRef, useState } from 'react'
import { createPost } from '../../api/postApi'
import { getMyVacancies } from '../../api/vacancyApi'
import { useAuthStore } from '../../auth/authStore'
import { ROLES } from '../../utils/roles'
import { Button, Card, CardHeader, ErrorMessage, FormField, Select, TextArea, toast } from '../common'

const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024
const MAX_BODY_LENGTH = 2000

export default function CreatePostForm({ onCreated }) {
  const role = useAuthStore((state) => state.role)
  const canPost = role === ROLES.JOB_SEEKER || role === ROLES.RECRUITER || role === ROLES.HR_EXECUTIVE
  const type = role === ROLES.JOB_SEEKER ? 'PROFILE_SHOWCASE' : 'VACANCY_PROMO'

  const [body, setBody] = useState('')
  const [vacancyId, setVacancyId] = useState('')
  const [vacancies, setVacancies] = useState([])
  const [image, setImage] = useState(null)
  const [previewUrl, setPreviewUrl] = useState(null)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const fileInputRef = useRef(null)

  useEffect(() => {
    if (type !== 'VACANCY_PROMO') return
    getMyVacancies()
      .then((data) => setVacancies(data.filter((v) => v.status === 'PUBLISHED')))
      .catch(() => setVacancies([]))
  }, [type])

  function handleImageChange(event) {
    const file = event.target.files[0]
    event.target.value = ''
    if (!file) return

    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
      toast.error('Only JPG, PNG, or WEBP images are allowed.')
      return
    }
    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      toast.error('Image must be 2MB or smaller.')
      return
    }

    setImage(file)
    setPreviewUrl(URL.createObjectURL(file))
  }

  function clearImage() {
    setImage(null)
    setPreviewUrl(null)
  }

  function resetForm() {
    setBody('')
    setVacancyId('')
    clearImage()
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)

    if (type === 'VACANCY_PROMO' && !vacancyId) {
      setError('Choose one of your published vacancies to promote.')
      return
    }

    setSubmitting(true)
    try {
      const post = await createPost({ type, body, vacancyId: vacancyId || undefined, image })
      toast.success('Post published.')
      resetForm()
      onCreated?.(post)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (!canPost) return null

  return (
    <Card>
      <CardHeader
        title={type === 'VACANCY_PROMO' ? 'Promote a vacancy' : 'Showcase your profile'}
        description={
          type === 'VACANCY_PROMO'
            ? 'Share one of your own published vacancies with the feed.'
            : 'Introduce yourself to recruiters browsing the feed.'
        }
      />

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <ErrorMessage message={error} />

        {type === 'VACANCY_PROMO' && (
          <FormField label="Vacancy" htmlFor="post-vacancy">
            <Select id="post-vacancy" required value={vacancyId} onChange={(e) => setVacancyId(e.target.value)}>
              <option value="">Select a published vacancy…</option>
              {vacancies.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.title}
                </option>
              ))}
            </Select>
          </FormField>
        )}

        <FormField
          label="Body"
          htmlFor="post-body"
          helperText={`${body.length}/${MAX_BODY_LENGTH} characters`}
        >
          <TextArea
            id="post-body"
            required
            rows={4}
            maxLength={MAX_BODY_LENGTH}
            value={body}
            onChange={(e) => setBody(e.target.value)}
          />
        </FormField>

        <div>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/webp"
            className="hidden"
            onChange={handleImageChange}
          />
          {previewUrl ? (
            <div className="flex items-center gap-3">
              <img src={previewUrl} alt="Preview" className="h-20 w-20 rounded-md border border-line object-cover" />
              <Button type="button" variant="ghost" size="sm" onClick={clearImage}>
                Remove image
              </Button>
            </div>
          ) : (
            <Button type="button" variant="ghost" size="sm" onClick={() => fileInputRef.current.click()}>
              Add an image (optional)
            </Button>
          )}
          <p className="mt-1 text-small text-ink-muted">JPG, PNG, or WEBP. Up to 2MB.</p>
        </div>

        <div>
          <Button type="submit" loading={submitting}>
            Publish
          </Button>
        </div>
      </form>
    </Card>
  )
}
