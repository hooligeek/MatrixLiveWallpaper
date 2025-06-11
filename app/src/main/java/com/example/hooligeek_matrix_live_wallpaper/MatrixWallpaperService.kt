package com.example.hooligeek_matrix_live_wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.content.res.ResourcesCompat
import java.util.Random
import android.util.Log

class MatrixWallpaperService : WallpaperService() {

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

    override fun onCreateEngine(): Engine {
        return MatrixWallpaperEngine()
    }

    inner class MatrixWallpaperEngine : WallpaperService.Engine() {
        private val frameRate = 1000L / 60
        private val handler = Handler(Looper.getMainLooper())
        private val textSize = 60f
        private val columnWidth = textSize * 0.9f

        private lateinit var customFontTypeface: Typeface

        private val greenPaint = Paint().apply {
            color = Color.GREEN
            textSize = this@MatrixWallpaperEngine.textSize
            // fakeBoldText = true // Commented out as per previous discussion, can be re-enabled for bold
        }
        private val blackPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            alpha = 255
        }

        // charPaint is now used for initial setup, but a new Paint object will be created per character in drawFrame for diagnostic
        private val charPaint = Paint(greenPaint)

        private var columns: MutableList<MatrixColumn> = mutableListOf()
        private val random = Random()

        private fun getRandomMatrixChar(): String {
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

            val chars = sb.toString()
            return chars[random.nextInt(chars.length)].toString()
        }

        private val drawRunner = object : Runnable {
            override fun run() {
                drawFrame()
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)

            customFontTypeface = ResourcesCompat.getFont(applicationContext, R.font.matrix_code)!!

            // --- DIAGNOSTIC LOG ---
            Log.d("MatrixWallpaper", "Font loaded: $customFontTypeface")
            Log.d("MatrixWallpaper", "Font is null: ${customFontTypeface == null}")
            Log.d("MatrixWallpaper", "Font class: ${customFontTypeface?.javaClass?.name}")
            // --- END DIAGNOSTIC LOG ---

            greenPaint.typeface = customFontTypeface


            val numberOfColumns = (holder.surfaceFrame.width() / columnWidth).toInt() + 1
            columns.clear()
            for (i in 0 until numberOfColumns) {
                val startY = random.nextInt(holder.surfaceFrame.height()).toFloat() - (textSize * random.nextInt(5))

                val minFallSpeed = textSize / 20f
                val randomSpeedAdditionFactor = textSize * 0.375f

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
            handler.post(drawRunner)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), blackPaint)

                    // --- DIAGNOSTIC DRAWING ---
                    // Temporarily draw a known custom character to test the font
                    // val testChar = '\u30AB' // Katakana Ka - a unique char from your font
                    // canvas.drawText(testChar.toString(), 100f, 200f, greenPaint) // Draw using greenPaint with your custom font
                    // --- END DIAGNOSTIC DRAWING ---


                    for (i in columns.indices) {
                        val x = i * columnWidth
                        val matrixColumn = columns[i]
                        val charactersInColumn = matrixColumn.characters

                        val minFallSpeed = textSize / 20f
                        val randomSpeedAdditionFactor = textSize * 0.375f

                        when (matrixColumn.state) {
                            ColumnState.ACTIVE -> {
                                charactersInColumn.removeAll { it.y > holder.surfaceFrame.height() + textSize }

                                val topCharY = charactersInColumn.firstOrNull()?.y
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

                                if (random.nextFloat() < 0.002f) {
                                    matrixColumn.state = ColumnState.FADING_OUT
                                    matrixColumn.columnAlpha = 255
                                    matrixColumn.columnFadeSpeed = random.nextInt(5) + 2
                                }
                            }
                            ColumnState.FADING_OUT -> {
                                matrixColumn.columnAlpha -= matrixColumn.columnFadeSpeed
                                if (matrixColumn.columnAlpha <= 0) {
                                    charactersInColumn.clear()
                                    val initialColumnCharCount = random.nextInt(10) + 5
                                    var currentY = -random.nextInt(holder.surfaceFrame.height() / 2).toFloat()
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

                        for (j in charactersInColumn.indices) {
                            val charObj = charactersInColumn[j]

                            charObj.ticksUntilChange--
                            if (charObj.ticksUntilChange <= 0) {
                                charObj.char = getRandomMatrixChar()
                                charObj.ticksUntilChange = random.nextInt(10) + 5
                            }

                            charObj.fadeProgress += charObj.fadeRate

                            if (j < charactersInColumn.size - 1) {
                                val charBelow = charactersInColumn[j + 1]
                                charObj.fadeProgress = charObj.fadeProgress.coerceAtMost(charBelow.fadeProgress + 0.0025f)
                            }
                            charObj.fadeProgress = charObj.fadeProgress.coerceIn(0f, 1f)

                            // --- OPTIMIZED CHARACTER PAINTING ---
                            // Reuse the existing 'greenPaint' object and modify its properties.
                            // greenPaint already has the correct color, text size, and typeface set in onSurfaceCreated.

                            val individualCharAlpha = (255 * (1f - charObj.fadeProgress)).toInt().coerceIn(0, 255)
                            val combinedAlpha = (individualCharAlpha * (matrixColumn.columnAlpha / 255f)).toInt().coerceIn(0, 255)

                            greenPaint.alpha = combinedAlpha // Set alpha for this character
                            greenPaint.clearShadowLayer()    // Clear any previous shadow before potentially adding a new one

                            val glowFadeThreshold = 0.7f
                            if (charObj.hasLeadingGlow && charObj.fadeProgress < glowFadeThreshold) {
                                val glowProgressFactor = 1f - (charObj.fadeProgress / glowFadeThreshold).coerceIn(0f, 1f)
                                val currentGlowRadius = charObj.maxGlowRadius * glowProgressFactor

                                if (currentGlowRadius > 0f) {
                                    // Apply shadow for this character using the same greenPaint object
                                    greenPaint.setShadowLayer(
                                        currentGlowRadius,
                                        0f,
                                        0f,
                                        Color.GREEN
                                    )
                                }
                            }

                            // Draw the character using the modified greenPaint
                            canvas.drawText(charObj.char, x, charObj.y, greenPaint)
                            // --- END OPTIMIZED CHARACTER PAINTING ---

                            charObj.y += charObj.fallSpeed
                        }
                    }
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            if (isVisible) {
                handler.removeCallbacks(drawRunner)
                handler.postDelayed(drawRunner, frameRate)
            }
        }
    }
}