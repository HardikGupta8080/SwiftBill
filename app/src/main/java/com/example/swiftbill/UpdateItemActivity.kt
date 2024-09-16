// UpdateItemActivity.kt

package com.example.swiftbill

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.swiftbill.databinding.ActivityUpdateItemBinding
import com.example.swiftbill.model.Item
import com.google.android.material.snackbar.Snackbar
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
//for deleting
        binding.deleteitem.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            val documentRef = db.collection("USER").document(Firebase.auth.currentUser?.uid.toString()).collection("INVETORY").document(uid.toString())

            // Fetch the document data before deletion
            documentRef.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val documentData = documentSnapshot.data as HashMap<String, Any>?


                        // Show the confirmation dialog
                        AlertDialog.Builder(this)
                            .setTitle("Delete")
                            .setMessage("Are you sure you want to Delete?")
                            .setPositiveButton("Yes") { dialog, which ->
                                // Delete the document
                                documentRef.delete()
                                    .addOnSuccessListener {
                                        // Document successfully deleted

                                        Toast.makeText(this,"Deleted successfully",Toast.LENGTH_SHORT).show()


                                        // Show Snackbar with Undo option
                                        val resultIntent = Intent()
                                        resultIntent.putExtra("UNDO_DATA", documentData)
                                        resultIntent.putExtra("ITEM_ID", uid)
                                        setResult(RESULT_OK, resultIntent)
                                       finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("Firestore", "Error deleting document", e)
                                    }
                            }
                            .setNegativeButton("No") { dialog, which ->
                                dialog.dismiss()
                            }
                            .show()
                    } else {
                        Log.w("Firestore", "Document does not exist")
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error fetching document", e)
                }
        }
//for Updating
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
                db.collection("USER").document(Firebase.auth.currentUser?.uid.toString()).collection("INVETORY").document(uid.toString())
                    .update(updates).addOnSuccessListener {
                        finish()
                }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to Update due to poor internet" , Toast.LENGTH_SHORT).show()
                    }

            }
        }

    }
}
