package com.example.foliohaven.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 Shape Tokens for FolioHaven:
 * - Extra Small: 4dp (Badges, small tags)
 * - Small: 8dp (Chips, small buttons)
 * - Medium: 12dp (Input fields, search bars, cards)
 * - Large: 16dp (Hero cards, book covers, bottom sheets)
 * - Extra Large: 28dp (M3 Dialogs, full pill search bars, extended FABs)
 */
val FolioHavenShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
