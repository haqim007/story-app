package dev.haqim.storyapp.ui.add_story

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import dev.haqim.storyapp.R
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.databinding.ActivityAddStoryBinding
import dev.haqim.storyapp.di.Injection
import dev.haqim.storyapp.helper.util.*
import dev.haqim.storyapp.ui.CameraActivity
import dev.haqim.storyapp.ui.CameraActivity.Companion.IS_BACK_CAMERA
import dev.haqim.storyapp.ui.base.BaseActivity
import dev.haqim.storyapp.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

class AddStoryActivity : BaseActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel: AddStoryViewModel by viewModels{
        Injection.provideViewModelProvider(this)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                val error = Snackbar.make(
                    binding.root,
                    getString(R.string.not_getting_needed_permission),
                    Snackbar.LENGTH_INDEFINITE
                )
                error.setAction(R.string.close){
                    error.dismiss()
                    finish()
                }
                error.show()

            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.new_story)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val uiState = viewModel.uiState
        val uiAction = {action: AddStoryUiAction -> viewModel.processAction(action)}

        bindingCamera(uiState, uiAction)

        bindingGallery(uiState, uiAction)

        bindingUpload(uiState, uiAction)

        bindNavigateToStories(uiState, uiAction)

        bindSetDescription(uiState, uiAction)

        bindShareLocation(uiAction)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun bindShareLocation(
        uiAction: (AddStoryUiAction) -> Boolean
    ) {

        binding.switchToShare.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                if(checkPermissionLocationAccess()){
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if(location != null){
                            uiAction(AddStoryUiAction.ShareLocation(isChecked, location.longitude, location.latitude))
                        }else{
                            Toast.makeText(
                                this,
                                "Location is not found. Try Again",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.switchToShare.isChecked = false
                        }
                    }
                } else {
                    launchPermissionLocation()
                    binding.switchToShare.isChecked = false
                }
            }else{
                uiAction(AddStoryUiAction.ShareLocation(isChecked))
            }
        }
        
        binding.switchToShare.doAfterTextChanged {
            uiAction(AddStoryUiAction.SetDescription(it.toString()))
        }
    }

    private fun bindSetDescription(
        uiState: StateFlow<AddStoryUiState>,
        uiAction: (AddStoryUiAction) -> Boolean
    ) {
        val descriptionInputFlow = uiState.map { it.description }.distinctUntilChanged()
        lifecycleScope.launch {
            descriptionInputFlow.collectLatest {
                binding.tilDescription.isErrorEnabled = true
                if (it is ResultInput.Invalid) {
                    when (it.validation) {
                        InputValidation.RequiredFieldInvalid -> {
                            binding.tilDescription.error = getString(R.string.description_is_required)
                        }
                        else -> {}
                    }
                } else {
                    binding.tilDescription.error = null
                    binding.tilDescription.isErrorEnabled = false
                }
            }
        }

        binding.edDescription.doAfterTextChanged {
            uiAction(AddStoryUiAction.SetDescription(it.toString()))
        }
    }

    private fun bindNavigateToStories(
        uiState: StateFlow<AddStoryUiState>,
        uiAction: (AddStoryUiAction) -> Boolean,
    ) {
        val navigateToStoriesFlow = uiState.map { it.navigateToStories }.distinctUntilChanged()
        lifecycleScope.launch {
            navigateToStoriesFlow.collect {
                if (it) {
                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this@AddStoryActivity,
                            Pair(binding.edDescription, "description"),
                            Pair(binding.ivImage, "photo"),
                        )

                    val intent = Intent(
                        this@AddStoryActivity,
                        MainActivity::class.java
                    )
                    startActivity(intent, optionsCompat.toBundle())
                    uiAction(AddStoryUiAction.NavigateToStories)
                    finish()
                }
            }
        }
    }

    private fun bindingUpload(
        uiState: StateFlow<AddStoryUiState>,
        uiAction: (AddStoryUiAction) -> Boolean,
    ) {
        binding.btnUpload.apply {
            setText(getString(R.string.upload))
            setEnable(false)
            setAllCaps(false)
        }
        binding.btnUpload.setOnClickListener {
            uiAction(AddStoryUiAction.UploadStory)
        }
        val submitResultFlow = uiState.map { it.uploadResult }.distinctUntilChanged()
        lifecycleScope.launch {
            submitResultFlow.collectLatest {
                binding.btnUpload.apply {
                    setLoading(false)
                    setEnable(uiState.value.allInputValid)
                }
                when (it) {
                    is Resource.Loading -> {
                        binding.btnUpload.apply {
                            setLoading(true)
                            setEnable(false)
                        }.also {
                            binding.edDescription.clearFocus()
                        }
                    }
                    is Resource.Success -> {
                        binding.btnGalerry.isClickable = false
                        binding.btnCamera.isClickable = false
                        binding.btnUpload.isClickable = false
                        binding.edDescription.apply {
                            isEnabled = false
                            isFocusableInTouchMode = false
                        }
                        val successSnackBar = Snackbar.make(
                            binding.root,
                            it.message ?: getString(R.string.successfully_uploaded_story),
                            Snackbar.LENGTH_INDEFINITE
                        )
                        lifecycleScope.launch {
                            delay(3000)
                            uiAction(AddStoryUiAction.NavigateToStories)
                        }
                        successSnackBar.show()

                    }
                    is Resource.Error -> {
                        val mySnackBar = Snackbar.make(
                            binding.root,
                            it.message ?: getString(R.string.failed_to_upload),
                            Snackbar.LENGTH_INDEFINITE
                        )
                        mySnackBar.setAction(R.string.close) {
                            mySnackBar.dismiss()
                        }
                        mySnackBar.show()
                    }
                    else -> {}
                }
            }
        }

        val allInputValidFlow = uiState.map { it.allInputValid }.distinctUntilChanged()
        lifecycleScope.launch {
            allInputValidFlow.collect {
                binding.btnUpload.setEnable(it)
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun bindingCamera(
        uiState: StateFlow<AddStoryUiState>,
        uiAction: (AddStoryUiAction) -> Boolean
    ) {
        binding.btnCamera.setOnClickListener {
            uiAction(AddStoryUiAction.OpenCamera)
        }
        val openCameraFlow = uiState.map { it.openCamera }.distinctUntilChanged()
        lifecycleScope.launch {
            openCameraFlow.collect{
                if(it){
                    val intent = Intent(this@AddStoryActivity, CameraActivity::class.java)
                    launcherIntentCameraX.launch(intent)
                    uiAction(AddStoryUiAction.CloseCamera)
                }
            }
        }
    }

    private fun bindingGallery(
        uiState: StateFlow<AddStoryUiState>,
        uiAction: (AddStoryUiAction) -> Boolean
    ) {
        binding.btnGalerry.setOnClickListener {
            uiAction(AddStoryUiAction.OpenGallery)
        }
        val openCameraFlow = uiState.map { it.openGallery }.distinctUntilChanged()
        lifecycleScope.launch {
            openCameraFlow.collect{
                if(it){
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "image/*"
                    val chooser = Intent.createChooser(intent, getString(R.string.choose_a_picture))
                    launcherIntentGallery.launch(chooser)
                    uiAction(AddStoryUiAction.CloseGallery)
                }
            }
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra(CameraActivity.PICTURE) as File
            val reducedFile = reduceFileImage(myFile)
            if(reducedFile.length() > MAX_IMAGE_SIZE){
                val mySnackBar = Snackbar.make(binding.root,
                    getString(R.string.exceed_max_size),
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.try_again){
                    viewModel.processAction(AddStoryUiAction.OpenCamera)
                }
                mySnackBar.show()
            }else{
                viewModel.processAction(AddStoryUiAction.SetFile(reducedFile))
                val isBackCamera = it.data?.getBooleanExtra(IS_BACK_CAMERA, true) as Boolean
                val result = rotateBitmap(
                    BitmapFactory.decodeFile(reducedFile.path),
                    isBackCamera
                )
                binding.ivImage.setImageBitmap(result)
            }
            
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@AddStoryActivity)
            if(myFile.length() > MAX_IMAGE_SIZE){
                val mySnackBar = Snackbar.make(binding.root,
                    getString(R.string.exceed_max_size),
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.try_again){
                    viewModel.processAction(AddStoryUiAction.OpenGallery)
                }
                mySnackBar.show()
            }else{
                viewModel.processAction(AddStoryUiAction.SetFile(myFile))
                binding.ivImage.setImageURI(selectedImg)   
            }
        }
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val MAX_IMAGE_SIZE = 1048576
    }
}