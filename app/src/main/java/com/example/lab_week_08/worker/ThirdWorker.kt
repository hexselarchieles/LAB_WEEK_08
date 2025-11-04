// In app/src/main/java/com/example/lab_week_08/worker/ThirdWorker.kt
package com.example.lab_week_08.worker

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class ThirdWorker(
    context: Context, workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val id = inputData.getString(INPUT_DATA_ID)
        Log.d("ThirdWorker", "Third worker process started for ID: $id")

        // Simulasi proses yang berjalan selama 2 detik
        Thread.sleep(2000L)

        val outputData = Data.Builder()
            .putString(OUTPUT_DATA_ID, id)
            .build()

        Log.d("ThirdWorker", "Third worker process finished.")
        return Result.success(outputData)
    }

    companion object {
        const val INPUT_DATA_ID = "inId"
        const val OUTPUT_DATA_ID = "outId"
    }
}
