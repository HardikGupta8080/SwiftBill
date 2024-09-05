package com.example.swiftbill



import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter

import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swiftbill.databinding.ActivityAddsaleBinding
import com.example.swiftbill.fragments.Adapter.Bill_Item_Adapter
import com.example.swiftbill.model.BillItem
import com.example.swiftbill.model.CustomerId
import com.example.swiftbill.model.Item
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AddsaleActivity : AppCompatActivity() {
    private lateinit var billItemAdapter: Bill_Item_Adapter
    private val db = FirebaseFirestore.getInstance()
    private val binding by lazy {
        ActivityAddsaleBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = getColor(R.color.blue)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Switch logic
        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.switch1.text = "Cash"
                binding.Coustmername.editText?.hint = "Customer Name (Optional)"
            } else {
                binding.switch1.text = "Credit"
                binding.Coustmername.editText?.hint = "Customer Name"
            }
        }
        var itemlist:MutableList<Item> = mutableListOf()
        val itemListener = db.collection(Firebase.auth.currentUser?.uid.toString())
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    itemlist.clear()
                    for (document in snapshots.documents) {
                        val item: Item? = document.toObject(Item::class.java)
                        if (item != null) {
                            itemlist.add(item)
                        }
                    }

                    // Setup AutoCompleteTextView after itemlist is populated
                    val autoCompleteTextView = binding.autoCompleteItems
                    val productNames = itemlist.map { item -> item.productname }.filterNotNull()
                    if (productNames.isNotEmpty()) {
                        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, productNames)
                        autoCompleteTextView.setAdapter(adapter)
                        autoCompleteTextView.threshold = 1 // Start showing suggestions after 1 character
                    } else {
                        Toast.makeText(this, "No items available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle the case when there are no snapshots
                    Log.d("TAG", "No snapshots found.")
                    Toast.makeText(this, "No items found", Toast.LENGTH_SHORT).show()
                }
            }

        // Setup RecyclerView with initial data
        val initialBillItems = mutableListOf(
            BillItem(productname = "hv", sp = 100, discount = 55, quantity = 100),
            BillItem(productname = "hv", sp = 100, discount = 55, quantity = 100)
        )
        billItemAdapter = Bill_Item_Adapter(initialBillItems)
        binding.recyclerViewItems.apply {
            layoutManager = LinearLayoutManager(this@AddsaleActivity)
            adapter = billItemAdapter
        }

        // Save customer details to Firestore
        binding.save.setOnClickListener {
            val customer = CustomerId().apply {
                CustomerName = binding.Coustmername.editText?.text.toString()
                contactNumber = binding.Contactno.editText?.text.toString()
            }

            db.collection("Customer" + Firebase.auth.currentUser?.uid.toString()).document()
                .set(customer)
                .addOnSuccessListener {
                    Toast.makeText(this, "Customer details saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save customer details", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
