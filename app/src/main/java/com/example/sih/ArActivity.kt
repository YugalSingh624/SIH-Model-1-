package com.example.sih

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.pose.PoseLandmark

class ArActivity : AppCompatActivity() {

    private lateinit var modelRenderer: ModelRenderer
    private lateinit var modelSurfaceView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)  // Use this to set the XML layout

        modelSurfaceView = findViewById(R.id.modelSurfaceView)
        modelSurfaceView.setEGLContextClientVersion(3)
        modelRenderer = ModelRenderer(this)
        modelSurfaceView.setRenderer(modelRenderer)
        modelSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        // Create a surface for rendering
        val surface = modelSurfaceView.holder.surface
        modelRenderer.createSwapChain(surface)

        // Load model
        loadModel()
    }

    private fun loadModel() {
        modelRenderer.loadModel("model.glb") // Ensure this path is correct
    }

    override fun onResume() {
        super.onResume()
        modelSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        modelSurfaceView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        modelRenderer.destroySwapChain()  // Clean up resources
    }
}
