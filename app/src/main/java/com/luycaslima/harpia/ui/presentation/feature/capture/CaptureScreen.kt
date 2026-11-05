package com.luycaslima.harpia.ui.presentation.feature.capture

import android.Manifest
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.luycaslima.harpia.data.CapturedFrame
import com.luycaslima.harpia.ui.presentation.common.TopCenterBar
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.capture.rememberSceneRecorder
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.mutableIntStateOf
import com.google.ar.core.Pose
import com.luycaslima.harpia.ui.presentation.common.CameraButton
import com.luycaslima.harpia.ui.presentation.common.debug.DebugInfoCard
import com.luycaslima.harpia.utils.getEulerRotation
import com.luycaslima.harpia.utils.toCorrectedBitmap
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


//TODO decouple functions from this screen
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CaptureScreen(
    navController: NavController
) {
    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    //Like use state
    var shouldCaptureNextFrame by remember { mutableStateOf(false) }
    var processingPhoto by remember { mutableStateOf(false)}
    var captureCount by remember { mutableIntStateOf(0) }
    var lastCapturedInfo by remember {  mutableStateOf<CapturedFrame?>(null) }
    var statusMessage by remember {  mutableStateOf("Ready To Capture") }

    val sceneRecorder = rememberSceneRecorder()

    //val engine = rememberEngine()
    //val modelLoader = rememberModelLoader(engine)
    //val modelInstance = rememberModelInstance(modelLoader, "models/basic_item_stand.glb")
    //TODO use ImageNode to render the images on the AR space
    //TODO keep all the information on a Data class and put on the state here
    val captureFrames = remember { mutableListOf<CapturedFrame>() }

    var anchor by remember { mutableStateOf<Anchor?>(null) }

    //TODO if i need more camera control i have to ditch sceneview and us pure ARCore + Camera2
    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }
    //TODO treat better this permission
    if (cameraPermissionState.status.isGranted) {
        Scaffold(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            topBar = {
                TopCenterBar("Capture", onClick = {
                    navController.popBackStack()
                })
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                CameraButton(isProcessing = processingPhoto, onClick = {
                        statusMessage= "Capturing..."
                        shouldCaptureNextFrame = true
                    }
                )
            },
            content = { paddingValues ->
                //When true it will capture the system back button command and inject what i pass
                Box(modifier = Modifier.padding(paddingValues)) {
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
                        modifier = Modifier.fillMaxSize()
                        ,planeRenderer = true,
                        onSessionUpdated = { _, frame ->
                            if(shouldCaptureNextFrame){
                                processingPhoto = true
                                shouldCaptureNextFrame = false
                                val pose : Pose = frame.camera.pose
                                val intrinsics = frame.camera.imageIntrinsics

                                try{
                                    val rawImage = frame.acquireCameraImage()
                                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.GERMAN).format(
                                        Date()
                                    )
                                    val fileName = "IMG_$timeStamp.jpg"

                                    //process image in the background thread
                                    coroutineScope.launch(Dispatchers.IO) {
                                        //val jpegBytes = rawImage.toJpegByteArray()
                                        val bitmap = rawImage.toCorrectedBitmap()
                                        rawImage.close()

                                        // Save file locally
                                        //val outputDir = File(ctx.filesDir, "captures").apply { mkdirs() }
                                        //val imageFile = File(outputDir, fileName)
                                        //imageFile.writeBytes(jpegBytes)

                                        // ARCore camera intrinsics are always returned unrotated relative to the native camera sensor orientation.
                                        // Changing orientation, need to also adjust this
                                        val focalLength = intrinsics.focalLength
                                        val dimensions = intrinsics.imageDimensions
                                        val principalPoint = intrinsics.principalPoint
                                        // Update verification UI
                                        launch(Dispatchers.Main) {
                                            captureCount++
                                            val capture = CapturedFrame(
                                                filename = fileName,
                                                bitmap = bitmap,
                                                positionX = pose.tx(),
                                                positionY = pose.ty(),
                                                positionZ = pose.tz(),
                                                rotationQx = pose.qx(),
                                                rotationQy = pose.qy(),
                                                rotationQz = pose.qz(),
                                                rotationQw = pose.qw(),
                                                focalLengthX = focalLength[0],
                                                focalLengthY = focalLength[1]
                                            )
                                            lastCapturedInfo = capture
                                            captureFrames.add(capture)
                                            statusMessage = "Captured successfully!"
                                            processingPhoto = false
                                        }
                                    }

                                }catch (e: Exception){
                                    Log.e("CaptureScreen", "Error capturing frame", e)
                                    statusMessage = "Capture failed: ${e.localizedMessage}"
                                }
                            }

                            // Create an anchor on the first detected horizontal plane
                            if (anchor == null) {
                                anchor = frame.getUpdatedPlanes()
                                    .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                                    ?.let { it.createAnchorOrNull(it.centerPose) }
                            }
                        }) {

                        // Verification Overlay HUD
                        DebugInfoCard(statusMessage= statusMessage, captureCount= captureCount, lastCapturedFrame = lastCapturedInfo)
                        captureFrames.forEach { c ->
                                ImageNode(bitmap = c.bitmap,
                                    scale = Scale(0.1f, 0.1f, 0.1f),
                                    position = Position(
                                        c.positionX,
                                        c.positionY,
                                        c.positionZ
                                    ),
                                    rotation = getEulerRotation(c.rotationQx, c.rotationQy, c.rotationQz, c.rotationQw)
                                )
                            }

                        }
                    }
            })
    } else {
        navController.popBackStack()
    }
}