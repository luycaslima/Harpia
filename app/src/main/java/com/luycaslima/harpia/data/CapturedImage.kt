package com.luycaslima.harpia.data

import android.media.Image
import com.google.ar.core.Pose

data class CapturedImage (val image : Image, val cameraExtrinsic : Pose) {}