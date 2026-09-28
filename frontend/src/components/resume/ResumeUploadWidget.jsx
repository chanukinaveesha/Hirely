import { useEffect, useRef, useState } from 'react'
import { downloadMyResume, getMyResume, uploadResume } from '../../api/resumeApi'
import { Button, Card, CardHeader, LoadingSpinner, toast } from '../common'

const ALLOWED_EXTENSIONS = ['pdf', 'docx']
const MAX_SIZE_BYTES = 5 * 1024 * 1024

function formatSize(bytes) {
  return `${(bytes / 1024).toFixed(0)} KB`
}

export default function ResumeUploadWidget() {
  const [resume, setResume] = useState(null)
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [downloading, setDownloading] = useState(false)
  const fileInputRef = useRef(null)

  function loadResume() {
    setLoading(true)
    getMyResume()
      .then(setResume)
      .catch(() => setResume(null))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadResume()
  }, [])

  async function handleFileChange(event) {
    const file = event.target.files[0]
    event.target.value = ''
    if (!file) return

    const extension = file.name.split('.').pop()?.toLowerCase()
    if (!ALLOWED_EXTENSIONS.includes(extension)) {
      toast.error('Only PDF or DOCX files are allowed.')
      return
    }
    if (file.size > MAX_SIZE_BYTES) {
      toast.error('File must be 5MB or smaller.')
      return
    }

    setUploading(true)
    try {
      const updated = await uploadResume(file)
      setResume(updated)
      toast.success(resume ? 'Resume replaced.' : 'Resume uploaded.')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setUploading(false)
    }
  }

  async function handleDownload() {
    setDownloading(true)
    try {
      await downloadMyResume()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setDownloading(false)
    }
  }

  return (
    <Card>
      <CardHeader title="My resume" description="Upload a PDF or DOCX, up to 5MB. Uploading again replaces it." />

      {loading ? (
        <LoadingSpinner label="Loading resume..." />
      ) : (
        <div className="flex flex-col gap-4">
          {resume ? (
            <div className="flex items-center justify-between rounded-sm border border-line bg-surface px-4 py-3">
              <div>
                <p className="text-body text-ink-primary">{resume.originalFilename}</p>
                <p className="text-small text-ink-muted">
                  {resume.fileType} · {formatSize(resume.fileSizeBytes)}
                </p>
              </div>
              <Button variant="secondary" size="sm" loading={downloading} onClick={handleDownload}>
                Download
              </Button>
            </div>
          ) : (
            <p className="text-body text-ink-secondary">You haven't uploaded a resume yet.</p>
          )}

          <div>
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf,.docx"
              className="hidden"
              onChange={handleFileChange}
            />
            <Button variant={resume ? 'ghost' : 'primary'} loading={uploading} onClick={() => fileInputRef.current.click()}>
              {resume ? 'Replace resume' : 'Upload resume'}
            </Button>
          </div>
        </div>
      )}
    </Card>
  )
}
