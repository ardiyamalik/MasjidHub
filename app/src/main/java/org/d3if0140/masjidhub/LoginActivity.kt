package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordToggle: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this,WelcomeActivity::class.java)
            startActivity(intent)
        }

        val textDaftar: TextView = findViewById(R.id.textDaftar)

        // Teks yang akan ditampilkan dalam TextView
        val text = "Belum punya akun? Daftar"

        // Membuat objek SpannableString dari teks
        val spannableString = SpannableString(text)

        // Mendapatkan indeks kata "sini" dalam teks
        val startIndex = text.indexOf("Daftar")

        // Membuat ClickableSpan untuk menangani klik pada kata "sini"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // Tindakan yang akan dilakukan saat teks diklik (mengarahkan ke halaman pendaftaran)
                val intent = Intent(this@LoginActivity, RegistActivity::class.java)
                startActivity(intent)
            }
        }

        // Menambahkan ClickableSpan ke SpannableString
        spannableString.setSpan(clickableSpan, startIndex, startIndex + 4, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Mengatur teks dalam TextView
        textDaftar.text = spannableString

        // Mengaktifkan tautan gerak dalam TextView
        textDaftar.movementMethod = LinkMovementMethod.getInstance()

        passwordEditText = findViewById(R.id.passwordEditText)
        passwordToggle = findViewById(R.id.passwordToggle)

        // Set onClickListener for passwordToggle button
        passwordToggle.setOnClickListener {
            // Change inputType of passwordEditText to toggle visibility
            val inputType = passwordEditText.inputType
            if (inputType == 129) { // Password mode (textPassword)
                passwordEditText.inputType = 145 // Visible password mode (textVisiblePassword)
                passwordToggle.setImageResource(R.drawable.baseline_visibility) // Change icon to visible
            } else { // Visible password mode (textVisiblePassword)
                passwordEditText.inputType = 129 // Password mode (textPassword)
                passwordToggle.setImageResource(R.drawable.baseline_visibility_off) // Change icon to hidden
            }
            // Move cursor to the end of the text
            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
        }
    }
}

