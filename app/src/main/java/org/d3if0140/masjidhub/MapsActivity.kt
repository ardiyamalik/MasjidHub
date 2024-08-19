package org.d3if0140.masjidhub

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.d3if0140.masjidhub.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private var selectedLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.selectLocationButton.setOnClickListener {
            val resultIntent = Intent().apply {
                selectedLocation?.let {
                    putExtra("latitude", it.latitude)
                    putExtra("longitude", it.longitude)
                }
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val defaultLocation = LatLng(-7.026074081265786, 107.5426824824841) // Example coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15.0f))

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            selectedLocation = latLng
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
        }

        binding.selectLocationButton.setOnClickListener {
            val resultIntent = Intent().apply {
                selectedLocation?.let {
                    putExtra("latitude", it.latitude)
                    putExtra("longitude", it.longitude)
                }
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}