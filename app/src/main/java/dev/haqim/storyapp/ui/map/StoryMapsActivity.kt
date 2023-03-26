package dev.haqim.storyapp.ui.map

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dev.haqim.storyapp.R
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.databinding.ActivityStoryMapsBinding
import dev.haqim.storyapp.di.Injection
import dev.haqim.storyapp.domain.model.Story
import dev.haqim.storyapp.ui.base.BaseActivity
import dev.haqim.storyapp.ui.story.DetailStoryActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class StoryMapsActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityStoryMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel: StoryMapViewModel by viewModels { 
        Injection.provideViewModelProvider(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        supportActionBar?.title = getString(R.string.title_activity_maps)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        val navigateToDetailStory = viewModel.uiState.map { it.storyToBeOpened }.distinctUntilChanged()
        lifecycleScope.launch { 
            navigateToDetailStory.collectLatest {
                if(it != null){
                    val intent = Intent(
                        this@StoryMapsActivity,
                        DetailStoryActivity::class.java
                    )
                    val bundle = Bundle()
                    bundle.putParcelable(DetailStoryActivity.DETAIL_STORY, it)
                    intent.putExtras(bundle)
                    startActivity(intent)
                    viewModel.processAction(MapUiAction.FinishNavigateToDetailStory)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        getPermissionLocationAccess()
    }


    @SuppressLint("MissingPermission")
    private fun getPermissionLocationAccess(){
        if(checkPermissionLocationAccess()){
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if(location != null){
                    showLatestLocation(location)
                    showStoriesLocations()
                }else{
                    Toast.makeText(
                        this,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            launchPermissionLocation()
        }
    }

    private fun showLatestLocation(location: Location){
        val startLocation = LatLng(location.latitude, location.longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 6f))
    }
    
    private fun showStoriesLocations(){
        val storiesFlow = viewModel.uiState.map { it.stories }.distinctUntilChanged()
        lifecycleScope.launch { 
            storiesFlow.collect{
                when(it){
                    is Resource.Error -> {
                        Toast.makeText(
                            this@StoryMapsActivity,
                            it.message ?: "Unknown error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Resource.Success -> {
                        it.data?.let { stories -> generateStoriesMarkers(stories) }
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun generateStoriesMarkers(stories: List<Story>){
        stories.forEach {story ->
            if(story.lat != null && story.lon != null){
                val latLng = LatLng(story.lat, story.lon)
                val addressName = getAddressName(story.lat, story.lon)
                val marker = mMap
                    .addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(story.name)
                            .snippet(addressName)
                    )
                marker?.tag = story
            }
        }
        
        mMap.setOnInfoWindowClickListener { marker -> 
            val story = marker.tag as? Story
            story?.let { 
                viewModel.processAction(MapUiAction.NavigateToDetailStory(it))
            }
        }
        
    }

    private fun getAddressName(latitude: Double, longitude: Double): String?{
        var addressName: String? = null
        val geocoder = Geocoder(this@StoryMapsActivity, Locale.getDefault())
        try {
            val list = geocoder.getFromLocation(latitude, longitude, 1)
            if (list != null && list.size > 0){
                addressName = list[0].getAddressLine(0)
                Log.d(ContentValues.TAG, "getAddressName: $addressName")
            }
        }catch (e: IOException){
            e.printStackTrace()
        }
        return addressName
    }
}