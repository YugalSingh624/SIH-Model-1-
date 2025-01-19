package com.example.sih

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.Surface
import com.google.android.filament.*
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.MaterialProvider
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ModelRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var engine: Engine
    private lateinit var scene: Scene
    private lateinit var view: View
    private lateinit var camera: Camera
    private lateinit var renderer: Renderer
    private var model: FilamentAsset? = null
    private var swapChain: SwapChain? = null
    private lateinit var assetLoader: AssetLoader
    private lateinit var resourceLoader: ResourceLoader
    private var surface: Surface? = null

    init {
        Utils.init()
        initializeFilament()
    }

    private fun initializeFilament() {
        engine = Engine.create()
        scene = engine.createScene()
        view = engine.createView()
        val cameraEntity = EntityManager.get().create()
        camera = engine.createCamera(cameraEntity)
        renderer = engine.createRenderer()
        assetLoader = AssetLoader(engine, MaterialProvider(engine), EntityManager.get())
        resourceLoader = ResourceLoader(engine)

        view.camera = camera
        view.scene = scene
    }

    fun createSwapChain(surface: Surface) {
        this.surface = surface
        swapChain = engine.createSwapChain(surface)
    }

    fun destroySwapChain() {
        swapChain?.let {
            engine.destroySwapChain(it)
            swapChain = null
        }
    }

    fun loadModel(assetPath: String) {
        try {
            val buffer = context.assets.open(assetPath).use { it.readBytes() }
            model = assetLoader.createAssetFromBinary(ByteBuffer.wrap(buffer))
            model?.let { asset ->
                scene.addEntities(asset.entities)
                resourceLoader.loadResources(asset)
                asset.releaseSourceData()
            }
        } catch (e: Exception) {
            Log.e("ModelRenderer", "Failed to load model", e)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // No-op
    }

    override fun onDrawFrame(gl: GL10?) {
        swapChain?.let {
            renderer.render(view)
        } ?: Log.e("ModelRenderer", "SwapChain is null, cannot render")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        view.viewport = Viewport(0, 0, width, height)
        val aspect = width.toDouble() / height.toDouble()
        camera.setProjection(45.0, aspect, 0.1, 20.0, Camera.Fov.VERTICAL)
        surface?.let { createSwapChain(it) }  // Ensure SwapChain is created
    }
}
