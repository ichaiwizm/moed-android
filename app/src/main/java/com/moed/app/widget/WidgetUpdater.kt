package com.moed.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Rafraîchissement des 3 widgets Glance via **WorkManager** (CONTRACTS §5.6).
 *
 * Deux déclencheurs, tous deux **offline** (`NetworkType.NOT_REQUIRED`, DESIGN §4 « refresh
 * WorkManager, zéro réseau ») :
 *  - [requestUpdate] : mise à jour ponctuelle, appelée par [com.moed.app.ui.MoedViewModel] après toute
 *    mutation d'état (ville, minhag, langue, carnet) → les widgets suivent les réglages en direct ;
 *  - [schedulePeriodic] : rafraîchissement périodique (les zmanim/l'allumage/l'Omer changent chaque
 *    jour), planifié une fois au boot par `MoedApp`.
 *
 * Instanciée avec un [Context] (cf. `MoedViewModel` : `WidgetUpdater(app).requestUpdate()`).
 */
class WidgetUpdater(private val context: Context) {

    /** File une mise à jour immédiate des 3 widgets (déclenchée par les mutations du ViewModel). */
    fun requestUpdate() {
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                .setConstraints(OFFLINE)
                .build()
        )
    }

    /**
     * Planifie le rafraîchissement périodique (toutes les 6 h, `KEEP` = idempotent). À appeler une
     * fois au démarrage de l'application. Offline.
     */
    fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(6, TimeUnit.HOURS)
            .setConstraints(OFFLINE)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_WORK, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    companion object {
        private const val PERIODIC_WORK = "moed_widget_refresh"

        /** Aucune contrainte réseau : le calcul est 100 % on-device (loi non négociable §1). */
        private val OFFLINE = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
    }
}

/**
 * Worker qui recalcule et pousse l'état des 3 widgets. [updateAll] déclenche `provideGlance` sur
 * chaque instance placée, laquelle relit [WidgetData] (DataStore + KosherJava). Best-effort.
 */
class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        CandleWidget().updateAll(applicationContext)
        ZmanimWidget().updateAll(applicationContext)
        OmerWidget().updateAll(applicationContext)
        Result.success()
    } catch (_: Exception) {
        // Rafraîchissement best-effort : on retentera au prochain cycle plutôt que de boucler.
        Result.retry()
    }
}
