package com.example.lab_week_08

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker
import com.example.lab_week_08.worker.ThirdWorker // -> Import ThirdWorker

class MainActivity : AppCompatActivity() {


    private val workManager by lazy { WorkManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v,
                                                                             insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                systemBars.bottom)
            insets
        }
        // Meminta izin notifikasi untuk Android 13 (TIRAMISU) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val id = "001"
        val firstRequest = OneTimeWorkRequest
            .Builder(FirstWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(FirstWorker
                .INPUT_DATA_ID, id)
            ).build()
        val secondRequest = OneTimeWorkRequest
            .Builder(SecondWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(SecondWorker
                .INPUT_DATA_ID, id)
            ).build()

        // -> Buat request untuk ThirdWorker
        val thirdRequest = OneTimeWorkRequest
            .Builder(ThirdWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(ThirdWorker.INPUT_DATA_ID, "003"))
            .build()

        // -> Perbarui rantai proses untuk menyertakan thirdRequest
        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .then(thirdRequest) // -> Tambahkan ThirdWorker ke rantai
            .enqueue()

        workManager.getWorkInfoByIdLiveData(firstRequest.id)
            .observe(this) { info ->
                if (info != null && info.state.isFinished) {
                    showResult("First process is done")
                }
            }
        workManager.getWorkInfoByIdLiveData(secondRequest.id)
            .observe(this) { info ->
                if (info != null && info.state.isFinished) {
                    showResult("Second process is done")
                    // Panggil service notifikasi setelah proses kedua selesai
                    launchNotificationService()
                }
            }

        // -> Tambahkan observer untuk thirdRequest
        workManager.getWorkInfoByIdLiveData(thirdRequest.id)
            .observe(this) { info ->
                if (info != null && info.state.isFinished) {
                    showResult("Third process is done")
                    // Panggil service notifikasi kedua setelah proses ketiga selesai
                    launchSecondNotificationService()
                }
            }
    }

    //input
    private fun getIdInputData(idKey: String, idValue: String): Data =
        Data.Builder()
            .putString(idKey, idValue)
            .build()
    //Show the result as toast
    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    //Launch the NotificationService
    private fun launchNotificationService() {
        //Observe if the service process is done or not
        //If it is, show a toast with the channel ID in it
        NotificationService.trackingCompletion.observe(
            this) { id -> // 'Id' diganti menjadi 'id' untuk konsistensi
            showResult("Process for Notification Channel ID $id is done!")
        }

        //Create an Intent to start the NotificationService
        //An ID of "001" is also passed as the notification channel ID
        val serviceIntent = Intent(this,
            NotificationService::class.java).apply {
            putExtra(EXTRA_ID, "001")
        }

        //Start the foreground service through the Service Intent
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    // -> Tambahkan fungsi untuk menjalankan SecondNotificationService
    private fun launchSecondNotificationService() {
        SecondNotificationService.trackingCompletion.observe(this) { id ->
            showResult("Process for Second Notification (ID: $id) is done!")
        }

        val serviceIntent = Intent(this, SecondNotificationService::class.java).apply {
            putExtra(SecondNotificationService.EXTRA_ID, "002")
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    companion object{
        const val EXTRA_ID = "Id"
    }
}
