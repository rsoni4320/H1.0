package com.example.automation

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class CommandResult {
    data class Handled(val spokenResponse: String, val actionExecuted: String? = null) : CommandResult()
    data class NeedsConfirmation(val title: String, val message: String, val onConfirm: () -> Unit) : CommandResult()
    data class FallbackToAi(val prompt: String) : CommandResult()
}

class CommandRouter(private val context: Context) {

    fun processCommand(input: String, assistantName: String = "Heena AI"): CommandResult {
        val trimmed = input.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Check for AI structured action tags [[ACTION: TYPE | PARAM]]
        val actionMatch = Regex("\\[\\[ACTION:\\s*([A-Z_]+)\\s*\\|?\\s*(.*?)\\]\\]").find(trimmed)
        if (actionMatch != null) {
            val type = actionMatch.groupValues[1].trim()
            val param = actionMatch.groupValues[2].trim()
            val cleanReply = trimmed.replace(actionMatch.value, "").trim()
            executeAction(type, param)
            return CommandResult.Handled(
                spokenResponse = cleanReply.ifBlank { "कार्य पूरा कर दिया गया है।" },
                actionExecuted = "$type: $param"
            )
        }

        // 2. Local intent rules for instant offline voice response in Hindi & English

        // YouTube
        if (lower.contains("youtube") && (lower.contains("kholo") || lower.contains("open") || lower.contains("chalao") || lower.contains("start"))) {
            // Check if there is a search term
            val query = extractQuery(lower, listOf("youtube par", "youtube pe", "youtube me", "search", "dhundo", "sunao"))
            AppLauncherHelper.openYouTube(context, query)
            val msg = if (query != null) "YouTube पर '$query' सर्च कर रही हूँ।" else "YouTube खोल रही हूँ।"
            return CommandResult.Handled(msg, "OPEN_YOUTUBE")
        }

        // Camera
        if ((lower.contains("camera") || lower.contains("camera kholo") || lower.contains("photo khicho") || lower.contains("take photo")) &&
            (lower.contains("kholo") || lower.contains("open") || lower.contains("on karo") || lower.contains("camera"))
        ) {
            AppLauncherHelper.openCamera(context)
            return CommandResult.Handled("कैमरा खोल रही हूँ।", "OPEN_CAMERA")
        }

        // Chrome / Web Browser
        if (lower.contains("chrome") || (lower.contains("browser") && (lower.contains("open") || lower.contains("kholo")))) {
            AppLauncherHelper.openChrome(context)
            return CommandResult.Handled("Chrome ब्राउज़र खोल रही हूँ।", "OPEN_CHROME")
        }

        // Google Maps
        if (lower.contains("maps") || lower.contains("google map") || lower.contains("map kholo") || lower.contains("rasta dikhao") || lower.contains("navigation")) {
            val destination = extractQuery(lower, listOf("rasta dikhao", "ka rasta", "maps for", "to"))
            AppLauncherHelper.openMaps(context, destination)
            val msg = if (destination != null) "Google Maps में '$destination' का रास्ता खोल रही हूँ।" else "Google Maps खोल रही हूँ।"
            return CommandResult.Handled(msg, "OPEN_MAPS")
        }

        // Gallery / Photos
        if (lower.contains("gallery") || lower.contains("photos") || lower.contains("photo dikhao") || lower.contains("gallery kholo")) {
            AppLauncherHelper.openGallery(context)
            return CommandResult.Handled("गैलरी खोल रही हूँ।", "OPEN_GALLERY")
        }

        // Settings
        if (lower.contains("settings") || lower.contains("setting kholo") || lower.contains("open settings")) {
            AppLauncherHelper.openSettings(context)
            return CommandResult.Handled("फोन सेटिंग्स खोल रही हूँ।", "OPEN_SETTINGS")
        }

        // WhatsApp
        if (lower.contains("whatsapp") && (lower.contains("kholo") || lower.contains("open") || lower.contains("bhejo") || lower.contains("send"))) {
            AppLauncherHelper.openWhatsApp(context)
            return CommandResult.Handled("WhatsApp खोल रही हूँ।", "OPEN_WHATSAPP")
        }

        // Phone call with sensitive confirmation safeguard
        if (lower.startsWith("call") || lower.contains("call lagao") || lower.contains("phone milao") || lower.contains("dial")) {
            val number = lower.filter { it.isDigit() }
            if (number.length >= 3) {
                return CommandResult.NeedsConfirmation(
                    title = "Confirm Phone Call",
                    message = "क्या आप नंबर $number पर कॉल मिलाना चाहते हैं?",
                    onConfirm = { AppLauncherHelper.openDialer(context, number) }
                )
            } else {
                AppLauncherHelper.openDialer(context)
                return CommandResult.Handled("फ़ोन डायलर खोल रही हूँ।", "OPEN_DIALER")
            }
        }

        // Timer
        if (lower.contains("timer") && (lower.contains("set") || lower.contains("lagao") || lower.contains("start"))) {
            var seconds = 300 // default 5 mins
            val number = lower.filter { it.isDigit() }.toIntOrNull()
            if (number != null && number > 0) {
                seconds = if (lower.contains("second")) number else number * 60
            }
            val minutes = seconds / 60
            val success = AppLauncherHelper.setTimer(context, seconds, "Heena AI Timer")
            val msg = if (success) "$minutes मिनट का टाइमर सेट कर दिया है।" else "टाइमर सेट करने के लिए क्लॉक ऐप खोला जा रहा है।"
            return CommandResult.Handled(msg, "SET_TIMER")
        }

        // Date and Time
        if (lower.contains("time kya") || lower.contains("samay kya") || lower.contains("what time") || lower.contains("kitne baje")) {
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            return CommandResult.Handled("अभी समय $time हुआ है।", "GET_TIME")
        }

        if (lower.contains("date kya") || lower.contains("taarikh kya") || lower.contains("what date") || lower.contains("aaj kaun sa din")) {
            val date = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("hi", "IN")).format(Date())
            return CommandResult.Handled("आज की तारीख $date है।", "GET_DATE")
        }

