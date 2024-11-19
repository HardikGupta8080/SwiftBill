// UpdateItemActivity.kt

package com.example.swiftbill

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swiftbill.Adapter.PurchaseItemAdapter
import com.example.swiftbill.Adapter.item_adapter
import com.example.swiftbill.databinding.ActivityUpdateItemBinding
import com.example.swiftbill.model.InventoryTransaction
import com.example.swiftbill.model.Item
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

class UpdateItemActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityUpdateItemBinding.inflate(layoutInflater)
    }
    private val db = FirebaseFirestore.getInstance()
    private lateinit var padapter: PurchaseItemAdapter

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


//toolbarsetup
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        // Get the item ID or other data from the intent
        val itemId = intent.getStringExtra("ITEM_NAME")
        val cp = intent.getStringExtra("CP")
        val sp = intent.getStringExtra("SP")
        val stock = intent.getStringExtra("STOCK")
        val uid = intent.getStringExtra("UID")
        val catgory = intent.getStringExtra("CATAGORY")
        Log.d("tag", itemId.toString())
        binding.itemname.setText(itemId)
        binding.costp.setText(cp)
        binding.sellp.setText(sp)
        binding.stocks.setText(stock)
//for deleting
        binding.deleteitem.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            val documentRef =
                db.collection("USER").document(Firebase.auth.currentUser?.uid.toString())
                    .collection("INVETORY").document(uid.toString())

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

                                    Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT)
                                        .show()


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
            // Create an AlertDialog builder
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Update Item")

            // Set up input fields for the dialog
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 40, 50, 10)
            }

            val nameInput = EditText(this).apply {
                hint = "Enter Product Name"
                setText(itemId)
            }
            val spInput = EditText(this).apply {
                hint = "Enter Selling Price (SP)"
                setText(cp)
                inputType = InputType.TYPE_CLASS_NUMBER // Set input type for numeric entry
            }
            val category = EditText(this).apply {
                setText(catgory)
            }

            layout.addView(nameInput)
            layout.addView(spInput)
            layout.addView(category)
            builder.setView(layout)

            // Set up dialog buttons
            builder.setPositiveButton("Update") { dialog, _ ->
                val name = nameInput.text.toString()
                val sp = spInput.text.toString()
                val catagary=category.text.toString()

                if (name.isEmpty() || sp.isEmpty()||catgory!!.isEmpty()) {
                    Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show()
                } else {
                    db.collection("USER").document(Firebase.auth.currentUser?.uid.toString()).collection("CATEGORIES")
                    val updates = hashMapOf<String, Any>(
                        "productname" to name,
                        "ratesp" to sp.toInt(),
                        "category" to catagary
                    )
                    // Update the Firestore database with new values
                    db.collection("USER")
                        .document(Firebase.auth.currentUser?.uid.toString())
                        .collection("INVETORY")
                        .document(uid.toString())
                        .update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT)
                                .show()
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Failed to update due to poor internet",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            // Show the dialog
            builder.create().show()
        }
        //for adding purchase
        binding.purchase.setOnClickListener {
            val context = this
            val cpText = binding.sp.editText?.text.toString()
            val quantityText = binding.stock.editText?.text.toString()

            try {
                val cp = cpText.toInt()
                val quantity = quantityText.toInt()
                val currentDate = LocalDate.now().toString()

                val purchaseItem:InventoryTransaction = InventoryTransaction(currentDate,cp, quantity,"Purchase")

                val userId = Firebase.auth.currentUser?.uid.toString()
                val itemUid = uid

                if (userId != null && itemUid != null) {
                    val itemDocRef = db.collection("USER")
                        .document(userId)
                        .collection("INVETORY")
                        .document(itemUid)

                    // Retrieve the current document to update the cost price
                    itemDocRef.get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val existingItem = document.toObject(Item::class.java)

                                // Update the cost price in the existing item
                                if (existingItem != null) {
                                    var prevcp = existingItem.ratecp
                                    var prevstock = existingItem.inStock
                                    existingItem.ratecp =
                                        ((prevstock!! * prevcp!!) + (cp * quantity)) / (prevstock + quantity)
                                    existingItem.inStock = prevstock + quantity
                                    // Save the updated item
                                    itemDocRef.set(existingItem)
                                        .addOnSuccessListener {
                                            // Add purchase item to PURCHASE subcollection
                                            itemDocRef.collection("TRANSACTIONS")
                                                .document()
                                                .set(purchaseItem)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Purchase item added and cost price updated",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(
                                                        context,
                                                        "Error adding purchase item: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                "Error updating cost price: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            } else {
                                Toast.makeText(context, "Item not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Error retrieving item: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(context, "User or item ID not found", Toast.LENGTH_SHORT).show()
                }

            } catch (e: NumberFormatException) {
                Toast.makeText(
                    context,
                    "Please enter valid numbers for cost and quantity",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        //recyclerview
        val transactions = listOf(
            InventoryTransaction(date = "2024-11-01", price = 100, quantity = 10, transactionType = "purchase"),
            InventoryTransaction(date = "2024-11-02", price = 120, quantity = -5, transactionType = "sale"),
            InventoryTransaction(date = "2024-11-03", price = 150, quantity = 20, transactionType = "purchase"),
            InventoryTransaction(date = "2024-11-04", price = 130, quantity = -7, transactionType = "sale"),
            InventoryTransaction(date = "2024-11-05", price = 200, quantity = 15, transactionType = "purchase"),
            InventoryTransaction(date = "2024-11-06", price = 180, quantity = -10, transactionType = "sale"),
            InventoryTransaction(date = "2024-11-07", price = 220, quantity = 30, transactionType = "purchase"),
            InventoryTransaction(date = "2024-11-08", price = 160, quantity = -8, transactionType = "sale"),
            InventoryTransaction(date = "2024-11-09", price = 250, quantity = 25, transactionType = "purchase"),
            InventoryTransaction(date = "2024-11-10", price = 170, quantity = -12, transactionType = "sale")
        )

        val userId = Firebase.auth.currentUser?.uid.toString()
        if (userId == null || uid == null) {
            Toast.makeText(this, "User ID or Item UID not found", Toast.LENGTH_SHORT).show()
            return // Exit if user or item ID is null
        }

        val purchaseItemlist: MutableList<InventoryTransaction> = mutableListOf()
         padapter = PurchaseItemAdapter(purchaseItemlist)

        binding.purchaserecyclerview.apply {
            layoutManager = LinearLayoutManager(this@UpdateItemActivity)
            adapter = padapter
        }

// Fetch data from Firestore
        db.collection("USER")
            .document(userId)
            .collection("INVETORY")
            .document(uid)
            .collection("TRANSACTIONS")
            .addSnapshotListener { documents, exception ->
                if (exception != null) {
                    Log.e("Firestore", "Error retrieving purchases", exception)
                    Toast.makeText(this, "Error retrieving purchases: ${exception.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (documents != null) {
                    purchaseItemlist.clear()
                    for (document in documents) {
                        try {
                            // Attempt to convert the document to a PurchaseItem object
                            val purchaseItem = document.toObject(InventoryTransaction::class.java)

                            // Add to the list if no exception occurs
                            purchaseItemlist.add(purchaseItem)
                            Log.d("Firestore", "Transaction added: $purchaseItem")
                        } catch (e: Exception) {
                            // Log the exception and document ID if the conversion fails
                            Log.e("FirestoreError", "Error parsing document ${document.id}: ${e.message}")
                        } // Add each PurchaseItem to the list
                    }
                }
                padapter.notifyDataSetChanged() // Notify adapter of data changes
            }



    }
}
