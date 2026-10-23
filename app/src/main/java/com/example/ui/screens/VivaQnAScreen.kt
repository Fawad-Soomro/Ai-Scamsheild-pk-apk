package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VivaQnAScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    FaqScreen(
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}
