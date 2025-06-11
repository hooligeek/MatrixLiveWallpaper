package com.example.hooligeek_matrix_live_wallpaper

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WallpaperDemoActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var renderer: MatrixCanvasRenderer
    private val handler = Handler(Looper.getMainLooper())
    private val frameRate = 1000L / 60 // Aim for 60 FPS

    private var isDrawing = false

    private val drawRunner = object : Runnable {
        override fun run() {
            if (isDrawing) {
                drawFrame()
                handler.postDelayed(this, frameRate)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper_demo)

        surfaceView = findViewById(R.id.surface_view)
        surfaceView.holder.addCallback(this)

        // Assuming MatrixCanvasRenderer is correctly defined elsewhere
        // and takes applicationContext
        renderer = MatrixCanvasRenderer(applicationContext)


        val btnCancel = findViewById<Button>(R.id.btn_cancel_preview)
        val btnApply = findViewById<Button>(R.id.btn_apply_wallpaper)

        btnCancel.setOnClickListener {
            finish() // Simply close this demo/preview activity
        }

        btnApply.setOnClickListener {
            try {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this@WallpaperDemoActivity, MatrixWallpaperService::class.java)
                )
                // Optional: You can add this flag if you want your activity to finish
                // after the user makes a selection in the wallpaper picker.
                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Or other flags as needed

                startActivity(intent)
                // You might want to finish this WallpaperDemoActivity after launching the picker
                // finish()
                Toast.makeText(this, "Opening Live Wallpaper picker...", Toast.LENGTH_SHORT).show()

            } catch (e: ActivityNotFoundException) {
                // This might happen if the device doesn't have a live wallpaper picker
                // or if something is fundamentally wrong with the intent.
                e.printStackTrace()
                Toast.makeText(
                    this,
                    "Could not open Live Wallpaper picker. Please set it manually through system settings.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (surfaceView.holder.surface.isValid) { // Ensure surface is valid before starting
            isDrawing = true
            handler.post(drawRunner)
        }
    }

    override fun onPause() {
        super.onPause()
        isDrawing = false
        handler.removeCallbacks(drawRunner)
    }

    override fun onDestroy() {
        super.onDestroy()
        isDrawing = false
        handler.removeCallbacks(drawRunner)
        // Potentially release resources used by MatrixCanvasRenderer if necessary
    }

    private fun drawFrame() {
        var canvas: Canvas? = null
        try {
            canvas = surfaceView.holder.lockCanvas()
            if (canvas != null) {
                // Assuming renderer methods are correctly defined
                renderer.update() // If you have an update method
                renderer.draw(canvas)
            }
        } finally {
            if (canvas != null) {
                try {
                    surfaceView.holder.unlockCanvasAndPost(canvas)
                } catch (e: IllegalStateException) {
                    // Surface might have been destroyed
                    e.printStackTrace()
                }
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Assuming renderer.initialize method exists
        renderer.initialize(holder.surfaceFrame.width(), holder.surfaceFrame.height())
        if (!isDrawing) { // Start drawing if not already started
            isDrawing = true
            handler.post(drawRunner)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Assuming renderer.initialize method exists
        renderer.initialize(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isDrawing = false
        handler.removeCallbacks(drawRunner)
    }
}