package com.luycaslima.harpia.data.local

import kotlinx.serialization.Serializable


@Serializable
data class CameraIntrinsicsData
    (val fx: Float,
     val fy: Float,
     val cx : Float,
     val cy : Float,
     val width : Int,
     val height : Int) {}

@Serializable
data class CameraExtrinsicData (
    val positionX: Float,
    val positionY: Float,
    val positionZ: Float,
    val rotationQx: Float,
    val rotationQy: Float,
    val rotationQz: Float,
    val rotationQw: Float) {}

@Serializable
data class CapturedPhoto (
    val index : Int,
    val fileName : String,
    val timeStampMillis : Long,
    val extrinsic: CameraExtrinsicData
    ) {}

@Serializable
data class ReconstructionProject (
    val id : String,
    val name : String,
    val createdAtMillis : Long,
    val photos : List<CapturedPhoto> = emptyList(),
    val intrinsics: CameraIntrinsicsData
    ) {}


