package com.luycaslima.harpia.data

import com.google.ar.core.CameraIntrinsics

data class ReconstructionProject (val name : String, val photos : Array<CapturedImage>, val intrinsics: CameraIntrinsics
    ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReconstructionProject

        if (name != other.name) return false
        if (!photos.contentEquals(other.photos)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + photos.contentHashCode()
        return result
    }
}