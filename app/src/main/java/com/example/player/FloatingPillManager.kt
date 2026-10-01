package com.example.player

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.hypot

/**
 * System-Wide Draggable Floating Music Status Bar Pill.
 *
 * Appears ONLY when the user leaves the app (background/home screen) while audio is active.
 * Disappears IMMEDIATELY when the user re-enters the app.
 * Can be freely dragged anywhere across the entire device screen.
 * Shows track art, live animated equalizer, title, artist, and play/pause controls.
 */
class FloatingPillManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var pillView: ViewGroup? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var titleView: TextView? = null
    private var artistView: TextView? = null
    private var artImageView: ImageView? = null
    private var equalizerView: MiniEqualizerView? = null
    private var playPauseBtn: ImageView? = null
    private var closeBtn: ImageView? = null

    private var isShowing = false
    private var savedX = -1
    private var savedY = -1

    private val density = context.resources.displayMetrics.density

    fun show(
        title: String,
        artist: String,
        artworkUrl: String?,
        artworkBitmap: Bitmap?,
        isPlaying: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            return
        }

        if (isShowing) {
            update(title, artist, artworkUrl, artworkBitmap, isPlaying)
            return
        }

        try {
            if (pillView == null) {
                buildPillView()
            }

            val view = pillView ?: return
            val params = layoutParams ?: return

            // Position calculation
            val screenW = context.resources.displayMetrics.widthPixels
            val estimatedWidth = (220f * density).toInt()
            if (savedX == -1) {
                params.x = ((screenW - estimatedWidth) / 2).coerceAtLeast(0)
            } else {
                params.x = savedX
            }

            if (savedY == -1) {
                params.y = (70f * density).toInt() // Just below system status bar
            } else {
                params.y = savedY
            }

            updateViewContent(title, artist, artworkUrl, artworkBitmap, isPlaying)

            windowManager?.addView(view, params)
            isShowing = true
        } catch (e: Exception) {
            e.printStackTrace()
            isShowing = false
        }
    }

    fun update(
        title: String,
        artist: String,
        artworkUrl: String?,
        artworkBitmap: Bitmap?,
        isPlaying: Boolean
    ) {
        if (!isShowing) {
            show(title, artist, artworkUrl, artworkBitmap, isPlaying)
            return
        }

        updateViewContent(title, artist, artworkUrl, artworkBitmap, isPlaying)
    }

    private fun updateViewContent(
        title: String,
        artist: String,
        artworkUrl: String?,
        artworkBitmap: Bitmap?,
        isPlaying: Boolean
    ) {
        titleView?.text = if (title.isNotBlank()) title else "Playing Audio"
        artistView?.text = if (artist.isNotBlank()) artist else "Vibes Music"

        equalizerView?.setPlaying(isPlaying)

        playPauseBtn?.setImageResource(
            if (isPlaying) R.drawable.ic_pill_pause else R.drawable.ic_pill_play
        )

        if (artworkBitmap != null) {
            artImageView?.setImageBitmap(artworkBitmap)
        } else if (!artworkUrl.isNullOrBlank()) {
            scope.launch(Dispatchers.IO) {
                try {
                    val loader = Coil.imageLoader(context)
                    val req = ImageRequest.Builder(context)
                        .data(artworkUrl)
                        .allowHardware(false)
                        .build()
                    val res = (loader.execute(req) as? SuccessResult)?.drawable
                    val bmp = (res as? BitmapDrawable)?.bitmap
                    withContext(Dispatchers.Main) {
                        if (bmp != null) {
                            artImageView?.setImageBitmap(bmp)
                        } else {
                            artImageView?.setImageResource(R.drawable.ic_vibes_logo)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        artImageView?.setImageResource(R.drawable.ic_vibes_logo)
                    }
                }
            }
        } else {
            artImageView?.setImageResource(R.drawable.ic_vibes_logo)
        }
    }

    fun hide() {
        if (!isShowing) return
        try {
            pillView?.let {
                windowManager?.removeView(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isShowing = false
            equalizerView?.setPlaying(false)
        }
    }

    fun destroy() {
        hide()
        scope.cancel()
        pillView = null
    }

    private fun buildPillView() {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f * density
                setColor(Color.parseColor("#F0141620")) // Deep dark glass
                setStroke((1.5f * density).toInt(), Color.parseColor("#441DB954")) // Spotify Green glow border
            }
            background = bg
            elevation = 16f * density
            val padH = (10f * density).toInt()
            val padV = (6f * density).toInt()
            setPadding(padH, padV, padH, padV)
        }

        // Album Art
        val artSize = (34f * density).toInt()
        val art = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(artSize, artSize)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.ic_vibes_logo)
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, view.height / 2f)
                }
            }
            clipToOutline = true
        }
        artImageView = art
        root.addView(art)

        // Equalizer Bars
        val eq = MiniEqualizerView(context).apply {
            val p = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            p.marginStart = (8f * density).toInt()
            p.marginEnd = (6f * density).toInt()
            layoutParams = p
        }
        equalizerView = eq
        root.addView(eq)

        // Track Info Column
        val textCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            p.marginEnd = (8f * density).toInt()
            layoutParams = p
        }

        val tvTitle = TextView(context).apply {
            textSize = 12f
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.END
            maxWidth = (130f * density).toInt()
            text = "Playing Audio"
        }
        titleView = tvTitle
        textCol.addView(tvTitle)

        val tvArtist = TextView(context).apply {
            textSize = 10f
            setTextColor(Color.parseColor("#A6AAB8"))
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.END
            maxWidth = (130f * density).toInt()
            text = "Vibes Music"
        }
        artistView = tvArtist
        textCol.addView(tvArtist)

        root.addView(textCol)

        // Play / Pause Button
        val btnSize = (30f * density).toInt()
        val btnPlay = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(btnSize, btnSize).apply {
                marginEnd = (4f * density).toInt()
            }
            val pad = (4f * density).toInt()
            setPadding(pad, pad, pad, pad)
            setImageResource(R.drawable.ic_pill_pause)
        }
        playPauseBtn = btnPlay
        root.addView(btnPlay)

        // Close 'X' Button
        val closeSize = (24f * density).toInt()
        val btnClose = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(closeSize, closeSize)
            val pad = (4f * density).toInt()
            setPadding(pad, pad, pad, pad)
            setImageResource(R.drawable.ic_pill_close)
        }
        closeBtn = btnClose
        root.addView(btnClose)

        // Configure Window LayoutParams
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        layoutParams = params

        // Setup unified drag and tap interactions
        setupTouchInteractions(root, btnPlay, btnClose)

        pillView = root
    }

    private fun setupTouchInteractions(root: View, btnPlay: View, btnClose: View) {
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        var downOnPlay = false
        var downOnClose = false

        root.setOnTouchListener { _, event ->
            val params = layoutParams ?: return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    downOnPlay = isPointInsideView(event.rawX, event.rawY, btnPlay)
                    downOnClose = isPointInsideView(event.rawX, event.rawY, btnClose)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()

                    if (!isDragging && hypot(dx.toDouble(), dy.toDouble()) > touchSlop) {
                        isDragging = true
                    }

                    if (isDragging) {
                        val metrics = context.resources.displayMetrics
                        val screenW = metrics.widthPixels
                        val screenH = metrics.heightPixels
                        val viewW = root.width.coerceAtLeast((180f * density).toInt())
                        val viewH = root.height.coerceAtLeast((44f * density).toInt())

                        // Allow full screen drag while keeping pill safely within viewport
                        params.x = (initialX + dx).coerceIn(0, screenW - viewW)
                        params.y = (initialY + dy).coerceIn(20, screenH - viewH - 20)

                        savedX = params.x
                        savedY = params.y

                        try {
                            windowManager?.updateViewLayout(root, params)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        if (downOnPlay && isPointInsideView(event.rawX, event.rawY, btnPlay)) {
                            // Tapped Play/Pause toggle
                            MediaPlaybackService.activePlayerManager?.togglePlayPause()
                        } else if (downOnClose && isPointInsideView(event.rawX, event.rawY, btnClose)) {
                            // Tapped Close button
                            hide()
                        } else {
                            // Tapped pill body -> open MainActivity to return to app!
                            openMainActivity()
                        }
                    }
                    downOnPlay = false
                    downOnClose = false
                    true
                }

                else -> false
            }
        }
    }

    private fun isPointInsideView(rawX: Float, rawY: Float, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        val w = view.width
        val h = view.height
        return rawX >= x && rawX <= (x + w) && rawY >= y && rawY <= (y + h)
    }

    private fun openMainActivity() {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 3-Bar Mini Animated Equalizer with smooth sinusoidal motion.
     */
    private class MiniEqualizerView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1DB954")
            style = Paint.Style.FILL
        }
        private val rect = RectF()
        private var isPlaying = false
        private var animator: ValueAnimator? = null
        private var phase = 0f

        private val density = context.resources.displayMetrics.density
        private val barWidth = 2.5f * density
        private val barSpacing = 2f * density
        private val minHeight = 3.5f * density
        private val maxHeight = 15f * density
        private val cornerRadius = 1.25f * density

        fun setPlaying(playing: Boolean) {
            if (isPlaying == playing) return
            isPlaying = playing
            if (playing) {
                startAnimation()
            } else {
                stopAnimation()
            }
        }

        private fun startAnimation() {
            if (animator?.isRunning == true) return
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 750L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                interpolator = LinearInterpolator()
                addUpdateListener {
                    phase = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }

        private fun stopAnimation() {
            animator?.cancel()
            animator = null
            phase = 0f
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cy = height / 2f
            for (i in 0 until 3) {
                val h = if (isPlaying) {
                    val offset = i * 0.33f
                    val wave = ((Math.sin(((phase + offset) * 2 * Math.PI)) + 1.0) / 2.0).toFloat()
                    minHeight + wave * (maxHeight - minHeight)
                } else {
                    minHeight
                }
                val left = i * (barWidth + barSpacing)
                val top = cy - (h / 2f)
                val right = left + barWidth
                val bottom = cy + (h / 2f)
                rect.set(left, top, right, bottom)
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val totalWidth = (3 * barWidth + 2 * barSpacing).toInt()
            val totalHeight = (18f * density).toInt()
            setMeasuredDimension(totalWidth, totalHeight)
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            stopAnimation()
        }
    }
}
