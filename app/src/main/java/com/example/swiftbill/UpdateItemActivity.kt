// UpdateItemActivity.kt

package com.example.swiftbill

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.swiftbill.databinding.ActivityUpdateItemBinding
import com.example.swiftbill.model.Item
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class UpdateItemActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityUpdateItemBinding.inflate(layoutInflater)
    }
   private val db = FirebaseFirestore.getInstance()


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom =  imeHeight )
            insets
        }


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        // Get the item ID or other data from the intent

        val itemId = intent.getStringExtra("ITEM_NAME")
        val cp= intent.getStringExtra("CP")
        val sp= intent.getStringExtra("SP")
        val stock= intent.getStringExtra("STOCK")
        val uid= intent.getStringExtra("UID")
        Log.d("tag",itemId.toString())
        binding.Namee.setText(itemId)
        binding.cost.setText(cp)
        binding.sell.setText(sp)
        binding.stok.setText(stock)

        binding.Update.setOnClickListener {
            var name = binding.Name.editText?.text.toString()
            var stock = binding.stock.editText?.text.toString()
            var cp = binding.cp.editText?.text.toString()
            var sp = binding.sp.editText?.text.toString()

            if (name.isEmpty() || stock.isEmpty() || cp.isEmpty() || sp.isEmpty()) {

                Toast.makeText(this, "Fill all details", Toast.LENGTH_SHORT).show()
            } else {
                val updates = hashMapOf<String, Any>(
                    "productname" to name,
                    "inStock" to stock.toInt(),
                    "ratecp" to cp.toInt(),
                    "ratesp" to sp.toInt()
                )
                db.collection(Firebase.auth.currentUser?.uid.toString()).document(uid.toString())
                    .update(updates).addOnSuccessListener {
                    Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
                }
                    .addOnFailureListener {
                        Toast.makeText(this, "Not Done", Toast.LENGTH_SHORT).show()
                    }

            }
        }

    }
}
