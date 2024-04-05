package org.d3if0140.masjidhub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.d3if0140.masjidhub.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
   private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageList = listOf(R.drawable.masjidhublogo, R.drawable.masjidhublogo, R.drawable.masjidhublogo) // Ganti dengan gambar-gambar yang ingin Anda tampilkan

        val adapter = CarouselAdapter(imageList)
        binding.viewPager.adapter = adapter
    }
}