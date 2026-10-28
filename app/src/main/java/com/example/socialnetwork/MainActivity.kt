package com.example.socialnetwork

import android.app.Activity
import android.content.Intent
import android.content.ActivityNotFoundException
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper

class MainActivity : Activity() {
    private lateinit var storage: LocalStorage
    private lateinit var pages: ViewFlipper
    private lateinit var navigation: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        storage = LocalStorage(this)
        pages = findViewById(R.id.pages)
        navigation = listOf(R.id.feedButton, R.id.friendsButton, R.id.messagesButton, R.id.profileButton)
            .map { findViewById<Button>(it) }
        navigation.forEachIndexed { index, button -> button.setOnClickListener { showPage(index) } }

        showFeed()
        fillProfile()
        val search = findViewById<EditText>(R.id.friendSearch)
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                showPeople()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        findViewById<Button>(R.id.saveProfile).setOnClickListener { saveProfile() }
        findViewById<Button>(R.id.chooseAvatar).setOnClickListener { chooseAvatar() }
        showPage(savedInstanceState?.getInt("page", 0) ?: 0)
    }

    override fun onResume() {
        super.onResume()
        // После возвращения из чата обновляем последнюю реплику в списке диалогов.
        showChats()
        showProfileAvatar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("page", pages.displayedChild)
        super.onSaveInstanceState(outState)
    }

    private fun showPage(index: Int) {
        pages.displayedChild = index.coerceIn(0, 3)
        navigation.forEachIndexed { position, button ->
            button.setBackgroundColor(if (position == pages.displayedChild) Color.rgb(223, 234, 244) else Color.WHITE)
            button.setTextColor(Color.rgb(37, 52, 65))
        }
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(pages.windowToken, 0)
        if (index == 1) showPeople()
        if (index == 2) showChats()
        if (index == 3) {
            updateProfileCount()
            showProfileAvatar()
        }
    }

    private fun showFeed() {
        val container = findViewById<LinearLayout>(R.id.feedContainer)
        container.removeAllViews()
        for (post in DemoData.posts) {
            val card = card()
            card.addView(label(post.author, 17f, true))
            card.addView(label(post.time, 12f))
            val image = ImageView(this).apply {
                setImageResource(post.picture)
                contentDescription = getString(R.string.picture_description, post.author)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            card.addView(image, LinearLayout.LayoutParams(-1, dp(170)).apply {
                topMargin = dp(12)
                bottomMargin = dp(12)
            })
            card.addView(label(post.text, 16f))
            container.addView(card)
        }
    }

    private fun showPeople() {
        val container = findViewById<LinearLayout>(R.id.friendsContainer)
        val query = findViewById<EditText>(R.id.friendSearch).text.toString().trim()
        val friends = storage.getFriends()
        val people = DemoData.people.filter {
            it.name.contains(query, true) || it.city.contains(query, true)
        }
        findViewById<TextView>(R.id.friendsCount).text =
            getString(R.string.friends_count, friends.size, people.size)
        container.removeAllViews()
        if (people.isEmpty()) container.addView(label(getString(R.string.no_people), 16f))
        for (person in people) {
            val isFriend = person.id in friends
            val card = card()
            val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
            header.addView(AvatarViews.person(this, person))
            val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            info.addView(label(person.name, 17f, true))
            info.addView(label(person.city, 14f))
            if (isFriend) info.addView(label(getString(R.string.friend_status), 12f))
            header.addView(info, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(12) })
            card.addView(header)
            val actions = LinearLayout(this)
            val add = Button(this).apply {
                setText(if (isFriend) R.string.remove_friend else R.string.add_friend)
                isAllCaps = false
                textSize = 14f
                setOnClickListener {
                    storage.toggleFriend(person.id)
                    showPeople()
                    updateProfileCount()
                }
            }
            val write = Button(this).apply {
                setText(R.string.write_message)
                isAllCaps = false
                textSize = 14f
                setOnClickListener { openChat(person.id) }
            }
            actions.addView(add, LinearLayout.LayoutParams(0, -2, 1f))
            actions.addView(write, LinearLayout.LayoutParams(0, -2, 1f))
            card.addView(actions)
            container.addView(card)
        }
    }

    private fun showChats() {
        val container = findViewById<LinearLayout>(R.id.chatsContainer)
        container.removeAllViews()
        val chatIds = listOf(DemoData.SAVED_CHAT) + DemoData.people.map { it.id }
        for (id in chatIds) {
            val name = if (id == DemoData.SAVED_CHAT) getString(R.string.saved_messages) else DemoData.person(id)!!.name
            val last = storage.getMessages(id).lastOrNull()
            val preview = when {
                last == null -> getString(if (id == DemoData.SAVED_CHAT) R.string.saved_hint else R.string.no_messages)
                last.outgoing -> getString(R.string.you_message, last.text)
                else -> last.text
            }
            val row = card()
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.addView(if (id == DemoData.SAVED_CHAT) avatar("Я")
                else AvatarViews.person(this, DemoData.person(id)!!))
            val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            info.addView(label(name, 17f, true))
            info.addView(label(preview, 14f).apply {
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
            })
            row.addView(info, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(12) })
            row.isFocusable = true
            row.setOnClickListener { openChat(id) }
            container.addView(row)
        }
    }

    private fun openChat(id: String) {
        startActivity(Intent(this, ChatActivity::class.java).putExtra("chat_id", id))
    }

    private fun fillProfile() {
        val profile = storage.getProfile()
        findViewById<EditText>(R.id.profileName).setText(profile.name)
        findViewById<EditText>(R.id.profileCity).setText(profile.city)
        findViewById<EditText>(R.id.profileAbout).setText(profile.about)
        findViewById<TextView>(R.id.profileAvatar).text = DemoData.initials(profile.name)
        updateProfileCount()
    }

    private fun saveProfile() {
        val input = findViewById<EditText>(R.id.profileName)
        val name = input.text.toString().trim()
        if (name.isEmpty()) {
            input.error = getString(R.string.name_required)
            input.requestFocus()
            return
        }
        storage.saveProfile(Profile(name,
            findViewById<EditText>(R.id.profileCity).text.toString().trim(),
            findViewById<EditText>(R.id.profileAbout).text.toString().trim()))
        findViewById<TextView>(R.id.profileAvatar).text = DemoData.initials(name)
        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show()
    }

    private fun chooseAvatar() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivityForResult(intent, 101)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.picker_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 101 || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        val button = findViewById<Button>(R.id.chooseAvatar)
        button.isEnabled = false
        button.setText(R.string.photo_loading)
        val appContext = applicationContext
        // Копирование и уменьшение фото не блокируют интерфейс.
        Thread {
            val saved = try {
                AvatarStorage(appContext).save(uri)
                true
            } catch (_: Exception) {
                false
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                button.isEnabled = true
                button.setText(R.string.choose_photo)
                if (saved) showProfileAvatar()
                Toast.makeText(this, if (saved) R.string.photo_saved else R.string.photo_error,
                    Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun showProfileAvatar() {
        val bitmap = AvatarStorage(applicationContext).load()
        findViewById<ImageView>(R.id.profileAvatarPhoto).apply {
            setImageBitmap(bitmap)
            visibility = if (bitmap != null) View.VISIBLE else View.GONE
        }
        findViewById<TextView>(R.id.profileAvatar).visibility =
            if (bitmap == null) View.VISIBLE else View.GONE
    }

    private fun updateProfileCount() {
        findViewById<TextView>(R.id.profileFriendsCount).text =
            getString(R.string.profile_friends_count, storage.getFriends().size)
    }

    private fun label(value: String, size: Float, bold: Boolean = false) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(Color.rgb(37, 52, 65))
        setPadding(0, dp(2), 0, dp(2))
        if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
    }

    private fun avatar(value: String) = label(value, 19f, true).apply {
        gravity = Gravity.CENTER
        setBackgroundResource(R.drawable.avatar_background)
        layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundResource(R.drawable.card_background)
        setPadding(dp(12), dp(12), dp(12), dp(12))
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
