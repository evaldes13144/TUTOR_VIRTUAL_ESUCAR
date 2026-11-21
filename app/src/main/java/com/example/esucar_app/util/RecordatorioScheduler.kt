package com.example.esucar_app.util

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.concurrent.TimeUnit

data class ResultadoProgramacionAlerta(
    val programadaParaFuturo: Boolean,
    val mensajeResumen: String
)

object RecordatorioScheduler {

    private val FORMATO_FECHA: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

    private val FORMATO_VISIBLE: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")

    internal fun calcularFechaHoraRecordatorio(
        fechaLimite: String,
        ahora: LocalDateTime = LocalDateTime.now()
    ): LocalDateTime? {
        val fechaEntrega = runCatching { LocalDate.parse(fechaLimite.trim(), FORMATO_FECHA) }
            .getOrNull() ?: return null

        val diaAntes = LocalDateTime.of(fechaEntrega.minusDays(1), LocalTime.of(9, 0))
        if (diaAntes.isAfter(ahora)) return diaAntes

        val mismoDia = LocalDateTime.of(fechaEntrega, LocalTime.of(9, 0))
        if (mismoDia.isAfter(ahora)) return mismoDia

        return null
    }

    fun programarRecordatorioHito(
        context: Context,
        hitoId: Int,
        tituloHito: String,
        fechaLimite: String
    ): ResultadoProgramacionAlerta {
        val ahora = LocalDateTime.now()
        val objetivo = calcularFechaHoraRecordatorio(fechaLimite, ahora)
            ?: return ResultadoProgramacionAlerta(
                programadaParaFuturo = false,
                mensajeResumen = "Fecha próxima o pasada: se envió recordatorio inmediato."
            )

        val delayMillis = Duration.between(ahora, objetivo).toMillis().coerceAtLeast(1_000L)
        val inputData = workDataOf(
            RecordatorioHitoWorker.KEY_HITO_ID to hitoId,
            RecordatorioHitoWorker.KEY_TITULO to tituloHito.trim(),
            RecordatorioHitoWorker.KEY_FECHA_LIMITE to fechaLimite.trim()
        )

        val request = OneTimeWorkRequestBuilder<RecordatorioHitoWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            nombreTrabajoUnico(hitoId),
            ExistingWorkPolicy.REPLACE,
            request
        )

        return ResultadoProgramacionAlerta(
            programadaParaFuturo = true,
            mensajeResumen = "Recordatorio programado para el ${objetivo.format(FORMATO_VISIBLE)}."
        )
    }

    fun cancelarRecordatorio(context: Context, hitoId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(nombreTrabajoUnico(hitoId))
    }

    private fun nombreTrabajoUnico(hitoId: Int): String = "recordatorio_hito_$hitoId"
}
