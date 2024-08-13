package org.d3if0140.masjidhub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.d3if0140.masjidhub.databinding.ActivityFullScreenImageBinding

class FullScreenImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullScreenImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val caption = intent.getStringExtra("CAPTION") ?: ""
        val truncatedCaption = if (caption.length > 100) caption.substring(0, 100) + "..." else caption

        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .into(binding.fullScreenImageView)
        }

        binding.captionTextView.text = truncatedCaption
        binding.captionTextView.setOnClickListener {
            if (binding.captionTextView.text.toString().endsWith("...")) {
                binding.captionTextView.text = caption
            } else {
                binding.captionTextView.text = truncatedCaption
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}
