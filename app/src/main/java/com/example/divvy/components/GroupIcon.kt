package com.example.divvy.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps a string icon name to a Material Icon vector.
 * Extend this map whenever you need new group icons.
 */
fun resolveGroupIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Home" -> Icons.Rounded.Home
        "Flight" -> Icons.Rounded.Flight
        "Restaurant" -> Icons.Rounded.Restaurant
        "Work" -> Icons.Rounded.Work
        "ShoppingBag" -> Icons.Rounded.ShoppingBag
        "Grocery" -> Icons.Rounded.LocalGroceryStore
        "Car" -> Icons.Rounded.DirectionsCar
        "School" -> Icons.Rounded.School
        "Movie" -> Icons.Rounded.Movie
        "Music" -> Icons.Rounded.MusicNote
        "Pets" -> Icons.Rounded.Pets
        "Celebration" -> Icons.Rounded.Celebration
        "Gaming" -> Icons.Rounded.SportsEsports
        "Bank" -> Icons.Rounded.AccountBalance
        else -> Icons.Rounded.Group
    }
}

@Composable
fun GroupIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Icon(
        imageVector = resolveGroupIcon(iconName),
        contentDescription = iconName,
        modifier = modifier,
        tint = tint
    )
}
