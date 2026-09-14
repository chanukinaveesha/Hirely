/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        // Surface elevation levels (page -> card -> modal/dropdown)
        base: '#0B0D10',
        surface: '#14171C',
        elevated: '#1C2026',

        // Borders / hairlines
        line: {
          DEFAULT: '#262B31',
          strong: '#333940',
        },

        // Text ("ink" to avoid colliding with Tailwind's text- utility prefix)
        ink: {
          primary: '#EDEBE6',
          secondary: '#A6A29B',
          muted: '#6E6A63',
          inverse: '#0B0D10',
        },

        // Primary accent: warm amber/gold. Used sparingly & deliberately.
        accent: {
          DEFAULT: '#E0AC45',
          hover: '#EABD5F',
          active: '#C6923A',
          subtle: '#2E2818',
        },

        // Secondary accent: muted teal, for secondary actions/links.
        secondary: {
          DEFAULT: '#4F8F8A',
          hover: '#5FA39D',
          active: '#437A76',
          subtle: '#1B2624',
        },

        // Semantic colors, desaturated for dark UI.
        success: { DEFAULT: '#5FAE7B', subtle: '#1C2A20' },
        error: { DEFAULT: '#D2665F', subtle: '#2E1E1C' },
        warning: { DEFAULT: '#C99A4A', subtle: '#2B2416' },
        info: { DEFAULT: '#5A82A8', subtle: '#1A2430' },
      },
      fontFamily: {
        heading: ['"Space Grotesk"', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        body: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        none: '0px',
        xs: '2px', // badges/tags — deliberately sharp
        sm: '6px', // buttons, inputs, small controls
        DEFAULT: '6px',
        md: '10px',
        lg: '16px', // cards
        xl: '20px', // modals
        full: '9999px',
      },
      boxShadow: {
        // Reserved for the one place a real drop shadow is deliberate: modals.
        modal: '0 20px 60px -15px rgba(0, 0, 0, 0.6)',
        // A subtle glow for the primary accent on hover/focus, not a generic soft shadow.
        glow: '0 0 0 1px rgba(224, 172, 69, 0.35), 0 4px 24px -6px rgba(224, 172, 69, 0.35)',
      },
    },
  },
  plugins: [],
}
