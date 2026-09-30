package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceSearchState {
    object Idle : VoiceSearchState()
    object Listening : VoiceSearchState()
    data class Processing(val partialText: String = "") : VoiceSearchState()
    data class Success(val recognizedText: String) : VoiceSearchState()
    data class Error(val message: String) : VoiceSearchState()
}

class VoiceSearchHelper(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow<VoiceSearchState>(VoiceSearchState.Idle)
    val state: StateFlow<VoiceSearchState> = _state.asStateFlow()

    private val _rmsAmplitude = MutableStateFlow(0f)
    val rmsAmplitude: StateFlow<Float> = _rmsAmplitude.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    val isRecognitionAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        mainHandler.post {
            stopListeningInternal()

            if (!isRecognitionAvailable) {
                _state.value = VoiceSearchState.Error("Speech recognition is not available on this device. You can speak or select a search term below.")
                return@post
            }

            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _state.value = VoiceSearchState.Listening
                        }

                        override fun onBeginningOfSpeech() {
                            _state.value = VoiceSearchState.Processing("Listening...")
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            // Convert rmsdB (-2dB to ~12dB) to 0f..1f range for fluid visualizer
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1f)
                            _rmsAmplitude.value = normalized
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _state.value = VoiceSearchState.Processing("Processing voice query...")
                        }

                        override fun onError(error: Int) {
                            val message = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly into the microphone."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Please tap the mic and try again."
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check your microphone."
                                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network connection issue. Please check your internet."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required for voice search."
                                else -> "Could not recognize speech. Tap to try again or choose a suggestion below."
                            }
                            Log.w("VoiceSearchHelper", "SpeechRecognizer error: $error -> $message")
                            _state.value = VoiceSearchState.Error(message)
                            _rmsAmplitude.value = 0f
                        }

                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val bestMatch = matches?.firstOrNull()?.trim()
                            if (!bestMatch.isNullOrBlank()) {
                                val normalized = normalizeVoiceQuery(bestMatch)
                                _state.value = VoiceSearchState.Success(normalized)
                            } else {
                                _state.value = VoiceSearchState.Error("No match found. Please try again.")
                            }
                            _rmsAmplitude.value = 0f
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = partials?.firstOrNull() ?: ""
                            if (text.isNotBlank()) {
                                _state.value = VoiceSearchState.Processing(text)
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a song title, artist name, or lyrics...")
                }

                speechRecognizer?.startListening(intent)
                _state.value = VoiceSearchState.Listening
            } catch (e: Exception) {
                Log.e("VoiceSearchHelper", "Failed to start speech recognizer", e)
                _state.value = VoiceSearchState.Error("Voice search encountered an error: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    private fun stopListeningInternal() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("VoiceSearchHelper", "Error stopping speech recognizer", e)
        } finally {
            speechRecognizer = null
            _rmsAmplitude.value = 0f
        }
    }

    fun stopListening() {
        mainHandler.post {
            stopListeningInternal()
        }
    }

    fun simulateVoiceMatch(query: String = "Cruel Summer") {
        mainHandler.post {
            _state.value = VoiceSearchState.Listening
            mainHandler.postDelayed({
                _state.value = VoiceSearchState.Processing("Recognized: $query")
                mainHandler.postDelayed({
                    _state.value = VoiceSearchState.Success(query)
                }, 600)
            }, 1000)
        }
    }

    fun reset() {
        mainHandler.post {
            stopListeningInternal()
            _state.value = VoiceSearchState.Idle
        }
    }

    companion object {
        fun normalizeVoiceQuery(raw: String): String {
            var query = raw.trim()
            val prefixes = listOf(
                "can you please play the song",
                "can you please play song",
                "can you please play",
                "could you please play",
                "could you play",
                "please play the song",
                "please play song",
                "please play",
                "play the song",
                "play songs by",
                "play song by",
                "play tracks by",
                "play music by",
                "play song",
                "play",
                "search for songs by",
                "search for song",
                "search for songs",
                "search for music by",
                "search for",
                "search songs by",
                "search",
                "find songs by",
                "find song by",
                "find the song",
                "find song",
                "find",
                "listen to song",
                "listen to",
                "put on the song",
                "put on song",
                "put on",
                "look up song",
                "look up"
            )
            for (prefix in prefixes) {
                if (query.startsWith(prefix, ignoreCase = true)) {
                    val stripped = query.substring(prefix.length).trim()
                    if (stripped.isNotBlank()) {
                        query = stripped
                        break
                    }
                }
            }
            // Strip leading "by " if left over (e.g. from "play by Taylor Swift")
            query = query.replace(Regex("^(by\\s+)", RegexOption.IGNORE_CASE), "").trim()
            return query
        }
    }
}

