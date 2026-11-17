package com.luycaslima.harpia.data.repository

import android.content.Context
import com.luycaslima.harpia.data.local.CameraExtrinsicData
import com.luycaslima.harpia.data.local.CameraIntrinsicsData
import com.luycaslima.harpia.data.local.CapturedPhoto
import com.luycaslima.harpia.data.local.ReconstructionProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

class ProjectStorageRepository(private val context : Context) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val projectRootDir : File
        get() = File(context.filesDir,"projects").apply { mkdirs() }

    fun getProjectDir(projectId : String): File =
        File(projectRootDir,projectId)

    fun getImagesDir(projectId: String): File =
        File(getProjectDir(projectId),"images").apply { mkdirs() }

    fun getPhotoFile( projectId : String , fileName : String) : File =
        File(getImagesDir(projectId),fileName)

    /**
     * Initialize a new project folder and writes its initial projects.json
     * */
    suspend fun createProject(
        name : String,
        intrinsics : CameraIntrinsicsData
    ) : ReconstructionProject = withContext(Dispatchers.IO)
    { // Suspend behaves like an async function already attached to await
        val projectId = UUID.randomUUID().toString()
        getImagesDir(projectId)

        val project = ReconstructionProject(
            id = projectId,
            name = name,
            createdAtMillis = System.currentTimeMillis(),
            intrinsics = intrinsics,
            photos = emptyList()
        )
        writeProjectMetadata(project)
        project
    }

    /**
     * Saves the full-resolution JPEG privately and update the projects.json atomically
     * */
    suspend fun addPhotoProject(
        project : ReconstructionProject,
        jpegBytes : ByteArray,
        pose: CameraExtrinsicData
    ) : ReconstructionProject = withContext(Dispatchers.IO) {
        val nextIndex = project.photos.size
        val fileName = "IMG_%04d.jpg".format(nextIndex)

        val imageFile= getPhotoFile(project.id,fileName)
        imageFile.writeBytes(jpegBytes)

        val newPhoto = CapturedPhoto (
            index = nextIndex,
            fileName = fileName,
            timeStampMillis = System.currentTimeMillis(),
            extrinsic = pose
        )
        val updatedProject = project.copy(
            photos = project.photos + newPhoto
        )
        writeProjectMetadata(updatedProject)
        updatedProject
    }

    /**
     * Load all saved projects for the main screen
     * */
    suspend fun getAllProjects() : List<ReconstructionProject> = withContext(Dispatchers.IO) {
        projectRootDir.listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { dir ->
                val metaFile = File(dir,"project.json")
                if(metaFile.exists()){
                    runCatching {
                        json.decodeFromString<ReconstructionProject>(metaFile.readText())
                    }.getOrNull()
                } else null
            }
            ?.sortedByDescending { it.createdAtMillis }
            ?: emptyList()
    }

    suspend fun deleteProject(projectId : String) = withContext(Dispatchers.IO) {
        getProjectDir(projectId).deleteRecursively()
    }

    private fun writeProjectMetadata(project : ReconstructionProject) {
        val projectDir = getProjectDir(project.id).apply { mkdirs() }
        val targetFile = File(projectDir,"project.json")
        val tempFile = File(projectDir,"project.json.tmp")

        //write tmp first , if the app crash or close wont corrupt the file
        tempFile.writeText(json.encodeToString(project))
        tempFile.renameTo(targetFile)
    }
}