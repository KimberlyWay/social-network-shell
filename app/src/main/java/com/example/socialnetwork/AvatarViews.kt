package com.example.socialnetwork

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

object AvatarViews {
    fun person(context: Context, person: Person): View {
        val view = if (person.avatar != 0) {
            ImageView(context).apply {
                setImageResource(person.avatar)
                scaleType = ImageView.ScaleType.CENTER_CROP
                clipToOutline = true
            }
        } else {
            TextView(context).apply {
                text = DemoData.initials(person.name)
                textSize = 19f
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(37, 52, 65))
                setTypeface(null, Typeface.BOLD)
            }
        }
        view.setBackgroundResource(R.drawable.avatar_background)
        val size = (48 * context.resources.displayMetrics.density).toInt()
        view.layoutParams = LinearLayout.LayoutParams(size, size)
        view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        return view
    }
}
