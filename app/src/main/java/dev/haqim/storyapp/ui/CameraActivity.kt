package dev.haqim.storyapp.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import dev.haqim.storyapp.R
import dev.haqim.storyapp.databinding.ActivityCameraBinding
import dev.haqim.storyapp.helper.util.createFile
import dev.haqim.storyapp.ui.add_story.AddStoryActivity
import dev.haqim.storyapp.ui.base.BaseActivity

class CameraActivity : BaseActivity() {
    private lateinit var binding: ActivityCameraBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.captureImage.setOnClickListener { takePhoto() }
        binding.switchCamera.setOnClickListener { startCamera() }
    }
    
    override fun onResume() {
        super.onResume()
        hideSystemUI()
        startCamera()
    }
    
    private fun takePhoto() {
        Toast.makeText(
            this@CameraActivity,
            getString(R.string.please_wait),
            Toast.LENGTH_LONG
        ).show()
        binding.captureImage.isClickable = false
        binding.switchCamera.isClickable = false
        
        val imageCapture = imageCapture ?: return
        val photoFile = createFile(application)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        getString(R.string.failed_to_take_a_picture),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.captureImage.isClickable = true
                    binding.switchCamera.isClickable = true
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        this@CameraActivity,
                        getString(R.string.success_taking_picture),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.captureImage.isClickable = true
                    binding.switchCamera.isClickable = true

                    val intent = Intent()
                    intent.putExtra(PICTURE, photoFile)
                    intent.putExtra(
                        IS_BACK_CAMERA,
                        cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA
                    )
                    setResult(AddStoryActivity.CAMERA_X_RESULT, intent)
                    finish()
                }

            }
        )
    }
    
    private var imageCapture: ImageCapture? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    
    private fun startCamera() {
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            
            imageCapture = ImageCapture
                .Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(
                    this@CameraActivity,
                    getString(R.string.failed_to_start_camera),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    companion object{
        const val PICTURE = "picture"
        const val IS_BACK_CAMERA = "isBackCamera"
    }
}