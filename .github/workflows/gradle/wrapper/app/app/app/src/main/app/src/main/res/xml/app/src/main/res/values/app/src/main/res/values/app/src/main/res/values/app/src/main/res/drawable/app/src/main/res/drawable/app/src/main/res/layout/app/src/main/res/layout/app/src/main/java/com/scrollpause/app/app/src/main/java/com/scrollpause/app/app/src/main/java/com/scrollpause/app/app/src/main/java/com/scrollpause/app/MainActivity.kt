package com.scrollpause.app

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var accessibilityStatusText: TextView
    private lateinit var overlayStatusText: TextView
    private lateinit var btnPreviewOverlay: MaterialButton
    private lateinit var overlayController: OverlayController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        overlayController = OverlayController(applicationContext)

        accessibilityStatusText = findViewById(R.id.accessibilityStatusText)
        overlayStatusText = findViewById(R.id.overlayStatusText)
        btnPreviewOverlay = findViewById(R.id.btnPreviewOverlay)

        findViewById<MaterialButton>(R.id.btnAccessibilitySettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<MaterialButton>(R.id.btnOverlaySettings).setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        btnPreviewOverlay.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                overlayController.show()
            } else {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        val accessibilityEnabled = isAccessibilityServiceEnabled()
        accessibilityStatusText.text = getString(
            if (accessibilityEnabled) R.string.status_accessibility_enabled
            else R.string.status_accessibility_disabled
        )
        accessibilityStatusText.setTextColor(
            ContextCompat.getColor(
                this,
                if (accessibilityEnabled) R.color.status_ok else R.color.status_warn
            )
        )

        val overlayGranted = Settings.canDrawOverlays(this)
        overlayStatusText.text = getString(
            if (overlayGranted) R.string.status_overlay_granted
            else R.string.status_overlay_denied
        )
        overlayStatusText.setTextColor(
            ContextCompat.getColor(
                this,
                if (overlayGranted) R.color.status_ok else R.color.status_warn
            )
        )

        btnPreviewOverlay.isEnabled = overlayGranted
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponent = ComponentName(this, ScrollSentinelService::class.java)
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            val componentName = ComponentName.unflattenFromString(splitter.next())
            if (componentName == expectedComponent) {
                return true
            }
        }
        return false
    }
}
