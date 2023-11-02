package com.example.bluromatic.data

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.bluromatic.IMAGE_MANIPULATION_WORK_NAME
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.TAG_OUTPUT
import com.example.bluromatic.getImageUri
import com.example.bluromatic.workers.BlurWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow


import com.example.bluromatic.workers.CleanupWorker
import com.example.bluromatic.workers.SaveImageToFileWorker

class WorkManagerBluromaticRepository(context: Context) : BluromaticRepository {

    override val outputWorkInfo: Flow<WorkInfo?> = MutableStateFlow(null)
    private var imageUri: Uri = context.getImageUri()
    private val workManager = WorkManager.getInstance(context)

    /**
     * Create WorkRequests to apply blur and save resulting image
     * @param blurLevel amount to blur image
     */
    override fun applyBlur(blurLevel: Int) {

        // WorkRequest to Cleanup temporary images
        // beginWith() returns WorkContinuation object and creates starting point for chain of
        // WorkRequest`s with first work request in chain.
        // Note alternate way to create OneTimeWorkRequest object.
        // Calling OneTimeWorkRequest.from(CleanupWorker::class.java) is equivalent to calling
        // OneTimeWorkRequestBuilder<CleanupWorker>().build().
        // OneTimeWorkRequest class comes from AndroidX Work library while
        // OneTimeWorkRequestBuilder is helper function provided by WorkManager KTX extension.
        // var continuation = workManager
        //  .beginWith(
        //      OneTimeWorkRequest.from(CleanupWorker::class.java)
        //  )

        // to chain work to run at as chunk before starting another chunk of work requests
        //  provide unique string name. Name entire chain to refer to and query them together
        var continuation = workManager
            .beginUniqueWork(
                IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanupWorker::class.java)
            )

        // Create WorkRequest to blur image
        val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()

        // input data object
        blurBuilder.setInputData(createInputDataForWorkRequest(blurLevel, imageUri))

        // Start single work request
        // workManager.enqueue(blurBuilder.build())

        // to add blur work request to chain
        continuation = continuation.then(blurBuilder.build())

        // WorkRequest to save image to filesystem
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .addTag(TAG_OUTPUT)
            .build()
        continuation = continuation.then(save)

        // Start work chain
        continuation.enqueue()

    }

    /**
     * Cancel any ongoing WorkRequests
     * */
    override fun cancelWork() {}

    /**
     * Creates input data bundle which includes blur level to
     * update amount of blur to be applied and Uri to operate on
     * @return Data which contains Image Uri as String and blur level as Integer
     */
    private fun createInputDataForWorkRequest(blurLevel: Int, imageUri: Uri): Data {
        val builder = Data.Builder()
        builder
            .putString(
                KEY_IMAGE_URI, imageUri.toString()
            ).putInt(
                KEY_BLUR_LEVEL, blurLevel
            )
        return builder.build()
    }
}
