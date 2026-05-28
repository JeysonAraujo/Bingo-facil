# Story 2.2: Dual-Viewport Responsive Player Layout & Onboarding Guide

Status: done

## Story

As an activated player,
I want to be greeted by a 3-step interactive onboarding guide and view the YouTube live frame and my ticket cards in an accessible responsive layout,
so that I can understand how to play and navigate comfortably on mobile, tablet, or desktop screen viewports.

## Acceptance Criteria

1. **Given** an active player session loaded in the browser
2. **When** the player game room is loaded for the first time
3. **Then** an interactive 3-step progressive highlight tutorial displays, highlighting the YouTube stream player, the Cartelão (cards) area, and the "BINGO!" button
4. **And** the page renders responsively:
   - **Desktop-first (`md` and up):** A two-column view (left: video/chat/tabs; right: 2x2 grid of all 4 active cards and the BINGO claim button).
   - **Mobile-first (`sm` and down):** A top-down vertical stack (top: YouTube sync player; middle: numerical card selector/mini-map; bottom: carousel displaying exactly 1 active `BingoCard` at a time).
5. **And** a premium, high-impact golden action button is displayed to let players claim "BINGO!", triggering a celebratory screen overlay with animated assets and confetti.
6. **And** first-time users can bypass or complete the tour safely, persisting the completed state in `localStorage` so it does not auto-trigger on subsequent refreshes, while a "Como Jogar" help button remains permanent in the navigation bar to replay the tour.

## Tasks / Subtasks

- [x] **Task 1: Reusable Bingo Card Component** (AC: 4, UX-DR4)
  - [x] Create `BingoCard.vue` in `bingo-frontend/src/components/player/`.
  - [x] Render B-I-N-G-O letters headers in bold Royal Blue / Gold gradations.
  - [x] Format the 3rd row / 3rd column cell as a special "★ GRÁTIS" (FREE) space with a custom CSS `spin-pulse` animation.
  - [x] Bind marked cell styling to a computed check mapping the `markedNumbers` prop, invoking a green pulsating radial background with `pop-in` animations.
  - [x] Add high-contrast text ratios and subtle responsive layouts to keep columns aligned under narrow viewports.

- [x] **Task 2: Custom Spotlight Onboarding Tour** (AC: 2, 3, 6, UX-DR1)
  - [x] Create `OnboardingTour.vue` in `bingo-frontend/src/components/player/`.
  - [x] Implement standard Vue Teleport targeting `body` to overlay the entire game viewport.
  - [x] Build a reactive bounding client rect listener utilizing target element selectors:
    - Step 1: `#youtube-stream-container` (YouTube Stream)
    - Step 2: `#bingo-cards-container` (Cartelas Grid/Carousel)
    - Step 3: `#bingo-claim-button-container` (BINGO button)
  - [x] Apply a CSS custom shadow mask (`box-shadow: 0 0 0 9999px ...`) to darken the screen, keeping only the focused bounding rect clear as a rounded spotlight.
  - [x] Add a pulsing orange boundary border (`spotlight-pulse`) to guide the player's focus.
  - [x] Code smart coordinate calculations to position the informative tooltip overlay above or below the spotlight safely.
  - [x] Bind navigation keys (Next, Back, Skip) and store completed state in `localStorage` as `bingo_onboarding_completed`.

- [x] **Task 3: Dual-Viewport Responsive Dashboard** (AC: 4, 6, UX-DR3)
  - [x] Completely refactor `GameRoom.vue` inside `bingo-frontend/src/components/player/`.
  - [x] Embed responsive YouTube player iframe inside a standard 16:9 container, loading an atmospheric ambient stream or live video.
  - [x] **Desktop View Layout:** Write a `v-row` using Vuetify display helpers (`d-none d-md-grid` and grids) splitting stream, prize structure configurations, and a simulated chat on the left column, while the right column shows the 4 cards in a 2x2 grid and the golden "BINGO!" button.
  - [x] **Mobile View Layout:** Write a stacked layout (`d-flex d-md-none`) featuring the stream at the top, a numerical mini-map horizontal index (buttons 1-4) in the middle, and a clean Vuetify `v-carousel` displaying 1 card at a time at the bottom, synchronized two-way.
  - [x] **Simulated Live Chat:** Code a simulated chat manager that appends realistic messages from different virtual players every 9 seconds, bringing the room to life and supporting real-time player input.

