package com.luycaslima.harpia.ui.presentation.feature.capture

import android.media.Image
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.ar.core.CameraIntrinsics
import com.google.ar.core.Pose
import com.luycaslima.harpia.data.local.CameraExtrinsicData
import com.luycaslima.harpia.data.local.CameraIntrinsicsData
import com.luycaslima.harpia.data.local.CapturedFrame
import com.luycaslima.harpia.data.local.ReconstructionProject
import com.luycaslima.harpia.data.repository.ProjectStorageRepository
import com.luycaslima.harpia.utils.toFrameBitmaps
import com.luycaslima.harpia.utils.toJpegBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ARCaptureUiState (
    val currentProject : ReconstructionProject? = null,
    val isProcessingPhoto : Boolean = false,
    val captureCount : Int = 0,
    val statusMessage : String = "",
    val lastCaptureInfo : CapturedFrame? = null,
    val capturedFrames : List<CapturedFrame> = emptyList()
)

//Business Logic goes Here
class ARCaptureViewModel(private val repository : ProjectStorageRepository) : ViewModel( ) {
    private val _uiState = MutableStateFlow<ARCaptureUiState>(ARCaptureUiState())
    val uiState : StateFlow<ARCaptureUiState> = _uiState.asStateFlow()


    private val _captureNextFrame = MutableStateFlow(false)
    val captureNextFrame: StateFlow<Boolean> = _captureNextFrame.asStateFlow()

    fun requestCapture() {
        if (_uiState.value.isProcessingPhoto || _captureNextFrame.value) return
        //it -> shorthand for currentState -> currentState.copy
        _uiState.update { it.copy(isProcessingPhoto = true , statusMessage = "Capturing...") }
        _captureNextFrame.value = true
    }

    // Atomically consumes the trigger; returns true for exactly one caller
    fun tryConsumeCaptureRequest(): Boolean {
        return _captureNextFrame.compareAndSet(expect = true, update = false)
    }

    fun onFrameCaptured(rawImage : Image, cameraPose : Pose, cameraIntrinsics: CameraIntrinsics ){
        //Extract synchronously
        val intrinsicsData = CameraIntrinsicsData(
            fx = cameraIntrinsics.focalLength[0],
            fy = cameraIntrinsics.focalLength[1],
            cx = cameraIntrinsics.principalPoint[0],
            cy = cameraIntrinsics.principalPoint[1],
            width = cameraIntrinsics.imageDimensions[0],
            height = cameraIntrinsics.imageDimensions[1]
        )

        val cameraExtrinsicData = CameraExtrinsicData (
            positionX = cameraPose.tx(),
            positionY = cameraPose.ty(),
            positionZ = cameraPose.tz(),
            rotationQx = cameraPose.qx(),
            rotationQy = cameraPose.qy(),
            rotationQz = cameraPose.qz(),
            rotationQw = cameraPose.qw(),
        )

        viewModelScope.launch (Dispatchers.IO){
            try {
                val jpegBytes = rawImage.toJpegBytes(quality = 95)

                val baseProject = _uiState.value.currentProject ?: repository.createProject( intrinsics = intrinsicsData)

                val updatedProject = repository.addPhotoProject(
                    project = baseProject,
                    jpegBytes = jpegBytes,
                    pose = cameraExtrinsicData
                )

                val bitmaps = jpegBytes.toFrameBitmaps(
                    arSampleSize = 4,
                    thumbSampleSize = 8,
                    uiRotationDegrees = 90f
                )

                // 4. Pure, non-suspending atomic UI state update
                _uiState.update { currentState ->
                    val captured = CapturedFrame(
                        filename = updatedProject.photos.last().fileName,
                        arBitmap = bitmaps.arTexture,
                        thumbnailBitmap = bitmaps.thumbnail,
                        index = currentState.captureCount,
                        positionX = cameraExtrinsicData.positionX,
                        positionY = cameraExtrinsicData.positionY,
                        positionZ = cameraExtrinsicData.positionZ,
                        rotationQx = cameraExtrinsicData.rotationQx,
                        rotationQy = cameraExtrinsicData.rotationQy,
                        rotationQz = cameraExtrinsicData.rotationQz,
                        rotationQw = cameraExtrinsicData.rotationQw,
                        focalLengthX = intrinsicsData.fx,
                        focalLengthY = intrinsicsData.fy
                    )

                    currentState.copy(
                        currentProject = updatedProject,
                        isProcessingPhoto = false,
                        statusMessage = "Captured successfully!",
                        lastCaptureInfo = captured,
                        captureCount = currentState.captureCount + 1,
                        capturedFrames = currentState.capturedFrames + captured
                    )
                }
                //Allow only after processing
                _captureNextFrame.value = false

            }catch (e : Exception){
                rawImage.close()
                _captureNextFrame.value = false
                _uiState.update { it.copy(
                    isProcessingPhoto = false,
                    statusMessage = "Failed to capture ${e.localizedMessage}"
                ) }
            }finally {
                // Guarantees the ARCore buffer is released even if toJpegBytes() throws
                rawImage.close()
            }
        }
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                ARCaptureViewModel(ProjectStorageRepository(app.applicationContext))
            }
        }
    }
}