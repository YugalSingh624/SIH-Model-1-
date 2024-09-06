package com.example.sih

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var previewView: PreviewView
    private lateinit var modelSurfaceView: SurfaceView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var overlayView: OverlayView
    private lateinit var modelRenderer: ModelRenderer
    private lateinit var button: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        modelSurfaceView = findViewById(R.id.modelSurfaceView)
        overlayView = findViewById(R.id.overlayView)
        button = findViewById(R.id.button)
        button.setOnClickListener {

                val intent = Intent(this, ArActivity::class.java)
                startActivity(intent)
            }


        cameraExecutor = Executors.newSingleThreadExecutor()
        modelRenderer = ModelRenderer(this)

        modelSurfaceView.holder.addCallback(this)

        lifecycleScope.launch {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { imageAnalysis ->
                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        detectPose(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e("CameraXApp", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun detectPose(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE) // for real time
            .build()

        val poseDetector = PoseDetection.getClient(options)

        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                processPose(pose)
            }
            .addOnFailureListener { e ->
                Log.e("PoseDetection", "Pose Detection failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun processPose(pose: Pose) {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isNotEmpty()) {
            overlayView.setPoseLandmarks(landmarks)
            //modelRenderer.updatePose(landmarks) // Update model with pose landmarks
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()  // Start camera if permission is granted
            } else {
                // Handle permission denial
            }
        }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()  // Clean up the camera executor
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val surfaceTexture = SurfaceTexture(0).apply {
            setDefaultBufferSize(modelSurfaceView.width, modelSurfaceView.height)
        }
        val surface = Surface(surfaceTexture)
        modelRenderer.createSwapChain(surface)
        //modelRenderer.loadModel("assets/Jake.glb") // Ensure correct path and file extension
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Handle surface changes if needed
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        modelRenderer.destroySwapChain()
        // Clean up resources if needed
    }
}
