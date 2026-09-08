---
name: Crumb & Crust
colors:
  surface: '#fff8f5'
  surface-dim: '#e9d7c8'
  surface-bright: '#fff8f5'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#fff1e8'
  surface-container: '#feeadc'
  surface-container-high: '#f8e5d6'
  surface-container-highest: '#f2dfd1'
  on-surface: '#231a11'
  on-surface-variant: '#544339'
  inverse-surface: '#392e25'
  inverse-on-surface: '#ffeee1'
  outline: '#877367'
  outline-variant: '#dac2b4'
  surface-tint: '#934b0d'
  primary: '#90490b'
  on-primary: '#ffffff'
  primary-container: '#ae6024'
  on-primary-container: '#fffbff'
  inverse-primary: '#ffb786'
  secondary: '#536345'
  on-secondary: '#ffffff'
  secondary-container: '#d6e9c2'
  on-secondary-container: '#59694a'
  tertiary: '#815215'
  on-tertiary: '#ffffff'
  tertiary-container: '#9d6a2c'
  on-tertiary-container: '#fffbff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdcc6'
  primary-fixed-dim: '#ffb786'
  on-primary-fixed: '#311300'
  on-primary-fixed-variant: '#723600'
  secondary-fixed: '#d6e9c2'
  secondary-fixed-dim: '#bacda7'
  on-secondary-fixed: '#111f07'
  on-secondary-fixed-variant: '#3c4b2f'
  tertiary-fixed: '#ffdcbb'
  tertiary-fixed-dim: '#faba75'
  on-tertiary-fixed: '#2b1700'
  on-tertiary-fixed-variant: '#673d00'
  background: '#fff8f5'
  on-background: '#231a11'
  surface-variant: '#f2dfd1'
typography:
  display-lg:
    fontFamily: Newsreader
    fontSize: 3.5rem
    fontWeight: '400'
    lineHeight: 4rem
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Newsreader
    fontSize: 2.25rem
    fontWeight: '400'
    lineHeight: 2.75rem
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Newsreader
    fontSize: 2.25rem
    fontWeight: '500'
    lineHeight: 2.75rem
    letterSpacing: -0.015em
  headline-lg-mobile:
    fontFamily: Newsreader
    fontSize: 1.75rem
    fontWeight: '500'
    lineHeight: 2.25rem
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Newsreader
    fontSize: 1.625rem
    fontWeight: '500'
    lineHeight: 2.125rem
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 1.25rem
    fontWeight: '600'
    lineHeight: 1.75rem
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 1rem
    fontWeight: '600'
    lineHeight: 1.5rem
  body-lg:
    fontFamily: Newsreader
    fontSize: 1.125rem
    fontWeight: '400'
    lineHeight: 1.875rem
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 0.9375rem
    fontWeight: '400'
    lineHeight: 1.5rem
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 0.8125rem
    fontWeight: '400'
    lineHeight: 1.25rem
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 0.8125rem
    fontWeight: '600'
    lineHeight: 1.125rem
    letterSpacing: 0.04em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 0.6875rem
    fontWeight: '600'
    lineHeight: 1rem
    letterSpacing: 0.06em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  space-3xs: 0.125rem
  space-2xs: 0.25rem
  space-xs: 0.5rem
  space-sm: 0.75rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
  space-2xl: 3rem
  space-3xl: 4rem
  gutter-mobile: 1rem
  gutter-desktop: 2rem
  container-max: 76rem
---

## Brand & Style

This design system embodies the timeless discipline of artisan baking translated into a refined digital workshop. It balances the tactile warmth of stone hearths, toasted crusts, and slow fermentation with the rigorous mathematical precision demanded by baker's percentages, hydration metrics, and fermentation schedules.

The target audience includes dedicated home sourdough bakers, micro-bakers, and culinary professionals who revere patience, craft, and methodical observation. The interface evokes quiet focus, tactile organic authenticity, and culinary reverence—feeling less like administrative software and more like a leather-bound, flour-dusted baker's journal crossbred with an atelier precision tool.

The visual direction marries **Warm Editorial Minimalism** with **Tactile / Organic Structure**:
- **Purity of Surface:** Vast cream/linen canvases simulating unbleached flour paper.
- **Architectural Grounding:** Hairline wheat-toned division rules evoke scoring patterns on proofed loaves.
- **Subtle Organic Materiality:** Delicate tonalities without loud digital gloss, heavy dropshadows, or synthetic saturations.

## Colors

The palette is derived directly from the transformation of simple grain into caramelized crust:

- **Primary (`#B46528` - Toasted Amber / Crust):** The Maillard caramelization of a blistered sourdough boule. Used for prominent active triggers, crucial state toggles, and hero highlights.
- **Secondary (`#4A5A3C` - Wild Herb / Proofing Sage):** Evokes natural fermentation, vitality, and rest. Used for completed milestones, healthy active starter state indicators, and subtle organic balances.
- **Tertiary (`#DDA15E` - Golden Spelt / Raw Wheat):** Warm golden accent used for secondary badges, dough hydrations, and hover nuances.
- **Neutral Primary (`#2B2118` - Roasted Espresso):** Deep, organic charcoal-brown serving as high-contrast body text. It replaces harsh synthetic `#000000` with kiln-fired depth.
- **Surface Canvas (`#FAF6F0` - Linen Cream) & Sub-surfaces (`#F4ECE1` - Unbleached Flour):** Soft, restful backgrounds that prevent eye strain and create warmth across long calculation sessions.
- **Hairline Dividers (`#E8DED1` - Scored Rye):** Fine structural borders replacing default gray rules.

## Typography

