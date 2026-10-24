package com.example.automation

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log

object AppLauncherHelper {

    fun launchAppByPackage(context: Context, packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                // If app is not installed, open Play Store or fallback intent
                openPlayStore(context, packageName)
                false
            }
        } catch (e: Exception) {
            Log.e("AppLauncherHelper", "Error launching package: $packageName", e)
            false
        }
    }

    fun openYouTube(context: Context, searchQuery: String? = null): Boolean {
        return try {
            if (!searchQuery.isNullOrBlank()) {
                val intent = Intent(Intent.ACTION_SEARCH).apply {
                    setPackage("com.google.android.youtube")
                    putExtra("query", searchQuery)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return true
                }
                // Fallback to web YouTube search
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(searchQuery))
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(webIntent)
                return true
            }
            launchAppByPackage(context, "com.google.android.youtube")
        } catch (e: Exception) {
            Log.e("AppLauncherHelper", "Error opening YouTube", e)
            false
        }
    }

    fun openChrome(context: Context, url: String? = null): Boolean {
        return try {
            val targetUrl = if (!url.isNullOrBlank()) {
                if (url.startsWith("http://") || url.startsWith("https://")) url else "https://www.google.com/search?q=${Uri.encode(url)}"
            } else "https://www.google.com"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                setPackage("com.android.chrome")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                // Generic browser fallback
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                true
            }
        } catch (e: Exception) {
            Log.e("AppLauncherHelper", "Error opening browser", e)
            false
        }
    }

    fun openMaps(context: Context, query: String? = null): Boolean {
        return try {
            val uri = if (!query.isNullOrBlank()) {
                Uri.parse("geo:0,0?q=" + Uri.encode(query))
            } else {
                Uri.parse("geo:0,0?q=")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=" + Uri.encode(query ?: ""))).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                true
            }
        } catch (e: Exception) {
            Log.e("AppLauncherHelper", "Error opening maps", e)
            false
        }
    }

    fun openCamera(context: Context): Boolean {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("AppLauncherHelper", "Error opening camera", e)
            false
        }
    }

    fun openGallery(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("AppLauncherHelper", "Error opening gallery", e)
            false
        }
    }

    fun openSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("AppLauncherHelper", "Error opening settings", e)
            false
        }
    }

    fun openDialer(context: Context, phoneNumber: String? = null): Boolean {
        return try {
            val uri = if (!phoneNumber.isNullOrBlank()) Uri.parse("tel:${phoneNumber.trim()}") else Uri.parse("tel:")
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("AppLauncherHelper", "Error opening dialer", e)
            false
        }
    }

    fun openWhatsApp(context: Context, message: String? = null): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                if (!message.isNullOrBlank()) {
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                openPlayStore(context, "com.whatsapp")
                false
            }
        } catch (e: Exception) {
            Log.e("AppLauncherHelper", "Error opening WhatsApp", e)
            false
        }
    }

    fun setTimer(context: Context, seconds: Int, title: String = "Heena AI Timer"): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, title)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AppLauncherHelper", "Error setting timer", e)
            false
        }
    }

    private fun openPlayStore(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
}
