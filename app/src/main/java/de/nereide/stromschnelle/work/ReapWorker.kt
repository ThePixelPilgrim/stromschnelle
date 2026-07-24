package de.nereide.stromschnelle.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.nereide.stromschnelle.widget.StromschnelleWidget
import java.util.concurrent.TimeUnit

/**
 * Periodically refreshes the home-screen widget so that completed todos whose
 * grace period has elapsed drop out of the visible list.
 *
 * Expiry itself is computed at query time (see [de.nereide.stromschnelle.domain.TodoRepository]);
 * this worker never deletes or mutates any [de.nereide.stromschnelle.data.Todo] data. Its sole
 * job is to nudge the Glance widget to recompose against the current clock so lingering
 * completed items disappear on schedule even without a user-triggered app open.
 */
class ReapWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        StromschnelleWidget().updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "de.nereide.stromschnelle.work.ReapWorker"
        private const val REPEAT_INTERVAL_MINUTES = 15L

        /**
         * Enqueues [ReapWorker] as unique periodic work using [ExistingPeriodicWorkPolicy.KEEP],
         * so re-invoking this (e.g. on every app start) does not reschedule an already-registered
         * job. The Application is expected to call this from `onCreate` since this module must
         * not edit spine files directly.
         */
        fun enqueueReapWork(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReapWorker>(
                REPEAT_INTERVAL_MINUTES, TimeUnit.MINUTES,
            )
                .setConstraints(Constraints.NONE)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
