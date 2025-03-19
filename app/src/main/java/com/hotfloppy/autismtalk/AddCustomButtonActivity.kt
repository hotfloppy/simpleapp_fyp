package com.hotfloppy.autismtalk

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class AddCustomButtonActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private var soundUri: Uri? = null
    private lateinit var imageView: ImageView
    private lateinit var labelEditText: EditText

    private val PICK_IMAGE_REQUEST = 1
    private val PICK_SOUND_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_custom_button)

        imageView = findViewById(R.id.imageViewPreview)
        labelEditText = findViewById(R.id.editTextLabel)

        val selectImageButton = findViewById<Button>(R.id.buttonSelectImage)
        val selectSoundButton = findViewById<Button>(R.id.buttonSelectSound)
        val saveButton = findViewById<Button>(R.id.buttonSave)

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        selectSoundButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            startActivityForResult(intent, PICK_SOUND_REQUEST)
        }

        saveButton.setOnClickListener {
            saveCustomButton()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    imageUri = data.data
                    imageView.setImageURI(imageUri)
                }
                PICK_SOUND_REQUEST -> {
                    soundUri = data.data
                    Toast.makeText(this, "Sound selected!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun copyUriToFile(uri: Uri, fileName: String): File {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(filesDir, fileName)
        val outputStream = file.outputStream()

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        return file
    }

    private fun saveCustomButton() {
        val prefs = getSharedPreferences("custom_buttons", MODE_PRIVATE)
        val editor = prefs.edit()

        var label = labelEditText.text.toString().ifEmpty {
            // Auto-generate random label if empty
            val count = prefs.getStringSet("button_keys", setOf())?.size ?: 0
            "Label%02d".format(count + 1)
        }

        // Save image
        val imageFilePath = if (imageUri != null) {
            val imageFile = copyUriToFile(imageUri!!, "image_${System.currentTimeMillis()}.jpg")
            imageFile.absolutePath
        } else {
            // Use default image stored in drawable - reference by resource
            // We will save a special string indicating default
            "default_image"
        }

        // Save sound
        val soundFilePath = if (soundUri != null) {
            val soundFile = copyUriToFile(soundUri!!, "sound_${System.currentTimeMillis()}.mp3")
            soundFile.absolutePath
        } else {
            "default_sound"
        }

        val currentKeys = prefs.getStringSet("button_keys", mutableSetOf())!!.toMutableSet()
        currentKeys.add(label)

        editor.putStringSet("button_keys", currentKeys)
        editor.putString("${label}_image", imageFilePath)
        editor.putString("${label}_sound", soundFilePath)
        editor.apply()

        Toast.makeText(this, "Custom button saved!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
