package com.luycaslima.harpia.utils

import android.graphics.ImageFormat
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