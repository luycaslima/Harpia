package com.luycaslima.harpia.utils


import io.github.sceneview.math.Rotation
import kotlin.math.*
import kotlin.math.withSign

fun getEulerRotation(qx: Float, qy: Float, qz: Float, qw: Float): Rotation {
    // Rotation around X axis
    val sinrCosp = 2.0 * (qw * qx + qy * qz)
    val cosrCosp = 1.0 - 2.0 * (qx * qx + qy * qy)
    val x = atan2(sinrCosp, cosrCosp)

    // Rotation around Y axis
    val sinp = 2.0 * (qw * qy - qz * qx)
    val y = if (abs(sinp) >= 1.0) (Math.PI / 2.0).withSign(sinp) else asin(sinp)

    // Rotation around Z axis
    val sinyCosp = 2.0 * (qw * qz + qx * qy)
    val cosyCosp = 1.0 - 2.0 * (qy * qy + qz * qz)
    val z = atan2(sinyCosp, cosyCosp)

    return Rotation(
        x = Math.toDegrees(x).toFloat(),
        y = Math.toDegrees(y).toFloat(),
        z = Math.toDegrees(z).toFloat()
    )
}