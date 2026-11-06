package com.luycaslima.harpia.ui.presentation.feature.capture

import android.media.Image
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.core.CameraIntrinsics
import com.google.ar.core.Pose
import com.luycaslima.harpia.data.CapturedFrame
import com.luycaslima.harpia.utils.toCorrectedBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CaptureUiState (
    val isProcessingPhoto : Boolean = false,
    val captureCount : Int = 0,
    val statusMessage : String = "",
    val lastCaptureInfo : CapturedFrame? = null,
    val capturedFrames : List<CapturedFrame> = emptyList()
)

//Business Logic goes Here
class CaptureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CaptureUiState>(CaptureUiState())
    val uiState : StateFlow<CaptureUiState> = _uiState.asStateFlow()


    private val _captureNextFrame = MutableStateFlow(false)
    val captureNextFrame: StateFlow<Boolean> = _captureNextFrame.asStateFlow()

    fun requestCapture() {
        //it -> shorthand for currentState -> currentState.copy
        _uiState.update { it.copy(isProcessingPhoto = true , statusMessage = "Capturing...") }
        _captureNextFrame.value = true
    }

    fun onFrameCaptured(rawImage : Image, cameraPose : Pose, cameraIntrinsics: CameraIntrinsics ){

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.GERMAN).format(Date())
        val fileName = "IMG_$timeStamp.jpg"

        viewModelScope.launch (Dispatchers.IO){

            try {
                val bitmap = rawImage.toCorrectedBitmap()
                rawImage.close()

                val focalLength = cameraIntrinsics.focalLength

                val captured = CapturedFrame(
                    filename = fileName,
                    bitmap = bitmap,
                    positionX = cameraPose.tx(),
                    positionY = cameraPose.ty(),
                    positionZ = cameraPose.tz(),
                    rotationQx = cameraPose.qx(),
                    rotationQy = cameraPose.qy(),
                    rotationQz = cameraPose.qz(),
                    rotationQw = cameraPose.qw(),
                    focalLengthX = focalLength[0],
                    focalLengthY = focalLength[1]
                )

                _uiState.update { currentState -> currentState.copy(
                    isProcessingPhoto = false,
                    statusMessage = "Captured successfully!",
                    lastCaptureInfo = captured,
                    captureCount = currentState.captureCount + 1,
                    capturedFrames = currentState.capturedFrames + captured //wtf
                ) }
                //Allow only after processing
                _captureNextFrame.value = false

            }catch (e : Exception){
                rawImage.close()
                _captureNextFrame.value = false
                _uiState.update { it.copy(
                    isProcessingPhoto = false,
                    statusMessage = "Failed to capture ${e.localizedMessage}"
                ) }
            }
        }
    }
}