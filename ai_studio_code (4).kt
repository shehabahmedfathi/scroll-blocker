package com.scrollpause.app

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import kotlin.math.ceil

class OverlayController(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var overlayView: View? = null
    private var countDownTimer: CountDownTimer? = null

    @Volatile
    var isShowing: Boolean = false
        private set

    fun show() {
        mainHandler.post {
            if (isShowing || !Settings.canDrawOverlays(context)) {
                return@post
            }

            val themedContext = ContextThemeWrapper(context, R.style.Theme_ScrollPause)
            val view = LayoutInflater.from(themedContext).inflate(R.layout.overlay_motivational, null)

            val quote = MotivationalQuotes.random()
            view.findViewById<TextView>(R.id.quoteText).text = "\"${quote.text}\""
            view.findViewById<TextView>(R.id.quoteAuthor).text = "— ${quote.author}"

            val countdownNumber = view.findViewById<TextView>(R.id.countdownNumber)
            val countdownProgress = view.findViewById<ProgressBar>(R.id.countdownProgress)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            try {
                windowManager.addView(view, params)
                overlayView = view
                isShowing = true
                startCountdown(countdownNumber, countdownProgress)
            } catch (_: Exception) {
                isShowing = false
                overlayView = null
            }
        }
    }

    private fun startCountdown(countdownNumber: TextView, countdownProgress: ProgressBar) {
        countDownTimer?.cancel()
        val totalMs = 5_000L
        countDownTimer = object : CountDownTimer(totalMs, 50L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = ceil(millisUntilFinished / 1000.0).toInt().coerceAtLeast(1)
                countdownNumber.text = secondsLeft.toString()
                countdownProgress.progress = ((millisUntilFinished * 100) / totalMs).toInt()
            }

            override fun onFinish() {
                countdownNumber.text = "0"
                countdownProgress.progress = 0
                dismiss()
            }
        }.start()
    }

    fun dismiss() {
        mainHandler.post {
            countDownTimer?.cancel()
            countDownTimer = null
            overlayView?.let { view ->
                runCatching { windowManager.removeViewImmediate(view) }
            }
            overlayView = null
            isShowing = false
        }
    }
}