package com.macosta.maikpet

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.macosta.maikpet.worker.CheckNewMascotasWorker
import com.google.android.gms.ads.MobileAds
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MaikPetApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar AdMob
        MobileAds.initialize(this) {}
        
        FirebaseMessaging.getInstance().subscribeToTopic("nuevas_mascotas")
        
        CheckNewMascotasWorker.schedule(this)
    }
}