- [x] **Task 4: Interactive Victory Claim Dialog** (AC: 5, UX-DR6)
  - [x] Create a large golden claim button in `GameRoom.vue` using gold linear gradients, active translation transitions, and a looping metallic reflections overlay (`shine`).
  - [x] Build a full-screen celebratory `v-dialog` card showing the player's active serial code when clicked.
  - [x] Program 30 unique CSS-animated falling confetti pieces (`@keyframes fall` and `transform rotate`) that drift, swing, and spin down the dialog card, providing premium visual feedback without heavy external packages.
  - [x] Include a permanent "Como Jogar" guide trigger in the navigation bar to run the tour at any time.

- [x] **Task 5: Compilation and Bundling Verification**
  - [x] Run `npm run type-check` to verify complete TypeScript compiling safety.
  - [x] Run `npm run build-only` to verify successful production asset packaging.

### Review Findings

- [x] [Review][Patch] Infinite Scroll Loop in Onboarding Spotlight [src/components/player/OnboardingTour.vue:104]
- [x] [Review][Patch] Tab Switch Scroll Reset in Chat Room [src/components/player/GameRoom.vue:432]
- [x] [Review][Patch] Accessibility Attribute for Confetti Decorations [src/components/player/GameRoom.vue:227]

## Dev Notes

### Frontend Architecture
- **Responsive Cascade Rules:** Vuetify Material Design styles are organized inside cascade layers alongside Tailwind CSS v4 resets and utility classes, ensuring responsive helpers like `d-none d-md-grid` operate without conflicts. [Source: architecture.md#Starter-Template-Evaluation]
- **Component Isolation:** The newly created `BingoCard.vue` and `OnboardingTour.vue` are modularly placed inside `src/components/player/`, keeping `GameRoom.vue` clean and focused. [Source: architecture.md#Frontend-Layout]
- **Animation Framework:** High-fidelity UI animations are compiled using native CSS keyframes:
  - `pop-in` for marked ball pops.
  - `spin-pulse` for the center grátis star.
  - `shine` and `pulse-button` for the golden BINGO button.
  - `fall` and `wobble` for victory confetti and trophies.
- **Safety Fallback:** Subscriptions and matrix displays safely handle missing matrices through reactive null-safe chains (`gameStore.ticketMatrix?.cards`).

### Existing Files Modified
- `bingo-frontend/src/components/player/GameRoom.vue` [MODIFIED — fully refactored placeholder to dual-viewport and chat setup]
- `_bmad-output/implementation-artifacts/sprint-status.yaml` [MODIFIED — marked story 2.2 and epic-2 as done]

### New Files Created
- `bingo-frontend/src/components/player/BingoCard.vue` [NEW]
- `bingo-frontend/src/components/player/OnboardingTour.vue` [NEW]
- `_bmad-output/implementation-artifacts/2-2-dual-viewport-responsive-player-layout-and-onboarding-guide.md` [NEW]

### References
- [Source: _bmad-output/planning-artifacts/epics.md — Story 2.2]
- [Source: _bmad-output/planning-artifacts/architecture.md — Frontend Layout, Integration Points]
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md — Responsive Game Split, Spotlight Tutorial, Interactive Card]
- [Source: bingo-frontend/src/components/player/GameRoom.vue]
- [Source: C:/Users/jeyso/.gemini/antigravity-ide/brain/434554ae-ecfb-4dd2-8235-eb313afdfb56/task.md]
- [Source: C:/Users/jeyso/.gemini/antigravity-ide/brain/434554ae-ecfb-4dd2-8235-eb313afdfb56/walkthrough.md]

## Dev Agent Record

### Agent Model Used

Gemini 3.5 Flash (High) / Antigravity

### Debug Log References

- **Type-Safety & Responsive Compiles:** Verified that TypeScript compilations through `vue-tsc --build --force` passed successfully without single warnings or import exceptions.
- **Static Assets Asset Bundling:** production build using `npm run build-only` bundled all JS/CSS resources under 5 seconds, validating correct relative asset links.

### Completion Notes List

- ✅ Component `BingoCard.vue` successfully created, encapsulating BINGO grid columns and pre-marked grátis central star.
- ✅ Component `OnboardingTour.vue` successfully created, managing a zero-dependency spotlight overlay guiding users step-by-step.
- ✅ Component `GameRoom.vue` successfully refactored to draw 2x2 desktop grid layouts and mobile carousel sliders synchronously.
- ✅ Simulated live chat dynamic additions written directly to Pinia-derived actions, preventing memory leaks on component unmounts.
- ✅ Full-screen celebration claims fully designed with keyframe falling confettis and card visualizers.
- ✅ Status updated globally to Done for Story 2.2 and Epic 2.
