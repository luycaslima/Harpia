package com.luycaslima.harpia.ui.presentation.feature.capture

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.ar.core.Config
import com.luycaslima.harpia.ui.presentation.common.TopCenterBar
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.capture.rememberSceneRecorder
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.shouldShowRationale
import com.luycaslima.harpia.ui.presentation.feature.capture.components.CameraButton
import com.luycaslima.harpia.ui.presentation.feature.capture.components.FrameCarousel
import com.luycaslima.harpia.ui.presentation.feature.capture.components.debug.DebugInfoCard
import com.luycaslima.harpia.utils.getEulerRotation
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CaptureScreen(
    navController: NavController,
    viewModel: CaptureViewModel = viewModel(factory = CaptureViewModel.Factory)
) {
    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val shouldCaptureNextFrame by viewModel.captureNextFrame.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    //Suspend view until receive permission or not
    LaunchedEffect(Unit) {
        if(!cameraPermissionState.status.isGranted){
            cameraPermissionState.launchPermissionRequest()
        }
    }

    val sceneRecorder = rememberSceneRecorder()
    //val engine = rememberEngine()
    //val modelLoader = rememberModelLoader(engine)
    //val modelInstance = rememberModelInstance(modelLoader, "models/basic_item_stand.glb")

    if (cameraPermissionState.status.isGranted) {
        Scaffold(/*modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)*/
            topBar = {
                TopCenterBar("Capture", onClick = {
                    navController.popBackStack()
                })
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                CameraButton(
                    isProcessing = uiState.isProcessingPhoto,
                    onClick = {viewModel.requestCapture()}
                )
            },
            content = { paddingValues ->
                //When true it will capture the system back button command and inject what i pass
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)) {
                    BackHandler(enabled = true) {
                        // 1. Your custom logic when back is pressed
                        navController.popBackStack()
                    }
                    ARSceneView (
                        // Pass the surfaceMirrorer so the recorder can see the pipeline
                        surfaceMirrorer = sceneRecorder.surfaceMirrorer,
                        sessionConfiguration = { session, config ->
                            // Depth — makes virtual objects occlude behind real ones
                            config.depthMode =
                                if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC))
                                    Config.DepthMode.AUTOMATIC
                                else Config.DepthMode.DISABLED

                            // Light estimation — matches virtual lighting to the real room
                            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                            // Instant placement — model appears immediately, then locks to plane
                            //config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                        },
                        modifier = Modifier.fillMaxSize(),
                        planeRenderer = true,
                        onSessionUpdated = { _, frame ->
                            if(shouldCaptureNextFrame ){
                                try {
                                    val pose = frame.camera.pose
                                    val intrinsics = frame.camera.imageIntrinsics
                                    val rawImage = frame.acquireCameraImage()

                                    if (viewModel.tryConsumeCaptureRequest()) {
                                        viewModel.onFrameCaptured(
                                            rawImage = rawImage,
                                            cameraPose = pose,
                                            cameraIntrinsics = intrinsics
                                        )
                                    } else {
                                        rawImage.close()
                                    }

                                }catch (e: Exception){
                                    //Raise a window calling error if hits here
                                    android.util.Log.w("CaptureScreen", "Frame not ready yet, retrying...", e)
                                }
                            }
                        }) {

                        uiState.capturedFrames.forEach { c ->
                                ImageNode(
                                    bitmap = c.arBitmap,
                                    scale = Scale(0.1f, 0.1f, 0.1f),
                                    position = Position(c.positionX, c.positionY, c.positionZ),
                                    rotation = getEulerRotation(c.rotationQx, c.rotationQy, c.rotationQz, c.rotationQw)
                                )
                            }

                        }
                    // Verification Overlay HUD
                    DebugInfoCard(statusMessage= uiState.statusMessage, captureCount= uiState.captureCount, lastCapturedFrame = uiState.lastCaptureInfo)
                    if (uiState.capturedFrames.isNotEmpty()) {
                        FrameCarousel(
                            frames = uiState.capturedFrames,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 88.dp) // Clears the center FAB
                        )
                    }
                }
            })
    } else {
        //Improve This is for logic reasons
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                val rationaleText = if (cameraPermissionState.status.shouldShowRationale) {
                    "This app needs the camera to map the 3D environment. Please grant the permission."
                } else {
                    "Camera permission is permanently denied. You must enable it in system settings to use this feature."
                }
                Text(rationaleText)
                Spacer(modifier = Modifier.height(16.dp))
                if (cameraPermissionState.status.shouldShowRationale) {
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                } else {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}