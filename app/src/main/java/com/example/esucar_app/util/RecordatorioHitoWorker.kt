package com.example.esucar_app.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RecordatorioHitoWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val titulo = inputData.getString(KEY_TITULO) ?: return Result.failure()
        val fechaLimite = inputData.getString(KEY_FECHA_LIMITE) ?: ""
        val hitoId = inputData.getInt(KEY_HITO_ID, titulo.hashCode())

        NotificacionHelper.crearCanalNotificaciones(applicationContext)
        NotificacionHelper.mostrarNotificacionHito(
            context = applicationContext,
            tituloHito = titulo,
            fechaLimite = fechaLimite,
            notificationId = hitoId,
            mensajeExtra = "Tu fecha límite está próxima."
        )
        return Result.success()
    }

    companion object {
        const val KEY_TITULO = "titulo_hito"
        const val KEY_FECHA_LIMITE = "fecha_limite"
        const val KEY_HITO_ID = "hito_id"
    }
}
