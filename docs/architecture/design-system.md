# Frontend design system

The frontend has one theme: dark. There is no light mode toggle yet. This
document is the source of truth for the visual tokens and shared components
in `frontend/src/components/common/`. **All six feature modules must build
their screens out of these shared components rather than writing their own
button/input/card styling.** If a screen needs something the system doesn't
cover, extend a shared component (or ask before adding a new one-off style).

## Color tokens

Defined in `frontend/tailwind.config.js` under `theme.extend.colors`. Use the
Tailwind utility name shown, not raw hex values, in any component.

| Token | Utility example | Use for |
|---|---|---|
| `base` | `bg-base` | Page background (near-black, `#0B0D10`) |
| `surface` | `bg-surface` | Card/panel background — one step up from the page |
| `elevated` | `bg-elevated` | Modals, dropdowns, toasts — one step up from `surface` |
| `line` / `line-strong` | `border-line`, `border-line-strong` | Hairline borders between surfaces; `strong` for modal/emphasis borders |
| `ink-primary` | `text-ink-primary` | Primary body/heading text |
| `ink-secondary` | `text-ink-secondary` | Secondary text, nav links, helper copy |
| `ink-muted` | `text-ink-muted` | Placeholder text, disabled/least-important text |
| `ink-inverse` | `text-ink-inverse` | Text on top of a filled accent/error button |
| `accent` (+`-hover`/`-active`/`-subtle`) | `bg-accent`, `text-accent` | **Primary** actions and key highlights only — warm amber/gold. Don't use it decoratively; it should stay special-purpose. |
| `secondary` (+`-hover`/`-active`/`-subtle`) | `text-secondary`, `border-secondary` | Secondary actions, links, secondary button outline — muted teal |
| `success` / `error` / `warning` / `info` (+`-subtle`) | `bg-success-subtle text-success` | Status colors — application states, form validation, toasts |

Never reach for Tailwind's default `blue-*`, `purple-*`, `gray-*`, etc.
palettes directly in a component — if a token you need doesn't exist, add it
to the config rather than using an arbitrary color.

## Type scale

Defined as classes in `frontend/src/styles/index.css` under `@layer
components`. Hierarchy comes from family + weight + tracking, not just size:

| Class | Use for |
|---|---|
| `.text-h1` | Page-level heading (rare — one per page at most) |
| `.text-h2` | Section heading (e.g. the auth card title) |
| `.text-h3` | Sub-section heading |
| `.text-h4` | Card title (see `CardHeader`) |
| `.text-body` | Default paragraph text |
| `.text-small` | Secondary/help text, table cells, nav links |
| `.text-label` | Form labels, badges, table header cells — uppercase, tracked out |

Headings use `font-heading` (Space Grotesk); body copy uses `font-body`
(Inter). Both are loaded via Google Fonts `<link>` tags in `index.html`.

## Radius scale

Two distinct radii, plus a deliberately sharp one — not one rounded value
applied everywhere:

- `rounded-xs` (2px) — badges/tags only. This is the intentional "sharp
  corner" accent that keeps the UI from reading as soft-rounded-everything.
- `rounded-sm` (6px) — buttons, inputs, selects, small controls.
- `rounded-lg` (16px) — cards.
- `rounded-xl` (20px) — modals.
- `rounded-full` — avatars, pills.

## Elevation

Depth comes from the `base` → `surface` → `elevated` background steps and
`line` → `line-strong` borders, not drop shadows. The one deliberate
exception is the `Modal`, which uses the `shadow-modal` token (a real,
soft-diffused shadow) because it needs to visually separate from everything
behind it. Buttons use `shadow-glow` (a tight amber glow) on hover instead of
a generic shadow.

## Gradients

Used exactly once, deliberately: `.bg-glow` in `index.css` — a barely-there
radial amber glow behind the auth card. Don't add a second gradient without
a specific reason; this is not a decorative pattern to reach for by default.

## Components (`frontend/src/components/common/`)

Import from the barrel: `import { Button, Input, Card } from
'../../components/common'`.

| Component | When to use |
|---|---|
| `Button` | Any clickable action. Variants: `primary` (amber, one per view for the main action), `secondary` (teal outline, secondary actions), `ghost` (low-emphasis, e.g. logout), `destructive` (delete/remove). Sizes `sm`/`md`/`lg`. Supports `loading` and `disabled`. |
| `Input` / `TextArea` / `Select` | Text/number/date, multi-line, and choice fields respectively. Accept an `error` boolean to switch to the error border. |
| `FormField` | Wraps a label + one input + helper/error text. Always use this around form controls instead of hand-rolling a `<label>`. |
| `Card` / `CardHeader` | Grouped content block. Use `CardHeader` for a title + optional description + action. |
| `Modal` | Dialogs/confirmations. Renders via a portal; closes on `Escape` or backdrop click. |
| `Table`, `TableHead`, `TableBody`, `TableRow`, `TableHeaderCell`, `TableCell`, `TableEmpty` | Any tabular list (vacancies, applications, interviews...). Compound components — compose your own columns/rows rather than passing a generic `columns` prop, since each module's data shape differs. |
| `Badge` | Status indicators — application status, account status, tags. Variants match the semantic color tokens plus `neutral`/`accent`/`secondary`. |
| `Avatar` | User profile picture with initials fallback (deterministic color per name). |
| `ToastContainer` + `toast` | Mount `ToastContainer` once at the app root (already done in `App.jsx`). Call `toast.success(message)` / `toast.error(message)` / `toast.warning(message)` / `toast.info(message)` from anywhere, including outside React components (e.g. `apiClient`'s interceptor already does this for network/server errors). |
| `LoadingSpinner` / `Spinner` | `LoadingSpinner` for a page/section loading state (icon + label). `Spinner` is the bare icon, used inside `Button`'s `loading` state. |
| `Skeleton` / `SkeletonText` | Placeholder blocks while async content loads. |
| `Tabs` | Controlled tab list for switching sections within a page (not for top-level portal navigation — that's handled by `PortalHeader`). |
| `ErrorMessage` | Banner-level error (e.g. "login failed"). For field-specific errors, use `FormField`'s `error` prop instead. |

## Layout shells

`frontend/src/layouts/{Candidate,Recruiter,Admin}Layout.jsx` each render a
shared `PortalHeader` (title + nav links + user avatar + logout) and an
`Outlet`. Add new nav items to a layout's `NAV_ITEMS` array — don't
duplicate the header markup.
