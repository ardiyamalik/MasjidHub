package org.d3if0140.masjidhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText

class RegistActivity : AppCompatActivity() {

    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordToggle: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regist)

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this,WelcomeActivity::class.java)
            startActivity(intent)
        }

        val textLogin: TextView = findViewById(R.id.textLogin)

        // Teks yang akan ditampilkan dalam TextView
        val text = "Sudah punya akun? Login"

        // Membuat objek SpannableString dari teks
        val spannableString = SpannableString(text)

        // Mendapatkan indeks kata "sini" dalam teks
        val startIndex = text.indexOf("Login")

        // Membuat ClickableSpan untuk menangani klik pada kata "sini"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // Tindakan yang akan dilakukan saat teks diklik (mengarahkan ke halaman pendaftaran)
                val intent = Intent(this@RegistActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        // Menambahkan ClickableSpan ke SpannableString
        spannableString.setSpan(clickableSpan, startIndex, startIndex + 4, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Mengatur teks dalam TextView
        textLogin.text = spannableString

        // Mengaktifkan tautan gerak dalam TextView
        textLogin.movementMethod = LinkMovementMethod.getInstance()

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


        val emailEditText: TextInputEditText = findViewById(R.id.emailEditText)
        val spinner: Spinner = findViewById(R.id.DkmSpinner)

        // Data untuk opsi dropdown
        val dkmOptions = arrayOf("Jamaah Masjid", "Masjid Nurul Hikmah", "Masjid Al-Lathif", "Masjid Al-Jabar")

        // Buat adapter untuk spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dkmOptions)

        // Set adapter untuk spinner
        spinner.adapter = adapter

        // Atur posisi awal spinner ke "Jamaah Masjid"
        spinner.setSelection(0)

    }
}