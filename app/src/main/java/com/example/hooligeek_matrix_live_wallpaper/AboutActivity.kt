package com.example.hooligeek_matrix_live_wallpaper

import android.content.Intent // Import for Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button // Import for Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Find the TextViews
        val aboutTitle: TextView = findViewById(R.id.about_title)
        val aboutContent: TextView = findViewById(R.id.about_content)
        val aboutCharSetDisplay: TextView = findViewById(R.id.about_char_set_display)

        // Set the text for title and main content.
        aboutTitle.text = "Matrix Live Wallpaper"
        aboutContent.text = "Version 1.0\n\n" +
                "Developed with AI by Hooligeek.\n\n" +
                "This live wallpaper brings the iconic digital rain from The Matrix to your device.\n\n" +
                "Enjoy the immersive experience!"

        // Load the custom font from assets
        val customTypeface: Typeface? = try {
            Typeface.createFromAsset(assets, "matrix_code.ttf")
        } catch (e: Exception) {
            e.printStackTrace() // Print the error to Logcat for debugging
            // Display an error message on the screen if the font cannot be loaded
            aboutCharSetDisplay.text = "Error: Matrix font could not be loaded. Please ensure 'matrix_code.ttf' is in your 'assets' folder."
            null // Return null if font loading fails
        }

        // Apply the custom font ONLY to the character set display TextView
        customTypeface?.let {
            aboutCharSetDisplay.typeface = it
            // Populate this TextView with the actual character set using the custom font
            aboutCharSetDisplay.text = getRandomMatrixCharSet()
        } ?: run {
            // If font loading failed (customTypeface is null), this block will execute.
            // The TextView will remain with its default font, showing the error message set above.
        }

        // --- NEW CODE STARTS HERE ---
        // Find the new demo button by its ID
        val showDemoButton: Button = findViewById(R.id.btn_show_demo)

        // Set an OnClickListener for the button
        showDemoButton.setOnClickListener {
            // Create an Intent to start the WallpaperDemoActivity
            val intent = Intent(this, WallpaperDemoActivity::class.java)
            startActivity(intent) // Launch the demo activity
        }
        // --- NEW CODE ENDS HERE ---
    }

    // Your verified getRandomMatrixCharSet() method
    private fun getRandomMatrixCharSet(): String {
        val sb = StringBuilder()
        // Matrix
        sb.append('\u0020') // space
        sb.append('\u0022') // "
        sb.append('\u002A') // *
        sb.append('\u002B') // +
        sb.append('\u003A') // :
        sb.append('\u003C') // <
        sb.append('\uA78A') // =
        sb.append('\u003E') // >
        // sb.append('\u00A9') // ©
        sb.append('\u0030') // 0
        sb.append('\u0031') // 1
        sb.append('\u0032') // 2
        sb.append('\u0033') // 3
        sb.append('\u0034') // 4
        sb.append('\u0035') // 5
        sb.append('\u0037') // 7
        sb.append('\u0038') // 8
        sb.append('\u0039') // 9
        sb.append('\u007A') // Z
        sb.append('\u007C') // |
        sb.append('\u00A6') // ¦
        sb.append('\u254C') // ╌
        sb.append('\u25AA') // ▪
        sb.append('\u30A2') // Katakana A
        sb.append('\u30A6') // Katakana U
        sb.append('\u30AA') // Katakana O
        sb.append('\u30BB') // Katakana Se
        sb.append('\u30CA') // Katakana Na
        sb.append('\u30DB') // Katakana Ho
        sb.append('\u30E0') // Katakana Mu
        sb.append('\u30E1') // Katakana Me
        sb.append('\u30E2') // Katakana Mo
        sb.append('\u30E4') // Katakana Ya
        sb.append('\u30EF') // Katakana Wa
        sb.append('\u30B7') // Katakana Si
        sb.append('\u30A8') // Katakana E
        sb.append('\u30AB') // Katakana Ka
        sb.append('\u30AD') // Katakana Ki
        sb.append('\u30B1') // Katakana Ke
        sb.append('\u30B3') // Katakana Ko
        sb.append('\u30B5') // Katakana Sa
        sb.append('\u30B9') // Katakana Su
        sb.append('\u30BD') // Katakana So
        sb.append('\u30BF') // Katakana Ta
        sb.append('\u30C4') // Katakana Tu
        sb.append('\u30C6') // Katakana Te
        sb.append('\u30CB') // Katakana Ni
        sb.append('\u30CC') // Katakana Nu
        sb.append('\u30CD') // Katakana Ne
        sb.append('\u30CF') // Katakana Ha
        sb.append('\u30D2') // Katakana Hi
        sb.append('\u30DE') // Katakana Ma
        sb.append('\u30DF') // Katakana Mi
        sb.append('\u30E8') // Katakana Yo
        sb.append('\u30E9') // Katakana Ra
        sb.append('\u30EA') // Katakana Ri
        sb.append('\u30FC') // Katakana prolonged sound mark
        sb.append('\uE937') // bold equals sign

        return sb.toString()
    }
}