package com.luycaslima.harpia.ui.presentation.feature.capture

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.luycaslima.harpia.ui.presentation.common.TopCenterBar
import io.github.sceneview.SceneView
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CaptureScreen(navController: NavController) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var anchor by remember { mutableStateOf<Anchor?>(null) }

    //TODO if i need more camera control i have to ditch sceneview and us pure ARCore + Camera2
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
                ARSceneView (
                    modifier = Modifier.fillMaxSize()
                    , planeRenderer = true,
                    onSessionUpdated = { _, frame ->
                        // Create an anchor on the first detected horizontal plane
                        if (anchor == null) {
                            anchor = frame.getUpdatedPlanes()
                                .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                                ?.let { it.createAnchorOrNull(it.centerPose) }
                        }
                    }) {

                }
            })
    } else {
        navController.popBackStack()
    }
}