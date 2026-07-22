package com.kychnoo.jnotes.ui.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CircularLoader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeCap = StrokeCap.Round,
            strokeWidth = 5.dp
        )
    }
}

@Preview
@Composable
private fun CircularLoaderPreview() {
    Box(
        modifier = Modifier.width(200.dp).height(200.dp)
    ) {
        CircularLoader(modifier = Modifier.fillMaxSize())
    }
}