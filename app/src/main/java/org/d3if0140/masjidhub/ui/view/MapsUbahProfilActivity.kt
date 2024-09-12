package org.d3if0140.masjidhub.ui.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.d3if0140.masjidhub.R
import org.d3if0140.masjidhub.databinding.ActivityMapsUbahProfilBinding

class MapsUbahProfilActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsUbahProfilBinding
    private lateinit var mMap: GoogleMap
    private var selectedLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsUbahProfilBinding.inflate(layoutInflater)
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
        val defaultLocation = LatLng(-6.91946128193482, 107.62118006373258) // Example coordinates
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