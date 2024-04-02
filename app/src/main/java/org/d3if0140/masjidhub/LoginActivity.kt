package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import org.d3if0140.masjidhub.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    // Deklarasi variabel binding untuk menggunakan ViewBinding
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menggunakan inflate pada binding untuk menghubungkan layout XML dengan Activity
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Menambahkan onClickListener pada button backButton untuk kembali ke WelcomeActivity
        binding.backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        // Set teks pada textDaftar menjadi "Belum punya akun? Daftar" dengan tautan ke RegistActivity
        val text = "Belum punya akun? Daftar"
        val spannableString = SpannableString(text)
        val startIndex = text.indexOf("Daftar")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val intent = Intent(this@LoginActivity, RegistActivity::class.java)
                startActivity(intent)
            }
        }
        spannableString.setSpan(clickableSpan, startIndex, startIndex + 4, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textDaftar.text = spannableString
        binding.textDaftar.movementMethod = LinkMovementMethod.getInstance()

        // Menambahkan onClickListener pada passwordToggle untuk mengubah visibilitas password
        binding.passwordToggle.setOnClickListener {
            val inputType = binding.passwordEditText.inputType
            if (inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.passwordToggle.setImageResource(R.drawable.baseline_visibility_off)
            } else {
                binding.passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.passwordToggle.setImageResource(R.drawable.baseline_visibility)
            }
            binding.passwordEditText.setSelection(binding.passwordEditText.text?.length ?: 0)
        }
    }
}