The typographical pairing celebrates the communion between literary prose and technical clarity:

- **Headlines & Editorial Callouts (`Newsreader`):** Infused with editorial warmth, gentle serifs, and high optical legibility. Display levels use slight negative tracking to feel deliberate and tailored like an artisanal cookbook publication.
- **Interface, Inputs & Calculations (`Plus Jakarta Sans`):** Clean, geometric, yet possessing softly rounded terminals that prevent clinical sterile feeling. It renders tabular numerals with razor-sharp balance for gram-level formulas and fermentation countdown timers.
- **Numbers in Tables:** Always set with `font-variant-numeric: tabular-nums lining-nums;` to ensure formula columns, hydration weights, and Baker's Percentages maintain strict vertical alignments.

## Layout & Spacing

The layout is disciplined by a responsive **12-column fluid grid** nested inside a constrained maximum width (`76rem` / 1216px), preserving intimacy and avoiding hyper-wide sparse dashboards:

- **Desktop (>= 1024px):** 12 columns with 32px (`2rem`) gutters and minimum 32px outer safe margins. Formula workbenches split 7/5 (7 columns for the Baker’s percentage calculator and recipe notes, 5 columns for the interactive fermentation timeline and flour composition charts).
- **Tablet (768px - 1023px):** 8 columns with 24px (`1.5rem`) gutters. Side panels stack or collapse into sticky bottom trays.
- **Mobile (< 768px):** 4 columns with 16px (`1rem`) gutters and 16px lateral padding. Tables switch to condensed horizontal-scroll cards with locked leftmost ingredient headers.

Rhythmic baseline spacing uses an 8px base grid, dropping to 4px solely for dense tabular row padding and fine input caps. Whitespace is generous around editorial narrative headers, yet strictly compact and utilitarian within ingredient calculations.

## Elevation & Depth

Visual hierarchy does not rely on synthetic blurred drop shadows. Depth is communicated through **Warm Surface Layering and Subtle Tonal Contrast**:

- **Ground Level (`#FAF6F0`):** Base canvas layer simulating heavy deckled linen.
- **Card / Panel Level (`#FFFFFF` or `#F4ECE1`):** Raised surfaces sit directly on the ground delineated by a 1px solid hairline border (`#E8DED1`).
- **Tonal Depth Hierarchy:**
  - *Tier 0 (Canvas):* `#FAF6F0`
  - *Tier 1 (Cards, Work Surfaces):* `#FFFFFF` with border `1px solid #E8DED1`.
  - *Tier 2 (Sticky Headers, Modal Overlays):* `#FFFFFF` supplemented by an ultra-diffused, ambient warm shadow: `0 12px 32px -4px rgba(43, 33, 24, 0.06), 0 4px 12px -2px rgba(43, 33, 24, 0.03)`.
  - *Tier 3 (Active Timeline Pins & Drag Handles):* Casts a tiny tactile pill drop: `0 2px 6px rgba(43, 33, 24, 0.12)`.

## Shapes

The shape philosophy mirrors artisanal craftsmanship: soft-edged, sculpted, but never toy-like.

- **Base Radius (`0.25rem` / 4px):** Standard inputs, small badges, calculation cell selections, and timeline nodes.
- **Container Radius (`0.5rem` / 8px):** Recipe cards, formula panels, dough log entries, and dialogs.
- **Pill Radius (`9999px`):** Reserved exclusively for hydration tags, levain status chips, and primary floating action triggers.
- **Scored Edge Accent:** Hero panels may feature a single refined 2px amber accent line along the top or left border, evoking the razor score on an ear of bread.

## Components

### Buttons
- **Primary:** Solid `#B46528` background with `#FFFFFF` label. On hover, transitions to `#96511C`. 0.5rem radius, medium weight, 0.02em letter tracking.
- **Secondary / Outline:** Background transparent, 1px solid `#B46528`, text `#B46528`. Hover: `#FAF6F0` with solid `#2B2118` text.
- **Ghost:** `#2B2118` text with no border. On hover: subtle `#F4ECE1` background fill.

### Input Fields & Gram Selectors
- **Input Ground:** Crisp white background `#FFFFFF` enclosed by a 1px border `#E8DED1`.
- **Focus State:** 1.5px border `#B46528` with zero fluorescent browser outlines.
- **Numeric Stepper:** Integrated inside Baker’s percentage table rows; subtle muted buttons (`+` / `-`) that blend quietly into the background until hovered.

### Baker's Percentage Tables
- **Header:** Uppercase `label-sm` in `#7B6C61` with subtle bottom border.
- **Rows:** Alternating soft hover highlight (`#F9F5EF`). Right-aligned tabular numeric inputs for Weight (g) and Percentage (%).
- **Flour Weight Lock:** Distinct visual pin indicator showing the 100% flour baseline from which all ambient hydration percentages derive.

### Interactive Vertical Fermentation Timeline
- **Spine:** 2px vertical rule in `#E8DED1` connecting step milestones.
- **Nodes (Stretch & Fold, Bulk Ferment, Shape, Cold Retard):** Circular 28px markers. Complete stages filled with `#4A5A3C` displaying a subtle cream check. Active stage outlined in `#B46528` with a pulsing inner core.
- **Time Offsets:** Monospaced tabular time readouts (`+00:45`, `+02:30`) anchored to the left of the spine; instructions and ambient temperature logs anchored to the right.

### Cards & Dough Logs
- Rendered on `#FFFFFF` with 1px `#E8DED1` border and 8px border radius.
- Includes distinct regional metadata header: Ambient Room Temp, Dough Temp, Inoculation %, and Hydration % displayed in pill chips with `#F4ECE1` fills.