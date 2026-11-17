package com.luycaslima.harpia.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import java.io.ByteArrayOutputStream


data class ProcessedFrameBitmaps (
    val arTexture : Bitmap,
    val thumbnail : Bitmap
    )


fun Image.toJpegBytes( quality : Int = 95) : ByteArray {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val vSize = vBuffer.remaining()
    val width = this.width
    val height = this.height

    val nv21 = ByteArray(ySize + width * height / 2)
    yBuffer.get(nv21, 0, ySize) // Copy Y plane

    val vRowStride = planes[2].rowStride
    val vPixelStride = planes[2].pixelStride
    val uPixelStride = planes[1].pixelStride

    // Handle chroma stride to prevent gray/purple color corruption
    if (vPixelStride == 2 && vRowStride == width) {
        // Fast path: U and V are already interleaved (most devices)
        vBuffer.get(nv21, ySize, vSize)
    } else {
        // Safe fallback: manually interleave U and V
        var pos = ySize
        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vIndex = row * vRowStride + col * vPixelStride
                val uIndex = row * planes[1].rowStride + col * uPixelStride
                nv21[pos++] = vBuffer.get(vIndex)
                nv21[pos++] = uBuffer.get(uIndex)
            }
        }
    }

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    return ByteArrayOutputStream().use { out ->
        yuvImage.compressToJpeg(Rect(0,0,width,height),quality,out)
        out.toByteArray()
    }

}

/**
 * Decodes subsampled bitmaps directly from JPEG bytes without allocating full-res bitmaps in RAM.
 * [arSampleSize] and [thumbSampleSize] should be powers of 2 (e.g., 2, 4, 8, 16).
 */
fun ByteArray.toFrameBitmaps(arSampleSize : Int = 4,
                             thumbSampleSize: Int = 8,
                             uiRotationDegrees: Float = 90f
) : ProcessedFrameBitmaps {

    val arOptions = BitmapFactory.Options().apply {
        inSampleSize = arSampleSize
    }

    val arTexture = BitmapFactory.decodeByteArray(this,0,this.size,arOptions)

    val thumbOptions = BitmapFactory.Options().apply {
        inSampleSize = thumbSampleSize
    }
    val rawThumb = BitmapFactory.decodeByteArray(this,0,this.size,thumbOptions)

    //Rotate image
    val matrix = Matrix().apply { postRotate(uiRotationDegrees) }
    val upRightThumb = Bitmap.createBitmap(
        rawThumb,0,0,rawThumb.width,rawThumb.height,matrix,true
    )

    if(upRightThumb != rawThumb){
        rawThumb.recycle()
    }

    return ProcessedFrameBitmaps(
        arTexture = arTexture,
        thumbnail = upRightThumb
    )
}
