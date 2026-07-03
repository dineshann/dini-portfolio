# Dinesh — Portfolio Site

A single-page, dark/light-mode portfolio built with Tailwind CSS (CDN), vanilla JS, and Google Fonts. No build step required — just open `index.html` in a browser.

## Structure

```
index.html   → entire site (markup, styles, and script in one file)
img/dini.png → portrait photo (falls back to a generated avatar if missing)
```

## Layout

- **Sidebar** (`<aside>`) — portrait, name/title, social links, "Hire Me" CTA, Core Focus tags, Vision quote, Connect icons, animated Skills bars.
  - Desktop (`lg` and up): `position: sticky; top: 0`. It sticks to the top of the viewport while shorter than it, then scrolls with the page once its content runs past the fold — no independent scrollbar.
  - Mobile: stacks above the main content as a normal block.
- **Main** (`<main>`) — Impact & Craft, Featured Work, Career Trajectory, Core Competencies, and a dark "What I'm Building Next" closing section with a contact CTA.

## Tech

| Piece | Source |
|---|---|
| Styling | Tailwind CSS via CDN (`forms`, `typography` plugins) + a small custom `<style>` block for grain texture, skill bars, hover lifts, and the sticky sidebar |
| Fonts | Inter (body) + Fraunces (display/italic) + Material Symbols Outlined (icons) via Google Fonts |
| Dark mode | Tailwind `class` strategy, toggled by the floating button (bottom-right); defaults to dark on load (`<html class="dark">`) |
| Skill bars | Rendered from a JS array (`skills`) and animated via `IntersectionObserver` when scrolled into view |
| Motion | Respects `prefers-reduced-motion` (disables transitions/animations) |

## Customizing

- **Portrait**: replace `img/dini.png`. If missing, an initials avatar is auto-generated.
- **Skills**: edit the `skills` array near the bottom of `index.html` — `{ label, pct }` pairs.
- **Color palette**: edit the `tailwind.config` block in `<head>` (`primary`, `primary-bright`, `sapphire`, `background-light`, `background-dark`).
- **Copy/links**: all content (bio, career history, featured work, contact links) is inline HTML in the relevant `<section>` — no CMS or data file.
- **Copyright year**: auto-updates via JS on page load.

## Browser support

Modern evergreen browsers. Uses `IntersectionObserver`, CSS `sticky`, and CSS custom properties (`--target-width`) — no polyfills included.

## Known limitations

- Tailwind is loaded via CDN (`cdn.tailwindcss.com`), which is fine for a static personal site but not recommended for production apps needing PurgeCSS/tree-shaking — consider a proper Tailwind build step if this grows beyond a single page.
- No routing/framework — it's intentionally a single static HTML file.