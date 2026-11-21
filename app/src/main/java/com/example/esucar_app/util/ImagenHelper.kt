package com.example.esucar_app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ImagenHelper {

    fun crearArchivoEvidencia(context: Context): File {
        val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
        return File(carpeta, "evidencia_${System.currentTimeMillis()}.jpg")
    }

    fun obtenerUriFileProvider(context: Context, archivo: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }

    fun decodificarBitmapEscalado(ruta: String, maxDimensionPx: Int = 1024): Bitmap? {
        return runCatching {
            val archivo = File(ruta)
            if (!archivo.exists() || archivo.length() == 0L) return null

            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(ruta, boundsOptions)

            val ancho = boundsOptions.outWidth
            val alto = boundsOptions.outHeight
            if (ancho <= 0 || alto <= 0) return null

            var inSampleSize = 1
            while ((ancho / inSampleSize) > maxDimensionPx || (alto / inSampleSize) > maxDimensionPx) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            BitmapFactory.decodeFile(ruta, decodeOptions)
        }.getOrNull()
    }

    fun eliminarArchivoSiExiste(ruta: String?) {
        if (ruta.isNullOrBlank()) return
        runCatching {
            val archivo = File(ruta)
            if (archivo.exists()) archivo.delete()
        }
    }
}
