package com.example.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "AiAssistantRepository"

class AiAssistantRepository {

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    Kamu adalah Asisten UMKM WarungKasir yang ramah, praktis, dan paham seluk-beluk warung kelontong & UMKM di Indonesia.
                    Selalu jawab dalam Bahasa Indonesia yang jelas, hangat, ringkas, dan siap pakai (misalnya mudah disalin ke WhatsApp atau catatan belanja).
                    Gunakan format Rupiah (Rp) yang rapi. Hindari basa-basi panjang.
                    """.trimIndent()
                )
            }
        )
    }

    fun streamResponse(prompt: String): Flow<String> = flow {
        try {
            val accumulated = StringBuilder()
            withTimeout(60_000L) {
                model.generateContentStream(prompt).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    if (text.isNotEmpty()) {
                        accumulated.append(text)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("Asisten AI tidak mengembalikan jawaban. Silakan coba lagi.")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini streaming failed", e)
            throw e
        }
    }
}
