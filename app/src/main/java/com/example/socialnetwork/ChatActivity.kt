package com.example.socialnetwork

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : Activity() {
    private lateinit var storage: LocalStorage
    private lateinit var chatId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatId = intent.getStringExtra("chat_id").orEmpty()
        if (chatId != DemoData.SAVED_CHAT && DemoData.person(chatId) == null) {
            Toast.makeText(this, R.string.missing_person, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContentView(R.layout.activity_chat)
        storage = LocalStorage(this)
        val isSaved = chatId == DemoData.SAVED_CHAT
        findViewById<TextView>(R.id.chatTitle).text =
            if (isSaved) getString(R.string.saved_messages) else DemoData.person(chatId)!!.name
        findViewById<TextView>(R.id.chatHint).setText(
            if (isSaved) R.string.saved_chat_hint else R.string.local_chat_hint)
        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }
        findViewById<Button>(R.id.sendButton).setOnClickListener {
            val input = findViewById<EditText>(R.id.messageInput)
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                storage.sendMessage(chatId, text)
                input.text.clear()
                showMessages()
            }
        }
        showMessages()
    }

    private fun showMessages() {
        val container = findViewById<LinearLayout>(R.id.messagesContainer)
        val messages = storage.getMessages(chatId)
        container.removeAllViews()
        if (messages.isEmpty()) {
            container.addView(TextView(this).apply {
                setText(R.string.empty_chat)
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(89, 102, 116))
                setPadding(dp(12), dp(24), dp(12), dp(24))
            })
        }
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        for (message in messages) {
            val row = LinearLayout(this).apply {
                gravity = if (message.outgoing) Gravity.END else Gravity.START
            }
            val bubble = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(8), dp(12), dp(8))
                setBackgroundResource(if (message.outgoing) R.drawable.message_outgoing else R.drawable.message_incoming)
            }
            bubble.addView(TextView(this).apply {
                text = message.text
                textSize = 16f
                setTextColor(Color.rgb(37, 52, 65))
                maxWidth = (resources.displayMetrics.widthPixels * 0.72f).toInt()
                setTextIsSelectable(true)
            })
            bubble.addView(TextView(this).apply {
                text = timeFormat.format(Date(message.timestamp))
                textSize = 11f
                gravity = Gravity.END
                setTextColor(Color.rgb(89, 102, 116))
                setPadding(0, dp(4), 0, 0)
            })
            row.addView(bubble)
            container.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
        }
        val scroll = findViewById<ScrollView>(R.id.messagesScroll)
        scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
