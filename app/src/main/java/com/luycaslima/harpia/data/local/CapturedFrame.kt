package com.luycaslima.harpia.data.local

import android.graphics.Bitmap

data class CapturedFrame(val filename: String,
                         val arBitmap : Bitmap,
                         val thumbnailBitmap : Bitmap,
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
