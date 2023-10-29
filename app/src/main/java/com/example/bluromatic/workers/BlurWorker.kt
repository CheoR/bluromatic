package com.example.bluromatic.workers

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)

        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )

        // By default CoroutineWorker runs as Dispatchers.Default
        // Change dispatcher with withContext()
        return withContext(Dispatchers.IO) {
            // return try {
            // to fix error: return not allowed here
            return@withContext try {

                // throw IllegalArgumentException if first argument evaluates to false
                require(!resourceUri.isNullOrBlank()) {
                    val errorMessage =
                        applicationContext.resources.getString(R.string.invalid_input_uri)
                    Log.e(TAG, errorMessage)
                    errorMessage
                }

                // use ContentResolver object to read contents pointed to by URI
                val resolver = applicationContext.contentResolver


                // Utility function added to emulate slower work.
                delay(DELAY_TIME_MILLIS)

//                val picture = BitmapFactory.decodeResource(
//                    applicationContext.resources,
//                    R.drawable.android_cupcake
//                )

                // image source is now the passed in URI
                val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri))
                )
                // val output = blurBitmap(picture, 1)
                val output = blurBitmap(picture, blurLevel)

                // Write bitmap to temp file
                val outputUri = writeBitmapToFile(applicationContext, output)

                // display notification message
                makeStatusNotification(
                    "Output is $outputUri",
                    applicationContext
                )

                Result.success()
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.error_applying_blur),
                    throwable
                )

                Result.failure()
            }
        }
    }
}

