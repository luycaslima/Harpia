package com.luycaslima.harpia.ui.presentation.common

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CameraButton(isProcessing : Boolean, onClick : () -> Unit) {
    if (!isProcessing ){
        FloatingActionButton(
            modifier = Modifier.size(64.dp),
            onClick =  onClick,
            shape = CircleShape, // Makes the button perfectly round
            containerColor = MaterialTheme.colorScheme.primary, // Optional background color
            contentColor = MaterialTheme.colorScheme.onPrimary

        ) {}
    } else {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
