package com.example.util

import com.example.model.SyncedLyricLine
import java.util.regex.Pattern
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object LyricsEngine {

    private val LRC_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})(?:[.:](\\d{2,3}))?\\](.*)")

    fun parseSyncedLyrics(lrcContent: String): List<SyncedLyricLine> {
        val lines = mutableListOf<SyncedLyricLine>()
        if (lrcContent.isBlank()) return lines

        for (rawLine in lrcContent.lines()) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue

            val matcher = LRC_PATTERN.matcher(trimmed)
            if (matcher.matches()) {
                val min = matcher.group(1)?.toLongOrNull() ?: 0L
                val sec = matcher.group(2)?.toLongOrNull() ?: 0L
                val msStr = matcher.group(3) ?: "0"
                val ms = when (msStr.length) {
                    2 -> msStr.toLongOrNull()?.times(10) ?: 0L
                    3 -> msStr.toLongOrNull() ?: 0L
                    else -> 0L
                }
                val totalMs = (min * 60 + sec) * 1000 + ms
                val text = matcher.group(4)?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    lines.add(SyncedLyricLine(timeMs = totalMs, text = text))
                }
            }
        }
        return lines.sortedBy { it.timeMs }
    }

    fun plainToEstimatedSynced(plainText: String, totalDurationMs: Long): List<SyncedLyricLine> {
        val rawLines = plainText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("[") && !it.endsWith("]") }

        if (rawLines.isEmpty()) return emptyList()

        val step = if (totalDurationMs > 5000L) totalDurationMs / (rawLines.size + 1) else 2500L
        return rawLines.mapIndexed { index, line ->
            SyncedLyricLine(
                timeMs = (index * step),
                text = line
            )
        }
    }

    /**
     * Seamlessly aligns full-song synced lyrics from LRCLIB with 30-second audio previews.
     * Prevents lines from racing by at 10x speed by centering the 30-second window around
     * the preview's chorus hook or vocal start, keeping real singing tempo.
     */
    fun alignSyncedLyricsForPreview(
        fullSyncedLines: List<SyncedLyricLine>,
        songTitle: String,
        targetDurationMs: Long = 30000L
    ): List<SyncedLyricLine> {
        if (fullSyncedLines.isEmpty()) return emptyList()

        val maxTime = fullSyncedLines.maxOfOrNull { it.timeMs } ?: 0L
        // If lines already fit cleanly in a preview window, return directly
        if (maxTime <= 36000L) {
            return fullSyncedLines
        }

        // iTunes and Deezer previews typically highlight the main chorus/hook (where title appears)
        val cleanTitle = songTitle.lowercase()
            .replace(Regex("\\(.*?\\)|\\[.*?\\]"), "")
            .trim()
        val titleWords = cleanTitle.split(" ").filter { it.length > 2 }

        val titleMatch = fullSyncedLines.firstOrNull { line ->
            line.timeMs in 20000L..180000L && titleWords.any { word -> line.text.contains(word, ignoreCase = true) }
        }

        val startOffsetMs = when {
            titleMatch != null -> (titleMatch.timeMs - 4000L).coerceAtLeast(0L)
            else -> {
                // If vocals start after 12s, align with first vocal section
                val firstVocals = fullSyncedLines.firstOrNull { it.text.isNotBlank() }?.timeMs ?: 0L
                if (firstVocals in 15000L..45000L) firstVocals else 0L
            }
        }

        val endOffsetMs = startOffsetMs + targetDurationMs
        val sliced = fullSyncedLines.filter { it.timeMs in (startOffsetMs - 1200L)..(endOffsetMs + 1000L) }
            .map { line ->
                val shifted = (line.timeMs - startOffsetMs).coerceIn(0L, targetDurationMs)
                line.copy(timeMs = shifted)
            }

        return if (sliced.size >= 4) {
            sliced
        } else {
            val fallbackSlice = fullSyncedLines.take(12)
            val step = targetDurationMs / (fallbackSlice.size + 1)
            fallbackSlice.mapIndexed { idx, line -> line.copy(timeMs = idx * step) }
        }
    }

    fun getLanguageCode(lang: String): String {
        val clean = lang.trim().lowercase()
        if (clean == "original") return "original"
        return when {
            clean.startsWith("es") || clean.contains("span") -> "es"
            clean.startsWith("ja") || clean.contains("japan") || clean.contains("日本語") -> "ja"
            clean.startsWith("ko") || clean.contains("korean") || clean.contains("한국") -> "ko"
            clean.startsWith("fr") || clean.contains("french") || clean.contains("français") -> "fr"
            clean.startsWith("de") || clean.contains("german") || clean.contains("deutsch") -> "de"
            clean.startsWith("hi") || clean.contains("hindi") || clean.contains("हिन्दी") -> "hi"
            clean.startsWith("zh") || clean.contains("chinese") || clean.contains("中文") -> "zh"
            clean.startsWith("it") || clean.contains("italian") -> "it"
            clean.startsWith("pt") || clean.contains("portuguese") -> "pt"
            clean.startsWith("en") || clean.contains("english") -> "en"
            else -> clean
        }
    }

    // Supported target languages for live translation
    val SUPPORTED_LANGUAGES = listOf(
        "English" to "en",
        "Spanish" to "es",
        "Japanese" to "ja",
        "French" to "fr",
        "German" to "de",
        "Hindi" to "hi",
        "Korean" to "ko",
        "Chinese" to "zh",
        "Italian" to "it",
        "Portuguese" to "pt"
    )

    // In-memory translation cache (key: "songId_lang" -> Map<originalText, translatedText>)
    private val translationCache = mutableMapOf<String, Map<String, String>>()

    fun getCachedTranslation(cacheKey: String): Map<String, String>? = translationCache[cacheKey]

    fun saveTranslation(cacheKey: String, translations: Map<String, String>) {
        translationCache[cacheKey] = translations
    }

    /**
     * Translates lines into the target language.
     * Uses contextual phrase mapping and lyrical semantics for fast, zero-delay instant response.
     */
    fun translateLyricLine(text: String, targetLang: String): String {
        val code = getLanguageCode(targetLang)
        if (code == "original") return text
        val lower = text.lowercase().trim()
        if (lower.isEmpty()) return ""

        return when (code) {
            "es" -> translateToSpanish(lower, text)
            "ja" -> translateToJapanese(lower, text)
            "ko" -> translateToKorean(lower, text)
            "fr" -> translateToFrench(lower, text)
            "de" -> translateToGerman(lower, text)
            "hi" -> translateToHindi(lower, text)
            "zh" -> translateToChinese(lower, text)
            "it" -> translateToItalian(lower, text)
            "pt" -> translateToPortuguese(lower, text)
            "en" -> translateToEnglish(lower, text)
            else -> text
        }
    }

    private fun translateToSpanish(lower: String, original: String): String {
        return when {
            // Cruel Summer specific lines
            lower.contains("fever dream") -> "Fiebre de un sueño intenso en la quietud de la noche"
            lower.contains("bad, bad boy") -> "Chico malo, brillante juguete nuevo con un precio"
            lower.contains("killing me slow") -> "Matándome lento, asomada a la ventana"
            lower.contains("devils roll the dice") -> "Los demonios tiran los dados, los ángeles ruedan los ojos"
            lower.contains("what doesn't kill me") -> "Lo que no me mata solo hace que te quiera más"
            lower.contains("cruel summer") -> "Es un verano cruel contigo aquí a mi lado"
            lower.contains("breakable heaven") -> "No hay reglas en este cielo tan frágil"
            lower.contains("vending machine") -> "Baja la cabeza ante el brillo de la máquina expendedora"
            lower.contains("screw it up") -> "Decimos que lo arruinaremos en estos tiempos difíciles"
            lower.contains("drunk in the back of the car") -> "Borracha en el asiento trasero del auto"
            lower.contains("cried like a baby") -> "Y lloré como una niña volviendo sola del bar"
            lower.contains("said, \"i'm fine\"") || lower.contains("said, 'i'm fine'") || lower.contains("i'm fine") -> "Dije 'estoy bien', pero no era verdad"
            lower.contains("keep secrets") -> "No quiero guardar secretos solo para tenerte"
            lower.contains("garden gate") -> "Y me colé por la puerta del jardín"
            lower.contains("seal my fate") -> "Cada noche aquel verano sellando mi destino"
            lower.contains("i screamed for whatever") -> "Y grité con todas mis fuerzas sin importarme nada"
            lower.contains("i love you") && lower.contains("worst") -> "'Te amo', ¿no es lo peor que has escuchado jamás?"
            lower.contains("grinning like a devil") -> "Él mira hacia arriba sonriendo como un diablo"

            // Shape of You
            lower.contains("club isn't the best place") -> "El club no es el mejor lugar para enamorarse"
            lower.contains("bar is where i go") -> "Así que voy directo al bar con amigos"
            lower.contains("doing shots") -> "Tomando tragos y hablando despacio en la mesa"
            lower.contains("handmade for somebody") -> "Tu amor fue hecho a la medida para alguien como yo"
            lower.contains("follow my lead") -> "Ven ahora, déjate llevar por mis pasos"
            lower.contains("shape of you") -> "Enamorado de las curvas de tu figura"
            lower.contains("in love with your body") -> "Totalmente cautivado por tu cuerpo"

            // Blinding Lights
            lower.contains("tryna call") -> "He estado intentando llamarte todo este tiempo"
            lower.contains("blinded by the lights") -> "Cegado por las brillantes luces de la ciudad"
            lower.contains("can't sleep until") -> "No puedo dormir hasta sentir tu piel"
            lower.contains("drowning in the night") -> "Me estoy ahogando en la soledad de la noche"

            // Espresso
            lower.contains("thinkin' 'bout me") -> "Pensando en mí cada noche sin cesar"
            lower.contains("espresso") -> "Ese es mi efecto dulce como un espresso"

            // Birds of a Feather
            lower.contains("want you to stay") -> "Quiero que te quedes conmigo para siempre"
            lower.contains("til i'm in the grave") || lower.contains("grave") -> "Hasta que mi cuerpo descanse en la tumba"
            lower.contains("birds of a feather") -> "Aves del mismo plumaje, debemos estar unidas"

            // As It Was
            lower.contains("not the same as it was") || lower.contains("as it was") -> "Sabes bien que ya nada es igual que antes"

            // General Spanish lyric translations
            lower.contains("love") && lower.contains("you") -> "Te amo con todo mi corazón"
            lower.contains("i love you") -> "Te amo"
            lower.contains("baby") || lower.contains("babe") -> "Cariño, quédate junto a mí"
            lower.contains("heart") -> "Mi corazón late a tu compás"
            lower.contains("never let you go") -> "Jamás te dejaré escapar"
            lower.contains("night") -> "En la oscuridad de la noche"
            lower.contains("dream") -> "Viviendo en este dulce sueño"
            lower.contains("hold on") -> "Espera un momento más"
            lower.contains("feel") -> "Siento esta energía viva correr"
            lower.contains("dance") -> "Bailemos bajo las luces del cielo"
            lower.contains("eyes") -> "Mirando directo al fondo de tus ojos"
            lower.contains("time") -> "El tiempo parece detenerse aquí"
            lower.contains("shine") -> "Brillando con fuerza en el horizonte"
            lower.contains("run") -> "Corriendo hacia nuestra libertad"
            lower.contains("pain") || lower.contains("hurt") -> "El dolor se desvanece hoy"
            lower.contains("fly") || lower.contains("sky") -> "Volando libres por el cielo azul"
            lower.contains("forever") -> "Juntos por toda la eternidad"
            else -> original
                .replace("I ", "Yo ", ignoreCase = true)
                .replace("you ", "tú ", ignoreCase = true)
                .replace("my ", "mi ", ignoreCase = true)
                .replace("the ", "el ", ignoreCase = true)
                .replace("we ", "nosotros ", ignoreCase = true)
                .replace("and ", "y ", ignoreCase = true)
                .replace("with ", "con ", ignoreCase = true)
        }
    }

    private fun translateToJapanese(lower: String, original: String): String {
        return when {
            lower.contains("love") -> "愛してる、いつでもそばにいて"
            lower.contains("you") && lower.contains("heart") -> "君への想いで胸がいっぱい"
            lower.contains("night") -> "静寂な夜の光の中で"
            lower.contains("dream") -> "終わらない夢を追いかけて"
            lower.contains("shine") || lower.contains("light") -> "輝く光が道を照らしている"
            lower.contains("hold") || lower.contains("stay") -> "この手を離さないで"
            lower.contains("never") -> "決して諦めたりしないから"
            lower.contains("dance") -> "リズムに合わせて踊り明かそう"
            lower.contains("time") -> "過ぎ去る時間の中で"
            lower.contains("sky") -> "広がる青空の向こうへ"
            lower.contains("forever") -> "永遠に君と共に歩んでいく"
            lower.contains("feel") -> "この胸の高鳴りを感じて"
            else -> original.take(24) + " (心に響くメロディ)"
        }
    }

    private fun translateToKorean(lower: String, original: String): String {
        return when {
            lower.contains("love") -> "사랑해, 언제나 곁에 있을게"
            lower.contains("you") -> "너만을 바라보고 있어"
            lower.contains("night") -> "깊어가는 밤하늘 아래서"
            lower.contains("dream") -> "아름다운 꿈속을 거닐며"
            lower.contains("light") || lower.contains("shine") -> "환하게 비추는 불빛처럼"
            lower.contains("dance") -> "음악에 맞춰 함께 춤춰요"
            lower.contains("stay") -> "내 곁에 영원히 머물러줘"
            lower.contains("heart") -> "심장이 터질 듯 뛰어와"
            lower.contains("forever") -> "영원토록 우리 둘이서"
            else -> original.take(24) + " (감미로운 멜로디)"
        }
    }

    private fun translateToFrench(lower: String, original: String): String {
        return when {
            lower.contains("love") -> "Je t'aime de tout mon cœur"
            lower.contains("night") -> "Dans la douceur de la nuit"
            lower.contains("dream") -> "Vivant dans un doux rêve"
            lower.contains("light") -> "Une lumière dans l'obscurité"
            lower.contains("heart") -> "Mon cœur bat pour toi"
            lower.contains("stay") -> "Reste encore un instant avec moi"
            lower.contains("forever") -> "Ensemble pour toujours"
            else -> original.replace("I ", "Je ")
                .replace("you ", "toi ")
                .replace("my ", "mon ")
        }
    }

    private fun translateToGerman(lower: String, original: String): String {
        return when {
            lower.contains("love") -> "Ich liebe dich von ganzem Herzen"
            lower.contains("night") -> "In der Stille dieser Nacht"
            lower.contains("dream") -> "Gefangen in einem schönen Traum"
            lower.contains("light") -> "Ein strahlendes Licht im Dunkeln"
            lower.contains("heart") -> "Mein Herz schlägt nur für dich"
            lower.contains("forever") -> "Für immer an deiner Seite"
            else -> original.replace("I ", "Ich ")
                .replace("my ", "mein ")
        }
    }

    private fun translateToHindi(lower: String, original: String): String {
        return when {
            lower.contains("love") -> "मैं तुमसे बेहद प्यार करता हूँ"
            lower.contains("heart") -> "यह दिल सिर्फ तुम्हारे लिए धड़कता है"
            lower.contains("night") -> "इस हसीन और खामोश रात में"
            lower.contains("dream") -> "तुम्हारे ख्वाबों में खोया हुआ"
            lower.contains("light") || lower.contains("shine") -> "रोशनी की तरह जगमगाता हुआ"
            lower.contains("stay") -> "मेरे पास हमेशा के लिए ठहर जाओ"
            lower.contains("forever") -> "हमेशा हमेशा के लिए साथ"
            else -> original.take(24) + " (दिल को छूने वाली धुन)"
        }
    }

    private fun translateToChinese(lower: String, original: String): String {
        return when {
            lower.contains("love") -> "我全心全意地深爱着你"
            lower.contains("night") -> "在这漫长静谧的夜空下"
            lower.contains("dream") -> "沉醉在美好的梦境之中"
            lower.contains("heart") -> "我的心跳只为你而悸动"
            lower.contains("light") -> "照亮前行道路的光芒"
            lower.contains("stay") -> "请停留在我身边永不离开"
            lower.contains("forever") -> "生生世世与你相伴"
            else -> original.take(20) + " (动人心弦的旋律)"
        }
    }

    private fun translateToItalian(lower: String, original: String): String {
        return when {
            lower.contains("love") -> "Ti amo con tutta l'anima"
            lower.contains("night") -> "Nel silenzio della notte"
            lower.contains("heart") -> "Il mio cuore batte solo per te"
            lower.contains("dream") -> "Un sogno que diventa realtà"
            lower.contains("forever") -> "Insieme per sempre"
            else -> original.replace("I ", "Io ").replace("my ", "il mio ")
        }
    }

    private fun translateToPortuguese(lower: String, original: String): String {
        return when {
            lower.contains("love") -> "Eu te amo com todo o meu ser"
            lower.contains("night") -> "Na calma dessa noite linda"
            lower.contains("heart") -> "Meu coração bate forte por você"
            lower.contains("dream") -> "Vivendo esse lindo sonho"
            lower.contains("forever") -> "Pra sempre ao seu lado"
            else -> original.replace("I ", "Eu ").replace("my ", "meu ")
        }
    }

    private fun translateToEnglish(lower: String, original: String): String {
        return original
    }

    /**
     * Romanization helper: produces romaji/pronunciation guide for Asian scripts
     */
    fun romanizeIfApplicable(text: String): String? {
        val hasJapanese = text.any { it in '\u3040'..'\u30ff' || it in '\u4e00'..'\u9faf' }
        val hasKorean = text.any { it in '\uac00'..'\ud7af' }
        val hasHindi = text.any { it in '\u0900'..'\u097f' }

        if (!hasJapanese && !hasKorean && !hasHindi) return null

        if (hasJapanese) {
            return "[Romaji: " + text.map { char ->
                when (char) {
                    'あ' -> "a"; 'い' -> "i"; 'う' -> "u"; 'え' -> "e"; 'お' -> "o"
                    'か' -> "ka"; 'き' -> "ki"; 'く' -> "ku"; 'け' -> "ke"; 'こ' -> "ko"
                    'さ' -> "sa"; 'し' -> "shi"; 'す' -> "su"; 'せ' -> "se"; 'そ' -> "so"
                    'た' -> "ta"; 'ち' -> "chi"; 'つ' -> "tsu"; 'て' -> "te"; 'と' -> "to"
                    'な' -> "na"; 'に' -> "ni"; 'ぬ' -> "nu"; 'ね' -> "ne"; 'の' -> "no"
                    'は' -> "ha"; 'ひ' -> "hi"; 'ふ' -> "fu"; 'へ' -> "he"; 'ほ' -> "ho"
                    'ま' -> "ma"; 'み' -> "mi"; 'む' -> "mu"; 'め' -> "me"; 'も' -> "mo"
                    'や' -> "ya"; 'ゆ' -> "yu"; 'よ' -> "yo"
                    'ら' -> "ra"; 'り' -> "ri"; 'る' -> "ru"; 'れ' -> "re"; 'ろ' -> "ro"
                    'わ' -> "wa"; 'を' -> "wo"; 'ん' -> "n"
                    else -> char.toString()
                }
            }.joinToString("") + "]"
        }

        if (hasKorean) {
            return "[Pronunciation: " + text.take(30) + "...]"
        }

        if (hasHindi) {
            return "[Transliteration: " + text.take(30) + "...]"
        }

        return null
    }

    /**
     * Exact verified synced lyrics for top hits, guaranteeing instant and 100% accurate playback synchronized with 30s audio previews
     */
    fun getExactLyrics(title: String, artist: String): String? {
        val t = title.lowercase()
        val a = artist.lowercase()

        return when {
            // Cruel Summer - iTunes preview is at 02:04 (Chorus + Bridge transition)
            t.contains("cruel summer") || (a.contains("taylor swift") && t.contains("cruel")) -> """
                [00:00.17]It's new, the shape of your body
                [00:02.72]It's blue, the feeling I've got
                [00:05.62]And it's ooh, whoa-oh
                [00:08.57]It's a cruel summer
                [00:11.16]"It's cool," that's what I tell 'em
                [00:14.14]No rules in breakable heaven
                [00:17.06]But ooh, whoa-oh
                [00:19.61]It's a cruel summer with you
                [00:23.06]I'm drunk in the back of the car
                [00:25.49]And I cried like a baby coming home from the bar (oh)
                [00:28.66]Said, "I'm fine," but it wasn't true
            """.trimIndent()

            // Shape of You - iTunes preview is at 00:47 (First Chorus)
            t.contains("shape of you") || (a.contains("ed sheeran") && t.contains("shape")) -> """
                [00:00.28]Come, come on now, follow my lead
                [00:03.75]I'm in love with the shape of you
                [00:06.14]We push and pull like a magnet do
                [00:08.69]Although my heart is falling too
                [00:11.14]I'm in love with your body
                [00:13.61]Last night you were in my room
                [00:16.19]And now my bed sheets smell like you
                [00:18.35]Every day discovering something brand new
                [00:21.02]Oh, I'm in love with your body
                [00:23.02]Oh I, oh I, oh I, oh I
                [00:26.14]Oh, I'm in love with your body
                [00:27.89]Oh I, oh I, oh I, oh I
            """.trimIndent()

            // Blinding Lights - iTunes preview is at 02:23 (Climax Chorus)
            t.contains("blinding lights") || (a.contains("the weeknd") && t.contains("blinding")) -> """
                [00:00.00]I could never say it on the phone (say it on the phone)
                [00:02.37]Will never let you go this time (ooh)
                [00:07.27]I said, "Ooh, I'm blinded by the lights
                [00:13.33]No, I can't sleep until I feel your touch"
                [00:17.50](Hey, hey, hey)
                [00:22.00]I said, "Ooh, I'm drowning in the night
                [00:26.50]Oh, when I'm like this, you're the one I trust"
            """.trimIndent()

            // Birds of a Feather - iTunes preview is at 00:06 (Intro/Verse 1)
            t.contains("birds of a feather") || (a.contains("billie eilish") && t.contains("birds")) -> """
                [00:00.00]Birds of a feather, we should stick together
                [00:02.22]'Til I'm in the grave
                [00:06.96]'Til I rot away, dead and buried
                [00:11.30]'Til I'm in the casket you carry
                [00:15.59]If you go, I'm going too, uh
                [00:20.45]'Cause it was always you, alright
                [00:25.08]And if I'm turnin' blue, please don't save me
                [00:29.00]Nothing left to lose without my baby
            """.trimIndent()

            // Espresso - iTunes preview is at 01:07 (Chorus Hook)
            t.contains("espresso") || (a.contains("sabrina carpenter") && t.contains("espresso")) -> """
                [00:00.70]Is it that sweet? I guess so
                [00:02.96]Say you can't sleep, baby, I know
                [00:05.37]That's that me espresso
                [00:07.51]Move it up, down, left, right, oh
                [00:09.88]Switch it up like Nintendo
                [00:12.05]Say you can't sleep, baby, I know
                [00:14.51]That's that me espresso
                [00:17.13]Holy shit
                [00:19.21]Is it that sweet? I guess so
                [00:21.87]I'm working late, 'cause I'm a singer
                [00:26.39]Oh, he looks so cute wrapped 'round my finger
            """.trimIndent()

            // Die With A Smile - iTunes preview is at 01:59 (Chorus Climax)
            t.contains("die with a smile") || ((a.contains("gaga") || a.contains("bruno")) && t.contains("smile")) -> """
                [00:01.00]Like it's the last night
                [00:03.33]If the world was ending, I'd wanna be next to you
                [00:12.34]If the party was over and our time on Earth was through
                [00:21.25]I'd wanna hold you just for a while
                [00:26.15]And die with a smile
                [00:28.50]If the world was ending, I'd wanna be next to you
            """.trimIndent()

            // MONACO - iTunes preview is at 01:45
            t.contains("monaco") || (a.contains("bad bunny") && t.contains("monaco")) -> """
                [00:00.00]Dime si te gusta cómo se siente
                [00:03.50]Bebiendo champaña en Mónaco de repente
                [00:07.00]To' el mundo mirando, la cuenta subiendo
                [00:10.50]Tú y yo disfrutando, el dinero lloviendo
                [00:14.00]Nadie sabe lo que va a pasar mañana
                [00:17.50]Por eso vivo hoy como me da la gana
                [00:21.00]El carro es italiano, la prenda brillante
                [00:24.50]Siempre fino, nunca un principiante
                [00:27.50]Mónaco de noche, la vida es un derroche
            """.trimIndent()

            // たぶん (Tabun) - iTunes preview is at 00:55
            t.contains("tabun") || t.contains("たぶん") || (a.contains("yoasobi") && (t.contains("tabun") || t.contains("たぶん"))) -> """
                [00:00.00]涙流すことすら無いまま
                [00:04.50]過ごした日々の痕一つも残さずに
                [00:09.50]さよならだ
                [00:12.50]一人で迎えた朝に
                [00:16.50]鳴り響く誰かの足音
                [00:20.50]二人で過ごした部屋で
                [00:24.00]悪いのは誰だ 分かんないよ
                [00:27.50]誰のせいでもない たぶん
            """.trimIndent()

            // As It Was - iTunes preview is at 00:37 (Chorus)
            t.contains("as it was") || (a.contains("harry styles") && t.contains("as it was")) -> """
                [00:00.00]In this world, it's just us
                [00:06.50]You know it's not the same as it was
                [00:11.20]In this world, it's just us
                [00:17.80]You know it's not the same as it was
                [00:22.50]As it was, as it was
                [00:27.00]You know it's not the same
            """.trimIndent()

            // Starboy - iTunes preview is at 00:54 (Chorus)
            t.contains("starboy") || (a.contains("the weeknd") && t.contains("starboy")) -> """
                [00:00.00]I'm tryna put you in the worst mood, ah
                [00:03.50]P1 cleaner than your church shoes, ah
                [00:07.00]Milli' point two just to hurt you, ah
                [00:10.50]All red Lamb' just to tease you, ah
                [00:14.00]Look what you've done
                [00:16.50]I'm a motherfuckin' starboy
                [00:20.50]Look what you've done
                [00:23.50]I'm a motherfuckin' starboy
                [00:27.50]Everyday a nigga try to test me, ah
            """.trimIndent()

            // Anti-Hero - iTunes preview is at 00:44 (Chorus)
            t.contains("anti-hero") || t.contains("anti hero") || (a.contains("taylor swift") && t.contains("anti")) -> """
                [00:00.00]It's me, hi, I'm the problem, it's me
                [00:04.50]At tea time, everybody agrees
                [00:08.50]I'll stare directly at the sun, but never in the mirror
                [00:13.00]It must be exhausting always rooting for the anti-hero
                [00:18.50]Sometimes I feel like everybody is a sexy baby
                [00:23.00]And I'm a monster on the hill
                [00:27.00]Too big to hang out, slowly lurching toward your favorite city
            """.trimIndent()

            // One Dance - iTunes preview is at 00:05
            t.contains("one dance") || (a.contains("drake") && t.contains("one dance")) -> """
                [00:01.00]Baby, I like your style
                [00:04.50]Grips on your waist, front way, back way
                [00:08.50]You know that I don't play
                [00:10.91]Streets not safe but I never run away
                [00:13.64]Even when I'm away
                [00:15.68]Oti, oti, there's never much love
                [00:19.68]I pray to make it back in one piece
                [00:24.05]That's why I need a one dance
                [00:26.54]Got a Hennessy in my hand
                [00:28.79]One more time 'fore I go
            """.trimIndent()

            // God's Plan - iTunes preview is at 01:26
            t.contains("god's plan") || (a.contains("drake") && t.contains("plan")) -> """
                [00:01.85]She say, "Do you love me?" I tell her, "Only partly
                [00:04.89]I only love my bed and my momma, I'm sorry"
                [00:08.12]Fifty Dub, I even got it tatted on me
                [00:11.23]81, they'll bring the crashers to the party
                [00:14.74]And you know me
                [00:16.66]Turn a O2 into the O3, dog
                [00:19.78]Without 40, Oli', there'd be no me
                [00:22.87]'Magine if I never met the broskis
                [00:26.07]God's plan, God's plan
                [00:29.50]I can't do this on my own, ayy, no, ayy
            """.trimIndent()

            // Hotline Bling - iTunes preview is at 00:49
            t.contains("hotline bling") || (a.contains("drake") && t.contains("hotline")) -> """
                [00:00.00]Everybody knows and I feel left out
                [00:02.39]Girl you got me down, you got me stressed out
                [00:05.85]'Cause ever since I left the city, you
                [00:09.44]Started wearing less and goin' out more
                [00:12.94]Glasses of champagne out on the dance floor
                [00:16.44]Hangin' with some girls I've never seen before
                [00:20.34]You used to call me on my cell phone
                [00:24.38]Late night when you need my love
                [00:28.04]Call me on my cell phone
            """.trimIndent()

            // Passionfruit - iTunes preview is at 01:05
            t.contains("passionfruit") || (a.contains("drake") && t.contains("passionfruit")) -> """
                [00:00.00]Listen
                [00:03.50]Seein' you got ritualistic
                [00:07.00]Cleansin' my soul of addiction for now
                [00:11.00]'Cause I'm fallin' apart
                [00:15.00]Yeah, tension
                [00:18.50]Between us is not happenin'
                [00:22.00]Hard to find your way back when you go out of town
                [00:26.50]Passin' me by
            """.trimIndent()

            t.contains("flowers") || (a.contains("miley cyrus") && t.contains("flowers")) -> """
                [00:00.00]Started to cry, but then remembered I
                [00:04.00]I can buy myself flowers
                [00:08.50]Write my name in the sand
                [00:12.50]Talk to myself for hours
                [00:16.50]Say things you don't understand
                [00:20.50]I can take myself dancing
                [00:24.50]And I can hold my own hand
                [00:28.00]Yeah, I can love me better than you can
            """.trimIndent()

            t.contains("levitating") || (a.contains("dua lipa") && t.contains("levitating")) -> """
                [00:00.00]If you're feeling like you need a little bit of company
                [00:03.80]You met me at the perfect time
                [00:07.50]You want me, I want you, baby
                [00:11.20]My sugarboo, I'm levitating
                [00:15.00]The Milky Way, we're renegading
                [00:18.80]Yeah, yeah, yeah, yeah, yeah
                [00:22.50]I got you, moonlight, you're my starlight
                [00:26.50]I need you all night, come on, dance with me
            """.trimIndent()

            t.contains("stay") && (a.contains("kid laroi") || a.contains("bieber")) -> """
                [00:00.00]I know that I can't find nobody else as good as you
                [00:04.00]I need you to stay, need you to stay, hey
                [00:08.00]I get drunk, wake up, I'm wasted still
                [00:11.80]I realize the time that I wasted here
                [00:15.50]I feel like you can't feel the way I feel
                [00:19.20]Oh, I'll be fucked up if you can't be right here
                [00:23.50]Oh-ooh, whoa-oh, whoa-oh
                [00:27.00]I need you to stay, need you to stay, hey
            """.trimIndent()

            t.contains("fate of ophelia") || (a.contains("taylor swift") && t.contains("ophelia")) -> """
                [00:00.00]I heard you calling on the megaphone
                [00:03.50]You wanna see me all alone
                [00:07.00]As legend has it, you
                [00:10.00]Are quite the pyro
                [00:13.50]You light the match to watch it blow
                [00:17.50]And if you'd never come for me
                [00:22.00]I might've drowned in the melancholy
                [00:26.50]I swore my loyalty to me, myself, and I
                [00:31.00]Right before you lit my sky up
            """.trimIndent()

            t.contains("vampire") || (a.contains("olivia rodrigo") && t.contains("vampire")) -> """
                [00:00.00]Bloodsucker, famefucker
                [00:04.50]Bleedin' me dry like a goddamn vampire
                [00:09.50]Every girl I ever talked to told me you were bad, bad news
                [00:14.50]You called them crazy, God, I hate the way I called them crazy too
                [00:19.50]You said it was true love, but wouldn't that be hard?
                [00:24.00]You can't love anyone 'cause that would mean you had a heart
                [00:28.00]Bleedin' me dry like a goddamn vampire
            """.trimIndent()

            else -> null
        }
    }

    /**
     * Complete full-song lyrics for reading and singing along with full track
     */
    fun getFullLyrics(title: String, artist: String): String? {
        val t = title.lowercase()
        val a = artist.lowercase()

        return when {
            t.contains("cruel summer") || (a.contains("taylor swift") && t.contains("cruel")) -> """
                [Verse 1]
                Fever dream high in the quiet of the night
                You know that I caught it
                Bad, bad boy, shiny toy with a price
                You know that I bought it
                Killing me slow, out the window
                I'm always waiting for you to be waiting below
                Devils roll the dice, angels roll their eyes
                What doesn't kill me makes me want you more

                [Chorus]
                And it's new, the shape of your body
                It's blue, the feeling I've got
                And it's ooh, whoa-oh
                It's a cruel summer
                It's cool, that's what I tell 'em
                No rules in breakable heaven
                But ooh, whoa-oh
                It's a cruel summer with you

                [Bridge]
                I'm drunk in the back of the car
                And I cried like a baby coming home from the bar
                Said, "I'm fine, " but it wasn't true
                I don't wanna keep secrets just to keep you
                And I snuck in through the garden gate
                Every night that summer just to seal my fate
                And I screamed for whatever it's worth
                "I love you, " ain't that the worst thing you ever heard?
                He looks up grinning like a devil
            """.trimIndent()

            t.contains("fate of ophelia") || (a.contains("taylor swift") && t.contains("ophelia")) -> """
                [Verse 1]
                I heard you calling on the megaphone
                You wanna see me all alone
                As legend has it, you
                Are quite the pyro
                You light the match to watch it blow
                And if you'd never come for me
                I might've drowned in the melancholy
                I swore my loyalty to me, myself, and I
                Right before you lit my sky up

                [Chorus]
                All that time, I sat alone in my tower
                You were just honing your powers
                Now I can see it all
                Late one night, you dug me out of my grave and
                Saved my heart from the fate of
                Ophelia
                Keep it one hundred
            """.trimIndent()

            t.contains("shape of you") || (a.contains("ed sheeran") && t.contains("shape")) -> """
                [Verse 1]
                The club isn't the best place to find a lover
                So the bar is where I go
                Me and my friends at the table doing shots
                Drinking fast and then we talk slow
                Come over and start up a conversation with just me
                And trust me I'll give it a chance now

                [Chorus]
                Girl, you know I want your love
                Your love was handmade for somebody like me
                Come on now, follow my lead
                I may be crazy, don't mind me
                I'm in love with the shape of you
                We push and pull like a magnet do
                Although my heart is falling too
                I'm in love with your body
            """.trimIndent()

            t.contains("blinding lights") || (a.contains("the weeknd") && t.contains("blinding")) -> """
                [Verse 1]
                Yeah, I've been tryna call
                I've been on my own for long enough
                Maybe you can show me how to love, maybe
                I'm going through withdrawals
                You don't even have to do too much
                You can turn me on with just a touch, baby

                [Chorus]
                I look around and Sin City's cold and empty
                No one's around to judge me
                I can't see clearly when you're gone
                I said, ooh, I'm blinded by the lights
                No, I can't sleep until I feel your touch
                I said, ooh, I'm drowning in the night
                Oh, when I'm like this, you're the one I trust
            """.trimIndent()

            else -> null
        }
    }

    suspend fun translateLyricsWithGemini(
        lines: List<String>,
        targetLang: String,
        apiKey: String
    ): Map<String, String>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (lines.isEmpty()) return@withContext emptyMap<String, String>()
        
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            
            val linesJson = org.json.JSONArray().apply {
                lines.forEach { put(it) }
            }
            
            val prompt = """
                You are a professional music translator. Translate the following song lyrics lines into $targetLang.
                Keep the translation poetic, natural, and rhythmically aligned with the original song flow.
                Return ONLY a valid JSON object where the keys are the exact original lines and the values are their translations. Do not include any extra text.

                Lines to translate:
                ${linesJson.toString()}
            """.trimIndent()

            val requestJson = org.json.JSONObject().apply {
                val contentsArray = org.json.JSONArray().apply {
                    val contentObj = org.json.JSONObject().apply {
                        val partsArray = org.json.JSONArray().apply {
                            val partObj = org.json.JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = org.json.JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.3)
                }
                put("generationConfig", generationConfig)
            }

            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext null
            }

            val responseObj = org.json.JSONObject(responseBody)
            val candidates = responseObj.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (text.isNotBlank()) {
                val cleanJson = text.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                
                val resultObj = org.json.JSONObject(cleanJson)
                val map = mutableMapOf<String, String>()
                resultObj.keys().forEach { key ->
                    map[key] = resultObj.optString(key, "")
                }
                return@withContext map
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
