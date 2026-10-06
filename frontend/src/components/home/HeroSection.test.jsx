import { beforeEach, describe, expect, it } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import HeroSection from './HeroSection'
import { useAuthStore } from '../../auth/authStore'

function renderHero() {
  render(
    <MemoryRouter initialEntries={['/home']}>
      <Routes>
        <Route path="/home" element={<HeroSection />} />
        <Route path="/candidate" element={<div>Candidate dashboard page</div>} />
        <Route path="/posts" element={<div>Posts feed page</div>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('HeroSection', () => {
  beforeEach(() => {
    useAuthStore.setState({ role: 'JOB_SEEKER' })
  })

  it('sends the Dashboard button to the signed-in role\'s home route', () => {
    renderHero()

    fireEvent.click(screen.getByRole('button', { name: /dashboard/i }))

    expect(screen.getByText('Candidate dashboard page')).toBeInTheDocument()
  })

  it('sends the Posts button to /posts', () => {
    renderHero()

    fireEvent.click(screen.getByRole('button', { name: /posts/i }))

    expect(screen.getByText('Posts feed page')).toBeInTheDocument()
  })
})
