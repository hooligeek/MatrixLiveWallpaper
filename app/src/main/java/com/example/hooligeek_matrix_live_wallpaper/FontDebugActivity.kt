package com.example.hooligeek_matrix_live_wallpaper

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import java.lang.StringBuilder
import java.util.Random

class FontDebugActivity : AppCompatActivity() {

    private lateinit var fontDisplayTextView: TextView
    private lateinit var customFontTypeface: Typeface

    // Replicate getRandomMatrixChar from your WallpaperService to ensure consistency
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
        sb.append('\u00A9') // ©
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_font_debug)

        fontDisplayTextView = findViewById(R.id.font_display_text_view)

        try {
            customFontTypeface = ResourcesCompat.getFont(this, R.font.matrix_code)!!
            fontDisplayTextView.typeface = customFontTypeface

            val allChars = getRandomMatrixCharSet()
            val displayString = StringBuilder()

            // Display characters in a grid-like format for easier inspection
            var charCount = 0
            for (char in allChars) {
                displayString.append(char)
                charCount++
                if (charCount % 20 == 0) { // Add a newline every 20 characters
                    displayString.append("\n")
                } else {
                    displayString.append(" ") // Add space between characters
                }
            }

            fontDisplayTextView.text = displayString.toString()

            Log.d("FontDebugActivity", "Font loaded and applied to TextView.")
            Log.d("FontDebugActivity", "Displayed chars: ${allChars.length}")

        } catch (e: Exception) {
            fontDisplayTextView.text = "Error loading font: ${e.message}"
            Log.e("FontDebugActivity", "Error loading font:", e)
        }
    }
}