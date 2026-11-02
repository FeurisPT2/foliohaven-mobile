# FolioHaven Design Tokens & Identity

FolioHaven Mobile's design language is synchronized with the upstream design specification in [`DESIGN.md`](https://github.com/FeurisPT2/foliohaven/blob/main/DESIGN.md) and [`replica/brand.md`](https://github.com/FeurisPT2/foliohaven/blob/main/replica/brand.md).

It is implemented natively in Jetpack Compose in [`Color.kt`](file:///C:/Users/epicg/orca/foliohaven-mobile/app/src/main/kotlin/com/example/foliohaven/ui/theme/Color.kt), [`Theme.kt`](file:///C:/Users/epicg/orca/foliohaven-mobile/app/src/main/kotlin/com/example/foliohaven/ui/theme/Theme.kt), [`Shape.kt`](file:///C:/Users/epicg/orca/foliohaven-mobile/app/src/main/kotlin/com/example/foliohaven/ui/theme/Shape.kt), and [`Type.kt`](file:///C:/Users/epicg/orca/foliohaven-mobile/app/src/main/kotlin/com/example/foliohaven/ui/theme/Type.kt).

---

## 🎨 Color Tokens & Theming

### 1. Brand Accents
| Token | Hex | Role | Contrast Ratio |
| :--- | :--- | :--- | :--- |
| `PrimaryTerracotta` | `#C2410C` | Primary brand accent, CTA buttons, active state | 4.8:1 on dark canvas |
| `PrimaryHover` | `#EA580C` | Highlight accent, selected items, progress indicators | 5.6:1 on dark canvas |
| `PrimaryPressed` | `#9A3412` | Pressed buttons, container borders | Deep bronze |
| `PrimarySubtle` | `#1FC2410C` | 12% opacity tint for badges and selected tab pills | Ambient background |

### 2. Luxury Archival Dark Theme (Default Palette)
| Token | Hex | Role | Description |
| :--- | :--- | :--- | :--- |
| `CanvasDark` | `#141110` | App root canvas | Deepest charcoal-black (OLED power saving) |
| `BackgroundDark` | `#181513` | Main screen background | Subtle warm charcoal |
| `SurfaceEspresso` | `#1E1A18` | Top bar, Navigation rail, Bottom bar | Rich espresso surface |
| `CardSurface` | `#26211E` | Book cards, stat cards, dialogs | Elevated container |
| `BorderCharcoal` | `#38312C` | Card borders, dividers | Crisp archival border |
| `TextParchment` | `#FAF7F2` | Headings, primary text | Warm linen white (WCAG AAA) |
| `TextLinen` | `#B5ACA3` | Secondary text, authors | Muted linen grey |
| `TextMuted` | `#7E756D` | Labels, page numbers, timestamps | Bronze-grey |

### 3. Material Design 3 Surface Container Roles
To align with the Material 3 specification, surface elevations map to container tokens:
- **`surfaceContainerLowest`:** `#110E0D` (Deepest inset surfaces)
- **`surfaceContainerLow`:** `#151211` (Background behind cards)
- **`surfaceContainer`:** `#1A1614` (Default card surfaces)
- **`surfaceContainerHigh`:** `#201B19` (Elevated cards & 28dp pill search bar)
- **`surfaceContainerHighest`:** `#28221F` (Hovered/pressed cards and active items)
- **`surfaceTint`:** `PrimaryTerracotta` (`#C2410C`)
- **`inverseSurface`:** `#FAF7F2`
- **`inverseOnSurface`:** `#141110`

### 4. Dynamic Color (Material You Monet)
On **Android 12+ (API 31+)**, FolioHaven supports **Material You Dynamic Theming**:
- Enabled or disabled at runtime via `useDynamicColor` in Settings.
- When enabled, colors are dynamically generated from the user's system wallpaper via `dynamicDarkColorScheme(context)` and `dynamicLightColorScheme(context)`.
- When disabled or on Android 11 and below, the app strictly uses FolioHaven's signature Terracotta and Archival Charcoal palettes.

### 5. Four Archival Reading Canvas Modes
Readers can toggle between 4 dedicated reading palettes in the reading canvas:
| Mode | Background | Text Color | Ideal Lighting Condition |
| :--- | :--- | :--- | :--- |
| **Day** | `#FAF8F5` | `#1F1D1B` | Bright daylight, outdoor reading |
| **Sepia** | `#F5EBD7` | `#3E2D1E` | Soft indoor lighting, vintage book feel |
| **Night (OLED)** | `#111111` | `#D4D4D8` | Bedtime, pure OLED power saving |
| **Dark Sepia** | `#221A14` | `#D7C7B7` | Low ambient lighting, reduced eye strain |

### 6. Format Badges
| Format | Color Token | Hex | Role |
| :--- | :--- | :--- | :--- |
| **EPUB** | `BadgeEpub` | `#0284C7` | Clean reflow EPUB books |
| **PDF** | `BadgePdf` | `#E11D48` | Fixed layout PDF documents |
| **MOBI** | `BadgeMobi` | `#059669` | PalmDOC LZ77 Kindle books |
| **TXT** | `BadgeTxt` | `#D97706` | Plain text literature |

### 7. Annotation Highlighters
| Highlighter | Hex | Semantics |
| :--- | :--- | :--- |
| **Amber** | `#FFE270` | General highlights, important passages |
| **Emerald** | `#A8F29A` | Definitions, actionable insights |
| **Azure** | `#7BE5F8` | Quotes, citations, references |
| **Rose** | `#FFA0C5` | Questions, critical counterarguments |

---

## 🔲 Material 3 Shape System

Defined in [`Shape.kt`](file:///C:/Users/epicg/orca/foliohaven-mobile/app/src/main/kotlin/com/example/foliohaven/ui/theme/Shape.kt) using `FolioHavenShapes`:

| Token | Corner Radius | Component Usage |
| :--- | :--- | :--- |
| **ExtraSmall** | `4dp` | Badges, small tags, tooltips |
| **Small** | `8dp` | Buttons, filter chips, input fields |
| **Medium** | `12dp` | Cards, hero containers, TOC items |
| **Large** | `16dp` | Dialogs, bottom sheets, modal cards |
| **ExtraLarge** | `28dp` | Pill search bar, onboarding CTA, floating action button |

---

## 🔤 Typography Hierarchy

FolioHaven employs an editorial **Dual Typography** system:
- **Serif (`FontFamily.Serif`):** Used for book titles, 3D embossed book covers, and the main reader text body. Evokes classical literature, newsprint, and vintage book craft.
- **Sans-serif (`FontFamily.SansSerif`):** Used for UI controls, navigation labels, stats, category chips, and metadata for maximum legibility on mobile screens.

```
Headline Large   • Serif Bold       • 30sp / 36sp (-0.5sp letter spacing)
Headline Medium  • Serif Bold       • 24sp / 30sp (-0.25sp)
Title Large      • Serif Bold       • 20sp / 26sp (Section headers)
Body Large       • Sans-Serif Reg   • 15sp / 22sp (Readable article copy)
Body Medium      • Sans-Serif Reg   • 13sp / 18sp (Card content)
Label Large      • Sans-Serif Semi  • 13sp / 18sp (Buttons & Action chips)
Label Small      • Sans-Serif Bold  • 10sp / 14sp (Badges & Progress tags)
```

---

## 📐 Adaptive Display Measures & Ergonomics

To deliver a first-class reading experience across form factors:

| Screen Class | Viewport Width | Navigation Structure | Reading Canvas Layout | Max Content Measure |
| :--- | :--- | :--- | :--- | :--- |
| **Compact** | `< 600dp` (Phones) | Bottom Navigation Bar (`FolioHavenBottomBar`) | Single-column centered scroll | `widthIn(max = 760.dp)` |
| **Medium** | `600dp - 839dp` (Foldables/Small Tablets) | Left Navigation Rail (`NavigationRail`) | Two-Column Book Spread with vertical gutter divider | `widthIn(max = 1120.dp)` |
| **Expanded** | `≥ 840dp` (Large Tablets/Desktop) | Left Navigation Rail (`NavigationRail`) | Two-Column Book Spread with vertical gutter divider | `widthIn(max = 1120.dp)` |

- **Adaptive Dialogs:** All modal dialogs are constrained to `widthIn(max = 540.dp)` to prevent awkward, stretched layouts on wide screens.
- **Library Grid:** Uses `GridCells.Adaptive(minSize = 160.dp)` ensuring book cards always maintain an optimal aspect ratio.
- **Notes Grid:** Uses `GridCells.Adaptive(minSize = 280.dp)` for clean card wrapping.

---

## 📖 3D Archival Book Cover Architecture

Book items on the shelf are rendered via [`ArchivalBookCover()`](file:///C:/Users/epicg/orca/foliohaven-mobile/app/src/main/kotlin/com/example/foliohaven/HomeScreen.kt#L280-L414) in Compose. It layers 6 physical book characteristics:

```
┌──────────────────────────────────────────────┐
│ [Ribbon]                   [Format Tag: EPUB]│  <- Layer 5 & 4
│ ▌                                            │
│ ▌ ┌────────────────────────────────────────┐ │
│ ▌ │        SHERLOCK HOLMES                 │ │  <- Layer 3: Gold Foil Deboss Frame
│ ▌ │     Arthur Conan Doyle                 │ │
│ ▌ └────────────────────────────────────────┘ │
│ ▌                                            │
│ ▌                                            │
│[============================================]│  <- Layer 6: Reading Progress Bar
└──────────────────────────────────────────────┘
  ▲
  └─ Layer 2: 3D Spine Crease Gradient
     Layer 1: Diagonal Light Sheen Overlay
```

1. **Leather Gradient Base:** Deep archival duo-tone background (`#1E293B`, `#451A20`, `#143022`, `#6E280A`).
2. **Diagonal Lighting Sheen:** Linear gradient (`0x22FFFFFF` to `0x00FFFFFF`) simulating angled overhead light.
3. **3D Spine Crease:** Multi-stop horizontal shadow (`#99000000` to transparent) mimicking the rounded hinge of a hardback book.
4. **Gold Foil Deboss Frame:** 1dp border with `GoldFoilDeboss` (`#D4AF37`) at 45% alpha.
5. **Bookmark Ribbon:** Terracotta satin ribbon tucked at the top edge.
6. **Progress Edge:** Slim bottom progress bar indicating reading completion.
