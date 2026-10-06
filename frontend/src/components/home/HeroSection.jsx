import { useLayoutEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../auth/authStore'
import { HOME_ROUTE_BY_ROLE } from '../brand/HirelyLogo'
import { Button } from '../common'
import hero1 from '../../assets/home/hero-1.jpeg'
import hero2 from '../../assets/home/hero-2.jpeg'

// `width: 100vw` + `margin-left: -50vw` is the usual full-bleed trick, but
// 100vw includes the scrollbar gutter while the page's real visible width
// doesn't, so it overshoots by the scrollbar's width and causes a faint
// horizontal scrollbar. Measuring the parent's actual position/width avoids
// that without touching global CSS (out of scope for this component).
function useFullBleed() {
  const ref = useRef(null)
  const [style, setStyle] = useState(undefined)

  useLayoutEffect(() => {
    function measure() {
      const parent = ref.current?.parentElement
      if (!parent) return
      setStyle({
        marginLeft: -parent.getBoundingClientRect().left,
        width: document.documentElement.clientWidth,
      })
    }
    measure()
    window.addEventListener('resize', measure)
    return () => window.removeEventListener('resize', measure)
  }, [])

  return [ref, style]
}

export default function HeroSection() {
  const navigate = useNavigate()
  const role = useAuthStore((state) => state.role)
  const dashboardRoute = HOME_ROUTE_BY_ROLE[role] ?? '/login'
  const [bleedRef, bleedStyle] = useFullBleed()

  return (
    <section ref={bleedRef} style={bleedStyle} className="relative -mt-6 bg-base py-16 sm:py-24">
      <div className="bg-glow pointer-events-none absolute inset-0" aria-hidden="true" />

      <div className="relative mx-auto grid max-w-7xl grid-cols-1 items-center gap-12 px-6 md:grid-cols-2">
        <div className="flex flex-col items-start gap-6 text-left">
          <h1 className="text-5xl font-bold leading-tight text-ink-primary md:text-7xl">
            Get hired <span className="text-accent">:)</span>
          </h1>
          <p className="max-w-md text-body text-ink-secondary">
            Find your next role, showcase your work, and stay in the loop with what's happening
            across Hirely.
          </p>
          <div className="flex flex-col gap-3 sm:flex-row">
            <Button
              variant="primary"
              size="lg"
              className="!rounded-full"
              onClick={() => navigate(dashboardRoute)}
            >
              Dashboard
            </Button>
            <Button
              variant="secondary"
              size="lg"
              className="!rounded-full"
              onClick={() => navigate('/posts')}
            >
              Posts
            </Button>
          </div>
        </div>

        <div className="relative mx-auto aspect-square w-full max-w-md md:ml-auto md:mr-0">
          <div
            className="pointer-events-none absolute -top-8 -left-8 h-40 w-40 rounded-full bg-accent/30 blur-3xl"
            aria-hidden="true"
          />
          <div
            className="pointer-events-none absolute top-1/3 -right-6 h-32 w-32 rounded-full bg-secondary/30 blur-3xl"
            aria-hidden="true"
          />
          <div
            className="pointer-events-none absolute bottom-0 left-1/4 h-28 w-28 rounded-full bg-info/25 blur-3xl"
            aria-hidden="true"
          />

          <span className="absolute left-2 top-6 h-2 w-2 rounded-full bg-accent" aria-hidden="true" />
          <span className="absolute right-4 top-2 h-1.5 w-1.5 rounded-full bg-secondary" aria-hidden="true" />
          <span className="absolute bottom-10 right-0 h-2 w-2 rounded-full bg-info" aria-hidden="true" />
          <span className="absolute bottom-2 left-10 h-1.5 w-1.5 rounded-full bg-accent" aria-hidden="true" />
          <span className="absolute right-10 top-1/2 h-1 w-1 rounded-full bg-ink-secondary" aria-hidden="true" />
          <span className="absolute bottom-1/3 left-0 h-1.5 w-1.5 rounded-full bg-secondary" aria-hidden="true" />

          <img
            src={hero2}
            width={906}
            height={1200}
            decoding="async"
            alt="A candidate smiling while reviewing a new job offer on a laptop"
            className="absolute right-0 top-0 h-[70%] w-[65%] rounded-[40%_60%_55%_45%/50%_45%_55%_50%] border border-line object-cover shadow-modal"
          />
          <img
            src={hero1}
            width={626}
            height={626}
            decoding="async"
            alt="Two colleagues shaking hands after a successful interview"
            className="absolute bottom-0 left-0 h-[55%] w-[55%] rounded-[55%_45%_45%_55%/45%_55%_45%_55%] border border-line object-cover shadow-modal"
          />

          <div className="absolute -bottom-4 right-4 flex items-center gap-2 rounded-full border border-line bg-surface/70 px-3 py-2 text-small text-ink-primary shadow-modal backdrop-blur-md">
            <span
              className="flex h-6 w-6 items-center justify-center rounded-full bg-accent/20 text-xs"
              aria-hidden="true"
            >
              🎯
            </span>
            New: 12 roles posted today
          </div>
        </div>
      </div>
    </section>
  )
}
