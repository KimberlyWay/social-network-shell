package com.example.socialnetwork

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Небольшое локальное хранилище. Это SharedPreferences, а не база данных.
class LocalStorage(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("social_shell", Context.MODE_PRIVATE)

    fun getProfile() = Profile(
        preferences.getString("name", "Алексей Иванов").orEmpty(),
        preferences.getString("city", "Самара").orEmpty(),
        preferences.getString("about", "Учусь программированию и делаю своё первое приложение.").orEmpty()
    )

    fun saveProfile(profile: Profile) {
        preferences.edit()
            .putString("name", profile.name)
            .putString("city", profile.city)
            .putString("about", profile.about)
            .apply()
    }

    fun getFriends(): Set<String> = preferences
        .getStringSet("friends", setOf("anna", "maxim"))
        .orEmpty().toSet()

    fun toggleFriend(id: String) {
        if (DemoData.person(id) == null) return
        val friends = getFriends().toMutableSet()
        if (!friends.add(id)) friends.remove(id)
        preferences.edit().putStringSet("friends", friends).apply()
    }

    fun getMessages(chatId: String): List<Message> {
        val stored = preferences.getString("messages_$chatId", null)
        if (stored == null) return demoMessages(chatId)
        val array = JSONArray(stored)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            Message(item.getString("text"), item.getBoolean("outgoing"), item.getLong("timestamp"))
        }
    }

    fun sendMessage(chatId: String, text: String) {
        val value = text.trim()
        if (value.isEmpty()) return
        val messages = getMessages(chatId) + Message(value.take(2000), true, System.currentTimeMillis())
        val array = JSONArray()
        for (message in messages) {
            array.put(JSONObject().apply {
                put("text", message.text)
                put("outgoing", message.outgoing)
                put("timestamp", message.timestamp)
            })
        }
        preferences.edit().putString("messages_$chatId", array.toString()).apply()
    }

    private fun demoMessages(chatId: String): List<Message> {
        val now = System.currentTimeMillis()
        return when (chatId) {
            "anna" -> listOf(
                Message("Привет! Это пример входящего сообщения.", false, now - 3600000),
                Message("Привет! Проверяю наш учебный чат.", true, now - 3540000),
                Message("Ниже можно написать своё сообщение. Оно останется только на твоём телефоне.", false, now - 3480000)
            )
            "maxim" -> listOf(
                Message("Привет! Как продвигается проект?", false, now - 7200000)
            )
            else -> emptyList()
        }
    }
}