        // 3. Fallback to Gemini AI conversational intelligence
        return CommandResult.FallbackToAi(trimmed)
    }

    private fun executeAction(type: String, param: String) {
        when (type) {
            "OPEN_APP" -> {
                when (param.lowercase()) {
                    "youtube", "com.google.android.youtube" -> AppLauncherHelper.openYouTube(context)
                    "chrome", "com.android.chrome" -> AppLauncherHelper.openChrome(context)
                    "maps", "com.google.android.apps.maps" -> AppLauncherHelper.openMaps(context)
                    "camera" -> AppLauncherHelper.openCamera(context)
                    "gallery" -> AppLauncherHelper.openGallery(context)
                    "settings" -> AppLauncherHelper.openSettings(context)
                    "whatsapp", "com.whatsapp" -> AppLauncherHelper.openWhatsApp(context)
                    "dialer" -> AppLauncherHelper.openDialer(context)
                    else -> AppLauncherHelper.launchAppByPackage(context, param)
                }
            }
            "SEARCH_YOUTUBE" -> AppLauncherHelper.openYouTube(context, param)
            "SEARCH_WEB" -> AppLauncherHelper.openChrome(context, param)
            "OPEN_MAPS" -> AppLauncherHelper.openMaps(context, param)
            "MAKE_CALL" -> AppLauncherHelper.openDialer(context, param)
            "SEND_MESSAGE" -> AppLauncherHelper.openWhatsApp(context, param)
            "SET_TIMER" -> {
                val sec = param.toIntOrNull() ?: 300
                AppLauncherHelper.setTimer(context, sec)
            }
        }
    }

    private fun extractQuery(input: String, keywords: List<String>): String? {
        for (kw in keywords) {
            val idx = input.indexOf(kw)
            if (idx != -1) {
                val sub = input.substring(idx + kw.length).trim()
                if (sub.isNotBlank()) return sub
            }
        }
        return null
    }
}
