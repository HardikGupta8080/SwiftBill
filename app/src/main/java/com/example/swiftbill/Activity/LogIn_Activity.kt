package com.example.swiftbill.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.swiftbill.databinding.ActivityLogInBinding
import com.example.swiftbill.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.ncorti.slidetoact.SlideToActView

class LogIn_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding
     private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLogInBinding.inflate(layoutInflater)

        binding.forgotpassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        auth= Firebase.auth
        binding.tosignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        binding.LoinButton.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener{
            override fun onSlideComplete(view: SlideToActView) {

                view.resetSlider()
                if (binding.EmailText.editText?.text.toString().isEmpty()||binding.Password.editText?.text.toString().isEmpty()) {
                    Toast.makeText(this@LogIn_Activity,"Fill All the details",Toast.LENGTH_SHORT).show()
                }
                else{
                var user=User(binding.EmailText.editText?.text.toString().trim(),binding.Password.editText?.text.toString())
                    auth.signInWithEmailAndPassword(user.email!!,user.password!!)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this@LogIn_Activity,
                                    "Login Successful",
                                    Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LogIn_Activity, HostActivity::class.java))
                                finish()
                            }
                            else
                            {
                                Toast.makeText(this@LogIn_Activity,it.exception?.localizedMessage,Toast.LENGTH_SHORT).show()
                            }
                        }

                }
            }

        }  //signin......
        setContentView(binding.root)
    }
}