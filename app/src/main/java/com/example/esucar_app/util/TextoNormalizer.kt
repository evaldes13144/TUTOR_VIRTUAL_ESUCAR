package com.example.esucar_app.util

import java.text.Normalizer
import java.util.Locale

object TextoNormalizer {
    private val REGEX_DIACRITICOS = Regex("\\p{Mn}+")

    fun normalizar(texto: String): String {
        val descompuesto = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
        return REGEX_DIACRITICOS.replace(descompuesto, "").lowercase(Locale.ROOT)
    }
}
