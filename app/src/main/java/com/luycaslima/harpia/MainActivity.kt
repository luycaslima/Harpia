package com.luycaslima.harpia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.luycaslima.harpia.ui.presentation.feature.navigation.Nav
import com.luycaslima.harpia.ui.theme.HarpiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HarpiaTheme {
                Nav()
            }
        }
    }
}
