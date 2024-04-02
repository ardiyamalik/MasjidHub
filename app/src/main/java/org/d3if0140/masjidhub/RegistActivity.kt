package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import org.d3if0140.masjidhub.databinding.ActivityRegistBinding

class RegistActivity : AppCompatActivity() {
    // Deklarasi variabel binding untuk menggunakan ViewBinding
    private lateinit var binding: ActivityRegistBinding
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordToggle: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menggunakan inflate pada binding untuk menghubungkan layout XML dengan Activity
        binding = ActivityRegistBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Menambahkan onClickListener pada button backButton untuk kembali ke WelcomeActivity
        val backButton: ImageButton = binding.backButton
        backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        // Mengatur teks pada textLogin menjadi "Sudah punya akun? Login" dengan tautan ke LoginActivity
        val textLogin: TextView = binding.textLogin
        val text = "Sudah punya akun? Login"
        val spannableString = SpannableString(text)
        val startIndex = text.indexOf("Login")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val intent = Intent(this@RegistActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        spannableString.setSpan(clickableSpan, startIndex, startIndex + 4, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        textLogin.text = spannableString
        textLogin.movementMethod = LinkMovementMethod.getInstance()

        // Menghubungkan passwordEditText dan passwordToggle dengan elemen UI yang sesuai
        passwordEditText = binding.passwordEditText
        passwordToggle = binding.passwordToggle

        // Menambahkan onClickListener pada passwordToggle untuk mengubah visibilitas password
        passwordToggle.setOnClickListener {
            val inputType = passwordEditText.inputType
            if (inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.baseline_visibility_off)
            } else {
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.baseline_visibility)
            }
            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
        }

        // Menghubungkan emailEditText dan spinner dengan elemen UI yang sesuai
        val emailEditText: TextInputEditText = binding.emailEditText
        val spinner: Spinner = binding.DkmSpinner

        // Data untuk opsi dropdown pada spinner
        val dkmOptions = arrayOf("Jamaah Masjid", "Masjid Nurul Hikmah", "Masjid Al-Lathif", "Masjid Al-Jabar")

        // Membuat adapter untuk spinner dan mengatur posisi awal ke "Jamaah Masjid"
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dkmOptions)
        spinner.adapter = adapter
        spinner.setSelection(0)
    }
}

