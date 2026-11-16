package com.luycaslima.harpia.data

import android.graphics.Bitmap

data class CapturedFrame(val filename: String,
                         val bitmap : Bitmap,
                         val index : Int,
                         val positionX: Float,
                         val positionY: Float,
                         val positionZ: Float,
                         val rotationQx: Float,
                         val rotationQy: Float,
                         val rotationQz: Float,
                         val rotationQw: Float,
                         val focalLengthX: Float,
                         val focalLengthY: Float
)
