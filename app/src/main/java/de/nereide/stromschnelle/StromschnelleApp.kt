package de.nereide.stromschnelle

import android.app.Application
import de.nereide.stromschnelle.work.ReapWorker

/**
 * Application entry point. Builds the [AppContainer] which widgets and workers
 * reach via `(context.applicationContext as StromschnelleApp).container`.
 */
class StromschnelleApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        ReapWorker.enqueueReapWork(this)
    }
}
