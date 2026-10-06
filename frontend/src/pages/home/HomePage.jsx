import HeroSection from '../../components/home/HeroSection'
import LatestPostsSection from '../../components/home/LatestPostsSection'

export default function HomePage() {
  return (
    <div className="flex flex-col gap-10">
      <HeroSection />
      <LatestPostsSection />
    </div>
  )
}
