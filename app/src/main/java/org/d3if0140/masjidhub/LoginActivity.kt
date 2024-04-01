package org.d3if0140.masjidhub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordToggle: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
