package com.childrenofcorn.eldorado

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.childrenofcorn.eldorado.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val editTextEmail = binding.editTextEmail
        val editTextPassword = binding.editTextPassword

        binding.buttonSignIn.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    val intent = Intent(this, ListActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, R.string.check_credentials, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}