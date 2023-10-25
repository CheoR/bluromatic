package com.example.bluromatic.data

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.WorkInfo
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class WorkManagerBluromaticRepository(context: Context) : BluromaticRepository {

    override val outputWorkInfo: Flow<WorkInfo?> = MutableStateFlow(null)

    /**
     * Create WorkRequests to apply blur and save resulting image
     * @param blurLevel amount to blur image
     */
    override fun applyBlur(blurLevel: Int) {}

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
