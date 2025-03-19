package com.hotfloppy.autismtalk

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridLayout = findViewById(R.id.gridLayoutCustom)
        val fabAddCustom = findViewById<FloatingActionButton>(R.id.fab_add_custom)

        fabAddCustom.setOnLongClickListener {
            val intent = Intent(this, AddCustomButtonActivity::class.java)
            startActivity(intent)
            true
        }

        mediaPlayer = MediaPlayer()

        loadDefaultButtons()
        loadCustomButtons()
    }

    private fun loadDefaultButtons() {
        addDefaultButton("Food", R.drawable.ic_food, R.raw.food_sound)
        addDefaultButton("Toilet", R.drawable.ic_toilet, R.raw.toilet_sound)
        addDefaultButton("Sleep", R.drawable.ic_sleep, R.raw.sleep_sound)
        addDefaultButton("Bath", R.drawable.ic_bath, R.raw.bath_sound)
    }

    private fun addDefaultButton(label: String, imageRes: Int, soundRes: Int) {
        val buttonLayout = createButtonLayout(label, imageRes)
        buttonLayout.setOnClickListener {
            playSoundFromRes(soundRes)
        }
        gridLayout.addView(buttonLayout)
    }

    private fun loadCustomButtons() {
        val prefs = getSharedPreferences("custom_buttons", MODE_PRIVATE)
        val buttonKeys = prefs.getStringSet("button_keys", setOf()) ?: setOf()

        for (key in buttonKeys) {
            val imagePath = prefs.getString("${key}_image", "default_image") ?: "default_image"
            val soundPath = prefs.getString("${key}_sound", "default_sound") ?: "default_sound"

            val buttonLayout = if (imagePath == "default_image") {
                createButtonLayout(key, R.drawable.ic_default)
            } else {
                val imageBitmap = BitmapFactory.decodeFile(imagePath)
                createButtonLayout(key, imageBitmap)
            }

            buttonLayout.setOnClickListener {
                if (soundPath == "default_sound") {
                    playSoundFromRes(R.raw.default_sound)
                } else {
                    playSoundFromFile(soundPath)
                }
            }

            buttonLayout.setOnLongClickListener {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Remove Button")
                    .setMessage("Do you want to remove \"$key\"?")
                    .setPositiveButton("Yes") { _, _ ->
                        removeCustomButton(key)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }


            gridLayout.addView(buttonLayout)
        }
    }

    private fun createButtonLayout(label: String, imageRes: Int): LinearLayout {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER

        val params = GridLayout.LayoutParams().apply {
            width = dpToPx(160)
            height = dpToPx(160)
            setMargins(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }
        layout.layoutParams = params
        layout.setBackgroundResource(R.drawable.button_background)
        layout.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))

        val imageView = ImageView(this)
        imageView.setImageResource(imageRes)
        imageView.layoutParams = LinearLayout.LayoutParams(dpToPx(100), dpToPx(100))

        val textView = TextView(this)
        textView.text = label
        textView.setTextColor(resources.getColor(android.R.color.white, theme))
        textView.textSize = 20f

        layout.addView(imageView)
        layout.addView(textView)
        return layout
    }

    private fun createButtonLayout(label: String, imageBitmap: android.graphics.Bitmap): LinearLayout {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER

        val params = GridLayout.LayoutParams().apply {
            width = dpToPx(160)
            height = dpToPx(160)
            setMargins(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }
        layout.layoutParams = params
        layout.setBackgroundResource(R.drawable.button_background)
        layout.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))

        val imageView = ImageView(this)
        imageView.setImageBitmap(imageBitmap)
        imageView.layoutParams = LinearLayout.LayoutParams(dpToPx(100), dpToPx(100))

        val textView = TextView(this)
        textView.text = label
        textView.setTextColor(resources.getColor(android.R.color.white, theme))
        textView.textSize = 20f

        layout.addView(imageView)
        layout.addView(textView)
        return layout
    }

    private fun removeCustomButton(key: String) {
        val prefs = getSharedPreferences("custom_buttons", MODE_PRIVATE)
        val editor = prefs.edit()
        val buttonKeys = prefs.getStringSet("button_keys", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        buttonKeys.remove(key)
        editor.putStringSet("button_keys", buttonKeys)
        editor.remove("${key}_image")
        editor.remove("${key}_sound")
        editor.apply()

        // Refresh grid
        gridLayout.removeAllViews()
        loadDefaultButtons()
        loadCustomButtons()
    }


    private fun playSoundFromRes(soundResId: Int) {
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer.create(this, soundResId)
        mediaPlayer.start()
    }

    private fun playSoundFromFile(filePath: String) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(filePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            Toast.makeText(this, "Error playing custom sound", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        gridLayout.removeAllViews()
        loadDefaultButtons()
        loadCustomButtons()
    }

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
