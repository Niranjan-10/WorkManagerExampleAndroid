package com.example.workmanagerdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.work.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
//            setOneTimeWorkRequest()
            setPeriodicWorkRequest()
        }
    }

    private fun setOneTimeWorkRequest() {
        val workManagerInstance = WorkManager.getInstance(applicationContext)

        val data: Data = Data.Builder()
            .putInt(KEY_COUNT_VALUE, 125)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresCharging(true)
            .build()

        val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        val filteringRequest = OneTimeWorkRequest.Builder(FilteringWorker::class.java)
            .build()

        val compressingRequest = OneTimeWorkRequest.Builder(CompressingWorker::class.java)
            .build()

        val downloadingRequest = OneTimeWorkRequest.Builder(DownloadingWorker::class.java)
            .build()


//        ------------------> Parallel Work
        val parallelWorks: MutableList<OneTimeWorkRequest> = mutableListOf<OneTimeWorkRequest>()
        parallelWorks.add(downloadingRequest)
        parallelWorks.add(filteringRequest)
        workManagerInstance
            .beginWith(parallelWorks)
            .then(compressingRequest)
            .then(uploadRequest)
            .enqueue()

        //------------>  Chaining
//        workManagerInstance
//            .beginWith(filteringRequest)
//            .then(compressingRequest)
//            .then(uploadRequest)
//            .enqueue()

        workManagerInstance.getWorkInfoByIdLiveData(uploadRequest.id)
            .observe(this, Observer {
                textView2.text = it.state.name
                if (it.state.isFinished) {
                    val data = it.outputData
                    val message = data.getString(UploadWorker.KEY_WORKER)
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
            })
    }


    private fun setPeriodicWorkRequest() {
        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(DownloadingWorker::class.java, 16, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(applicationContext).enqueue(periodicWorkRequest)

    }

    companion object {
        const val KEY_COUNT_VALUE = "key_count"
    }
}