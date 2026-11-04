package com.luycaslima.harpia.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import java.io.ByteArrayOutputStream


/**
 * Converts a YUV_420_888 camera frame to a JPEG ByteArray.
 * TODO improve into a FrameProcessor class ( convert to jpeg and also decode to bitmap the result separatly )
 */
fun Image.toJpegByteArray(quality: Int = 90): ByteArray {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // Copy Y channel
    yBuffer.get(nv21, 0, ySize)

    // Copy UV channels (interleaved for NV21 format)
    val vByte = ByteArray(1)
    val uByte = ByteArray(1)
    var offset = ySize

    while (vBuffer.hasRemaining() && uBuffer.hasRemaining()) {
        vBuffer.get(vByte)
        uBuffer.get(uByte)
        nv21[offset++] = vByte[0]
        nv21[offset++] = uByte[0]
    }

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, this.width, this.height), quality, out)
    return out.toByteArray()
}


fun Image.toCorrectedBitmap(): Bitmap {
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

    // Convert valid NV21 to JPEG, then to Bitmap
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()
    val rawBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    // Fix the 90-degree native sensor rotation
    //val matrix = Matrix().apply { postRotate(90f) }
    //return Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
    return rawBitmap
}