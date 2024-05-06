package com.example.galleryapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.ui.graphics.vector.ImageVector


sealed class Screen(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector
) {
    object Photos : Screen(
        "Photos",
        "Photos",
        selectedIcon = Icons.Filled.Photo,
        unSelectedIcon = Icons.Outlined.Photo
    )

    object Albums : Screen(
        "Albums",
        "Albums",
        selectedIcon = Icons.Filled.Folder,
        unSelectedIcon = Icons.Outlined.Folder
    )

    object Explore : Screen(
        "Explore",
        "Explore",
        selectedIcon = Icons.Filled.Explore,
        unSelectedIcon = Icons.Outlined.Explore
    )

    object PicDetail : Screen(
        "PicDetail",
        "PicDetail",
        selectedIcon = Icons.Filled.Explore,
        unSelectedIcon = Icons.Outlined.Explore
    )
}