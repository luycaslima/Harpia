package com.luycaslima.harpia.ui.presentation.feature.capture

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.luycaslima.harpia.ui.presentation.common.TopCenterBar
import io.github.sceneview.SceneView


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CaptureScreen(navController: NavController) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }
    //TODO treat better this permission
    if (cameraPermissionState.status.isGranted) {
        Scaffold(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars), topBar = {
            TopCenterBar("Capture", onClick = {
                navController.popBackStack()
            })
        },
            content = { paddingValues ->
                //When true it will capture the system back button command and inject what i pass
                BackHandler(enabled = true) {
                    // 1. Your custom logic when back is pressed
                    navController.popBackStack()
                }
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    SceneView() {

                    }
                }
            })
    } else {
        navController.popBackStack()
    }
}