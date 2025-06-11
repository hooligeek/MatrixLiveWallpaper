package com.example.hooligeek_matrix_live_wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import java.util.Random

// These data classes and enum are tightly coupled with the rendering logic,
// so it makes sense to put them here or in a dedicated 'model' package.
data class MatrixCharacter(
    var y: Float,
    var char: String,
    var fadeProgress: Float = 0f,
    val fadeRate: Float,
    var ticksUntilChange: Int,
    val fallSpeed: Float,
    var hasLeadingGlow: Boolean = false,
    val maxGlowRadius: Float = 0f
)

enum class ColumnState {
    ACTIVE,
    FADING_OUT
}

data class MatrixColumn(
    val characters: MutableList<MatrixCharacter>,
    var columnAlpha: Int = 255,
    var state: ColumnState = ColumnState.ACTIVE,
    var columnFadeSpeed: Int = 10
)

class MatrixCanvasRenderer(private val context: Context) {

    // Constants for rendering setup - moved from Engine, now members of renderer
    private val textSize = 60f
    private val columnWidth = textSize * 0.9f
    private val minFallSpeed = textSize / 20f // Now a class member
    private val randomSpeedAdditionFactor = textSize * 0.375f // Now a class member

    private lateinit var customFontTypeface: Typeface

    // Reusable Paint objects - initialized once
    private val greenPaint = Paint().apply {
        color = Color.GREEN
        textSize = this@MatrixCanvasRenderer.textSize
        // Typeface will be set in initialize() method below
    }
    private val blackPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        alpha = 255
    }

    private val columns: MutableList<MatrixColumn> = mutableListOf()
    private val random = Random()

    private lateinit var allMatrixChars: String // To be initialized once in initialize()

    private var surfaceWidth: Int = 0
    private var surfaceHeight: Int = 0

    // This method initializes the renderer with surface dimensions and loads resources
    fun initialize(width: Int, height: Int) {
        this.surfaceWidth = width
        this.surfaceHeight = height

        // Load font once
        customFontTypeface = ResourcesCompat.getFont(context, R.font.matrix_code)!!
        greenPaint.typeface = customFontTypeface

        // Build allMatrixChars string once (optimization)
        val sb = StringBuilder()
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
        allMatrixChars = sb.toString()

        // Initial column setup - moved from onSurfaceCreated
        val numberOfColumns = (surfaceWidth / columnWidth).toInt() + 1
        columns.clear()
        for (i in 0 until numberOfColumns) {
            val startY = random.nextInt(surfaceHeight).toFloat() - (textSize * random.nextInt(5))

            val initialChars = mutableListOf<MatrixCharacter>()
            val initialColumnCharCount = random.nextInt(10) + 5
            var currentY = startY
            for (j in 0 until initialColumnCharCount) {
                val initialGlow = random.nextFloat() < 0.15f
                val assignedMaxGlowRadius = if (initialGlow) {
                    (random.nextFloat() * (textSize * 0.4f)) + (textSize * 0.1f)
                } else 0f

                initialChars.add(MatrixCharacter(
                    y = currentY,
                    char = getRandomMatrixChar(),
                    fadeProgress = 0f,
                    fadeRate = (random.nextFloat() * 0.01f) + 0.001f,
                    ticksUntilChange = random.nextInt(10) + 5,
                    fallSpeed = minFallSpeed + (random.nextFloat() * randomSpeedAdditionFactor),
                    hasLeadingGlow = initialGlow,
                    maxGlowRadius = assignedMaxGlowRadius
                ))
                currentY -= textSize
            }
            columns.add(MatrixColumn(
                characters = initialChars,
                columnAlpha = 255,
                state = ColumnState.ACTIVE,
                columnFadeSpeed = random.nextInt(5) + 2
            ))
        }
    }

    // This method updates the state of all characters and columns for the next frame
    fun update() {
        for (i in columns.indices) {
            val matrixColumn = columns[i]
            val charactersInColumn = matrixColumn.characters

            when (matrixColumn.state) {
                ColumnState.ACTIVE -> {
                    // Remove off-screen characters
                    charactersInColumn.removeAll { it.y > surfaceHeight + textSize }

                    val topCharY = charactersInColumn.firstOrNull()?.y
                    // Add new character to the top of the column
                    if (topCharY == null || topCharY < -textSize * 0.5f || random.nextFloat() < 0.2f) {
                        val initialGlow = random.nextFloat() < 0.15f
                        val assignedMaxGlowRadius = if (initialGlow) {
                            (random.nextFloat() * (textSize * 0.4f)) + (textSize * 0.1f)
                        } else 0f

                        charactersInColumn.add(0, MatrixCharacter(
                            y = (topCharY ?: -textSize) - (textSize * (0.5f + random.nextFloat())),
                            char = getRandomMatrixChar(),
                            fadeProgress = 0f,
                            fadeRate = (random.nextFloat() * 0.01f) + 0.001f,
                            ticksUntilChange = random.nextInt(10) + 5,
                            fallSpeed = minFallSpeed + (random.nextFloat() * randomSpeedAdditionFactor),
                            hasLeadingGlow = initialGlow,
                            maxGlowRadius = assignedMaxGlowRadius
                        ))
                    }

                    // Chance for a column to start fading out
                    if (random.nextFloat() < 0.002f) {
                        matrixColumn.state = ColumnState.FADING_OUT
                        matrixColumn.columnAlpha = 255
                        matrixColumn.columnFadeSpeed = random.nextInt(5) + 2
                    }
                }
                ColumnState.FADING_OUT -> {
                    matrixColumn.columnAlpha -= matrixColumn.columnFadeSpeed
                    if (matrixColumn.columnAlpha <= 0) {
                        // Reset column when fully faded out
                        charactersInColumn.clear()
                        val initialColumnCharCount = random.nextInt(10) + 5
                        var currentY = -random.nextInt(surfaceHeight / 2).toFloat()
                        for (k in 0 until initialColumnCharCount) {
                            val initialGlow = random.nextFloat() < 0.15f
                            val assignedMaxGlowRadius = if (initialGlow) {
                                (random.nextFloat() * (textSize * 0.4f)) + (textSize * 0.1f)
                            } else 0f

                            charactersInColumn.add(MatrixCharacter(
                                y = currentY,
                                char = getRandomMatrixChar(),
                                fadeProgress = 0f,
                                fadeRate = (random.nextFloat() * 0.01f) + 0.001f,
                                ticksUntilChange = random.nextInt(10) + 5,
                                fallSpeed = minFallSpeed + (random.nextFloat() * randomSpeedAdditionFactor),
                                hasLeadingGlow = initialGlow,
                                maxGlowRadius = assignedMaxGlowRadius
                            ))
                            currentY -= textSize
                        }
                        matrixColumn.columnAlpha = 255
                        matrixColumn.state = ColumnState.ACTIVE
                        matrixColumn.columnFadeSpeed = random.nextInt(5) + 2
                    }
                }
            }

            // Update each character's properties (fade, change char, fall)
            for (j in charactersInColumn.indices) {
                val charObj = charactersInColumn[j]

                charObj.ticksUntilChange--
                if (charObj.ticksUntilChange <= 0) {
                    charObj.char = getRandomMatrixChar()
                    charObj.ticksUntilChange = random.nextInt(10) + 5
                }

                charObj.fadeProgress += charObj.fadeRate

                // Ensure a character doesn't fade faster than the one below it
                if (j < charactersInColumn.size - 1) {
                    val charBelow = charactersInColumn[j + 1]
                    charObj.fadeProgress = charObj.fadeProgress.coerceAtMost(charBelow.fadeProgress + 0.0025f)
                }
                charObj.fadeProgress = charObj.fadeProgress.coerceIn(0f, 1f) // Clamp between 0 and 1

                charObj.y += charObj.fallSpeed // Move character down
            }
        }
    }

    // This method draws all characters on the provided canvas
    fun draw(canvas: Canvas) {
        // Clear background with black
        canvas.drawRect(0f, 0f, surfaceWidth.toFloat(), surfaceHeight.toFloat(), blackPaint)

        // Draw each column and its characters
        for (i in columns.indices) {
            val x = i * columnWidth
            val matrixColumn = columns[i]
            val charactersInColumn = matrixColumn.characters

            for (j in charactersInColumn.indices) {
                val charObj = charactersInColumn[j]

                // Calculate individual character alpha based on fadeProgress
                val individualCharAlpha = (255 * (1f - charObj.fadeProgress)).toInt().coerceIn(0, 255)
                // Combine with column alpha
                val combinedAlpha = (individualCharAlpha * (matrixColumn.columnAlpha / 255f)).toInt().coerceIn(0, 255)

                greenPaint.alpha = combinedAlpha // Apply combined alpha to the shared paint object
                greenPaint.clearShadowLayer()    // Clear any previous shadow before potentially adding a new one

                val glowFadeThreshold = 0.7f
                // Apply glow effect if conditions met
                if (charObj.hasLeadingGlow && charObj.fadeProgress < glowFadeThreshold) {
                    val glowProgressFactor = 1f - (charObj.fadeProgress / glowFadeThreshold).coerceIn(0f, 1f)
                    val currentGlowRadius = charObj.maxGlowRadius * glowProgressFactor

                    if (currentGlowRadius > 0f) {
                        greenPaint.setShadowLayer(
                            currentGlowRadius,
                            0f,
                            0f,
                            Color.GREEN // Shadow color is also green
                        )
                    }
                }
                // Draw the character using the now configured greenPaint
                canvas.drawText(charObj.char, x, charObj.y, greenPaint)
            }
        }
    }

    // Helper to get a random Matrix character from the pre-built set
    private fun getRandomMatrixChar(): String {
        return allMatrixChars[random.nextInt(allMatrixChars.length)].toString()
    }
}