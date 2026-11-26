package com.scrollpause.app

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

class ScrollSentinelService : AccessibilityService() {

    private lateinit var overlayController: OverlayController

    private val scrollTimestamps = ArrayDeque<Long>()
    private var lastGestureTimeMs = 0L
    private var currentPackage: String? = null
    private var lastScrollY = 0
    private var lastFromIndex = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayController = OverlayController(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (packageName !in TARGET_PACKAGES) {
                    resetTracking()
                    currentPackage = null
                } else if (packageName != currentPackage) {
                    resetTracking()
                    currentPackage = packageName
                }
            }

            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (packageName !in TARGET_PACKAGES || overlayController.isShowing) {
                    return
                }

                if (isUpwardScroll(event)) {
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastGestureTimeMs < MIN_GESTURE_GAP_MS) {
                        return
                    }
                    lastGestureTimeMs = now
                    scrollTimestamps.addLast(now)

                    while (scrollTimestamps.isNotEmpty() && now - scrollTimestamps.first() > WINDOW_MS) {
                        scrollTimestamps.removeFirst()
                    }

                    if (scrollTimestamps.size >= SCROLL_THRESHOLD) {
                        scrollTimestamps.clear()
                        overlayController.show()
                    }
                }
            }
        }
    }

    private fun isUpwardScroll(event: AccessibilityEvent): Boolean {
        val upwardByDelta = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            when {
                event.scrollDeltaY > 0 -> true
                event.scrollDeltaY < 0 -> false
                else -> null
            }
        } else {
            null
        }

        val currentScrollY = event.scrollY
        val currentFromIndex = event.fromIndex

        val result = when {
            upwardByDelta != null -> upwardByDelta
            currentScrollY >= 0 && currentScrollY != lastScrollY -> currentScrollY > lastScrollY
            currentFromIndex >= 0 && currentFromIndex != lastFromIndex -> currentFromIndex > lastFromIndex
            else -> false
        }

        if (currentScrollY >= 0) lastScrollY = currentScrollY
        if (currentFromIndex >= 0) lastFromIndex = currentFromIndex
        return result
    }

    private fun resetTracking() {
        scrollTimestamps.clear()
        lastScrollY = 0
        lastFromIndex = 0
    }

    override fun onInterrupt() {
        if (::overlayController.isInitialized) {
            overlayController.dismiss()
        }
    }

    override fun onDestroy() {
        if (::overlayController.isInitialized) {
            overlayController.dismiss()
        }
        super.onDestroy()
    }

    companion object {
        private val TARGET_PACKAGES = setOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.snapchat.android"
        )
        private const val SCROLL_THRESHOLD = 5
        private const val WINDOW_MS = 15_000L
        private const val MIN_GESTURE_GAP_MS = 400L
    }
}