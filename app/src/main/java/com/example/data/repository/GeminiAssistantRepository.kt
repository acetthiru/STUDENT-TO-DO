package com.example.data.repository

import com.example.BuildConfig
import com.example.data.model.Subtask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class GeminiAssistantRepository {

    suspend fun breakDownTask(taskTitle: String, description: String = ""): List<Subtask> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val subtasks = callGeminiApiForSubtasks(taskTitle, description, apiKey)
                if (subtasks.isNotEmpty()) return@withContext subtasks
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Fallback intelligent subtask generator
        return@withContext generateLocalSubtasks(taskTitle)
    }

    suspend fun generateExamStudySchedule(
        subjectName: String,
        topics: List<String>,
        daysRemaining: Int
    ): List<Subtask> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val promptText = "Create a structured $daysRemaining-day study schedule for '$subjectName' covering topics: ${topics.joinToString(", ")}. Return JSON array of strings: [\"Day 1: Read Chapter 1\", \"Day 2: Solve Practice Problems\"]"
                val schedule = callGeminiApiForArray(promptText, apiKey)
                if (schedule.isNotEmpty()) {
                    return@withContext schedule.map { Subtask(id = UUID.randomUUID().toString(), title = it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Fallback schedule generator based on syllabus topics & days remaining
        val scheduleList = mutableListOf<Subtask>()
        if (topics.isNotEmpty()) {
            val topicsPerDay = (topics.size.toFloat() / daysRemaining.coerceAtLeast(1).toFloat()).coerceAtLeast(1f)
            topics.forEachIndexed { idx, topic ->
                val estimatedDay = ((idx / topicsPerDay) + 1).toInt()
                scheduleList.add(
                    Subtask(
                        id = UUID.randomUUID().toString(),
                        title = "Day $estimatedDay: Study & Master '$topic'"
                    )
                )
            }
            scheduleList.add(Subtask(UUID.randomUUID().toString(), "Final Day: Practice Mock Exam & Formula Review"))
        } else {
            scheduleList.add(Subtask(UUID.randomUUID().toString(), "Phase 1: Syllabus Core Reading & Note Making"))
            scheduleList.add(Subtask(UUID.randomUUID().toString(), "Phase 2: Practice Numerical & Essay Questions"))
            scheduleList.add(Subtask(UUID.randomUUID().toString(), "Phase 3: Formula Memorization & Flashcards"))
            scheduleList.add(Subtask(UUID.randomUUID().toString(), "Phase 4: Full Length Practice Exam"))
        }
        return@withContext scheduleList
    }

    private fun getApiKey(): String? {
        return try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String
        } catch (e: Exception) {
            null
        }
    }

    private fun callGeminiApiForSubtasks(title: String, desc: String, apiKey: String): List<Subtask> {
        val promptText = "Break down this task into 3 to 5 clear, actionable subtasks in JSON array format [\"Subtask 1\", \"Subtask 2\"]: Title: '$title'. Description: '$desc'."
        val rawList = callGeminiApiForArray(promptText, apiKey)
        return rawList.map { Subtask(id = UUID.randomUUID().toString(), title = it) }
    }

    private fun callGeminiApiForArray(promptText: String, apiKey: String): List<String> {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        val jsonPayload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptText)
                        })
                    })
                })
            })
        }

        conn.outputStream.use { os ->
            os.write(jsonPayload.toString().toByteArray())
        }

        if (conn.responseCode == 200) {
            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val jsonResp = JSONObject(responseText)
            val candidates = jsonResp.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val rawText = parts.getJSONObject(0).optString("text", "")
                    val jsonStart = rawText.indexOf("[")
                    val jsonEnd = rawText.lastIndexOf("]")
                    if (jsonStart != -1 && jsonEnd != -1) {
                        val arrayString = rawText.substring(jsonStart, jsonEnd + 1)
                        val jsonArray = JSONArray(arrayString)
                        val result = mutableListOf<String>()
                        for (i in 0 until jsonArray.length()) {
                            result.add(jsonArray.getString(i))
                        }
                        return result
                    }
                }
            }
        }
        return emptyList()
    }

    private fun generateLocalSubtasks(title: String): List<Subtask> {
        val lower = title.lowercase()
        return when {
            lower.contains("code") || lower.contains("app") || lower.contains("bug") -> listOf(
                Subtask(UUID.randomUUID().toString(), "Analyze requirements & setup environment"),
                Subtask(UUID.randomUUID().toString(), "Implement core logic & UI components"),
                Subtask(UUID.randomUUID().toString(), "Run unit tests and verify functionality"),
                Subtask(UUID.randomUUID().toString(), "Review code & commit changes")
            )
            lower.contains("design") || lower.contains("ui") -> listOf(
                Subtask(UUID.randomUUID().toString(), "Draft rough wireframe concepts"),
                Subtask(UUID.randomUUID().toString(), "Select color palette & typography"),
                Subtask(UUID.randomUUID().toString(), "Build high-fidelity interactive prototype"),
                Subtask(UUID.randomUUID().toString(), "Gather visual feedback and polish")
            )
            lower.contains("study") || lower.contains("assignment") || lower.contains("read") || lower.contains("exam") -> listOf(
                Subtask(UUID.randomUUID().toString(), "Gather syllabus topics and outline key chapters"),
                Subtask(UUID.randomUUID().toString(), "Complete deep focus reading block (25m)"),
                Subtask(UUID.randomUUID().toString(), "Summarize formulas & key takeaways"),
                Subtask(UUID.randomUUID().toString(), "Review notes and practice sample questions")
            )
            else -> listOf(
                Subtask(UUID.randomUUID().toString(), "Define goals & gather initial resources"),
                Subtask(UUID.randomUUID().toString(), "Execute primary action phase"),
                Subtask(UUID.randomUUID().toString(), "Review quality and mark completion")
            )
        }
    }
}
