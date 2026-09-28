package com.example

import com.example.model.UserSession
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun userSession_greetingName_extractsDisplayNameFirstName() {
        val user = UserSession(
            uid = "user_123",
            email = "aryandas.dev@gmail.com",
            displayName = "Aryan Das"
        )
        assertEquals("Aryan", user.greetingName)
        assertEquals("A", user.avatarLetter)
    }

    @Test
    fun userSession_greetingName_fallsBackToEmailName() {
        val user = UserSession(
            uid = "user_456",
            email = "aryandas.dev@gmail.com",
            displayName = null
        )
        assertEquals("Aryandas", user.greetingName)
    }

    @Test
    fun userSession_anonymous_greetingFallback() {
        val user = UserSession(
            uid = "user_anon",
            email = null,
            displayName = null,
            isAnonymous = true
        )
        assertEquals("Music Lover", user.greetingName)
        assertEquals("M", user.avatarLetter)
    }

    @Test
    fun karaokeProgressClipShape_clipsWidthAccordingToProgress() {
        val shapeHalf = com.example.ui.components.KaraokeProgressClipShape(0.5f)
        val density = androidx.compose.ui.unit.Density(1f)
        val size = androidx.compose.ui.geometry.Size(200f, 40f)
        val outline = shapeHalf.createOutline(size, androidx.compose.ui.unit.LayoutDirection.Ltr, density) as androidx.compose.ui.graphics.Outline.Rectangle

        assertEquals(0f, outline.rect.left, 0.001f)
        assertEquals(0f, outline.rect.top, 0.001f)
        assertEquals(100f, outline.rect.right, 0.001f)
        assertEquals(40f, outline.rect.bottom, 0.001f)
    }

    @Test
    fun karaokeProgressClipShape_clampsProgressBetween0And1() {
        val shapeOver = com.example.ui.components.KaraokeProgressClipShape(1.5f)
        val density = androidx.compose.ui.unit.Density(1f)
        val size = androidx.compose.ui.geometry.Size(300f, 50f)
        val outline = shapeOver.createOutline(size, androidx.compose.ui.unit.LayoutDirection.Ltr, density) as androidx.compose.ui.graphics.Outline.Rectangle

        assertEquals(300f, outline.rect.right, 0.001f)

        val shapeUnder = com.example.ui.components.KaraokeProgressClipShape(-0.2f)
        val outlineUnder = shapeUnder.createOutline(size, androidx.compose.ui.unit.LayoutDirection.Ltr, density) as androidx.compose.ui.graphics.Outline.Rectangle
        assertEquals(0f, outlineUnder.rect.right, 0.001f)
    }
}
