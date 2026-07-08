# amortree-landingpage

Dinesh's personal portfolio site — a single-page, self-contained resume/landing page built with Tailwind CSS. Presents a decade of UI/UX and front-end work in an editorial, platinum-on-charcoal "premium" visual style, with a light mode alternative.

**Live structure:** fixed sidebar (portrait, vision, skills, contact) + scrolling main content (impact highlights, featured work, career timeline, core competencies, and a closing call-to-action).

## Tech

- Static HTML, no build step or dependencies to install
- [Tailwind CSS](https://tailwindcss.com) via the CDN script (JIT, `forms` + `typography` plugins)
- Google Fonts: **Fraunces** (display/serif) + **Inter** (body/sans)
- Google **Material Symbols Outlined** for iconography
- Vanilla JS for the skill bars, scroll-triggered animation, dark mode toggle, and dynamic copyright year

## Structure

```
dini-portfolio/
├── index.html      # entire site — markup, Tailwind config, styles, and JS all live here
├── img/
│   └── dini.png     # portrait used in the sidebar
└── README.md
```

## Design system

| Token | Value | Used for |
|---|---|---|
| `primary` | `#9BB0C2` (brushed platinum) | accents, links, buttons, icons |
| `primary-bright` | `#EDF2F5` (icy silver) | gradient highlights |
| `sapphire` | `#16324F` | secondary jewel-tone accent (Featured Work) |
| `background-dark` | `#0B0D10` | dark mode base |
| `background-light` | `#F5F4F1` | light mode base |
| Display type | Fraunces (italic for editorial emphasis) | headings, pull quotes |
| Body type | Inter | paragraphs, labels, UI text |

Dark mode is the default (toggle bottom-right, persists only for the session — no `localStorage`, so it resets on reload). A subtle dot-grain texture, platinum hairline dividers, and a monogram "seal" badge on the portrait are the signature premium touches.

## Running locally

No build tools needed — open `index.html` directly in a browser, or serve it:

```bash
python3 -m http.server 8000
# then visit http://localhost:8000
```

## Editing content

- **Skills / proficiency bars:** edit the `skills` array near the bottom of `index.html` (`{ label, pct }` pairs) — bars render and animate automatically.
- **Career timeline / featured work / competencies:** each is a repeated card block in the corresponding `<section>` — copy an existing card and edit the text/icon.
- **Portrait:** replace `img/dini.png` (same filename) or update the `src` path. An avatar fallback (`ui-avatars.com`) is wired up via `onerror` in case the image fails to load.
- **Contact links:** update the `mailto:`, `tel:`, and social `href`s in the sidebar's Connect / Social block.

## Accessibility notes

- All icon-only links have `aria-label`s in addition to `title`s.
- Visible platinum focus rings on all interactive elements (`:focus-visible`).
- Animations and transitions are disabled for users with `prefers-reduced-motion: reduce`.
- External links open with `rel="noopener"`.

## Known limitations

- Tailwind is compiled client-side via CDN script — there's no purge/build step, so first paint depends on the CDN and Google Fonts loading (no offline fallback).
- Dark/light preference isn't persisted between visits by design (kept intentionally simple, no storage).