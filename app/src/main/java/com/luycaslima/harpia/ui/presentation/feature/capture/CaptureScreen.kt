package com.luycaslima.harpia.ui.presentation.feature.capture

import android.Manifest
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import java.io.File
import com.luycaslima.harpia.data.CapturedFrameInfo
import com.luycaslima.harpia.ui.presentation.common.TopCenterBar
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.capture.rememberSceneRecorder
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.core.Pose
import com.luycaslima.harpia.utils.toJpegByteArray
import io.github.sceneview.node.ImageNode
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
    var captureCount by remember { mutableIntStateOf(0) }
    var lastCapturedInfo by remember {  mutableStateOf<CapturedFrameInfo?>(null) }
    var statusMessage by remember {  mutableStateOf("Ready To Capture") }

    val sceneRecorder = rememberSceneRecorder()

    //val engine = rememberEngine()
    //val modelLoader = rememberModelLoader(engine)
    //val modelInstance = rememberModelInstance(modelLoader, "models/basic_item_stand.glb")
    //TODO use ImageNode to render the images on the AR space
    //TODO keep all the information on a Data class and put on the state here
    var imagePlanes by remember { mutableStateOf<Bitmap?>(null) }

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
                FloatingActionButton(
                    modifier = Modifier.size(64.dp),
                    onClick = {
                        statusMessage = "Capturing .."
                        shouldCaptureNextFrame = true
                    },
                    shape = CircleShape, // Makes the button perfectly round
                    containerColor = MaterialTheme.colorScheme.primary, // Optional background color
                    contentColor = MaterialTheme.colorScheme.onPrimary

                ) {

                }
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
                                        val jpegBytes = rawImage.toJpegByteArray()
                                        rawImage.close()

                                        // Save file locally
                                        val outputDir = File(ctx.filesDir, "captures").apply { mkdirs() }
                                        val imageFile = File(outputDir, fileName)
                                        imageFile.writeBytes(jpegBytes)

                                        // ARCore camera intrinsics are always returned unrotated relative to the native camera sensor orientation.
                                        // Changing orientation, need to also adjust this
                                        val focalLength = intrinsics.focalLength
                                        val dimensions = intrinsics.imageDimensions
                                        val principalPoint = intrinsics.principalPoint
                                        // Update verification UI
                                        launch(Dispatchers.Main) {
                                            captureCount++
                                            lastCapturedInfo = CapturedFrameInfo(
                                                filename = fileName,
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
                                            statusMessage = "Captured successfully!"
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
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                                .fillMaxWidth(0.85f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Total Captures: $captureCount", color = Color.Green, fontSize = 16.sp)
                                Text("Status: $statusMessage", color = Color.White, fontSize = 14.sp)

                                lastCapturedInfo?.let { info ->
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("File: ${info.filename}", color = Color.Yellow, fontSize = 12.sp)
                                    Text(
                                        "Pos (XYZ): %.3f, %.3f, %.3f".format(info.positionX, info.positionY, info.positionZ),
                                        color = Color.Cyan,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        "Rot (Q): %.2f, %.2f, %.2f, %.2f".format(info.rotationQx, info.rotationQy, info.rotationQz, info.rotationQw),
                                        color = Color.Cyan,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        "Focal (fx, fy): %.1f, %.1f".format(info.focalLengthX, info.focalLengthY),
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            /*anchor?.let { a ->
                                AnchorNode(anchor = a) {
                                    modelInstance?.let { instance ->
                                        ModelNode(
                                            modelInstance = instance,
                                            scaleToUnits = 0.3f ,  // 30cm cube,
                                            centerOrigin = io.github.sceneview.math.Position(
                                                x = 0f,
                                                y = 0f,
                                                z = 0f
                                            )
                                        )
                                    }
                                }
                            }*/
                        }
                    }
                }

            })
    } else {
        navController.popBackStack()
    }
}