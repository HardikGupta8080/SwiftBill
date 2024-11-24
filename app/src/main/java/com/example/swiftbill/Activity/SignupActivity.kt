package com.example.swiftbill.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.swiftbill.databinding.ActivitySignupBinding
import com.example.swiftbill.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.ncorti.slidetoact.SlideToActView

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private var db=FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignupBinding.inflate(layoutInflater)



        binding.signupButton.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {

                    auth= Firebase.auth
                    var user=User(binding.Name.editText?.text.toString().trim(),binding.EmailText.editText?.text.toString().trim(),binding.Password.editText?.text.toString().trim())
                    view.resetSlider()

                    if (binding.Name.editText?.text.toString().isEmpty() || binding.Password.editText?.text.toString().isEmpty()||binding.Name.editText?.text.toString().isEmpty()) {
                        Toast.makeText(this@SignupActivity, "Fill all Details", Toast.LENGTH_LONG).show()
                    }
                    else {
                        auth.createUserWithEmailAndPassword(user.email!!, user.password!!)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(
                                        this@SignupActivity,
                                        "Sign Up Successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val userMap = mapOf(
                                        "name" to user.name.toString(),
                                        "email" to user.email.toString()
                                    )
                                    db.collection("USER").document(Firebase.auth.currentUser?.uid.toString()).set(userMap)
                                    startActivity(Intent(this@SignupActivity, HostActivity::class.java))
                                    finish()
                                }
                                else {
                                    Toast.makeText(
                                        baseContext,
                                        it.exception?.localizedMessage,
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                    }

                    //signup.....




                }
            }//For creating a new user(sign up)
        binding.gotologin.setOnClickListener {
            startActivity(Intent(this, LogIn_Activity::class.java))
        }//going back to login activity......//
        setContentView(binding.root)
    }
}