package com.example.swiftbill.Activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swiftbill.Adapter.Customer_Adapter
import com.example.swiftbill.databinding.ActivityPartyBinding
import com.example.swiftbill.model.CustomerId
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.databinding.DialogAddCustomerBinding

class PartyActivity : AppCompatActivity(), Customer_Adapter.OnCustomerClickListener {
    private val binding by lazy {
        ActivityPartyBinding.inflate(layoutInflater)
    }
    private val db = FirebaseFirestore.getInstance()
    private val customerlist: MutableList<CustomerId> = mutableListOf()
    private lateinit var customerAdapter: Customer_Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.customer)
        binding.partyrecyclerview.visibility = View.VISIBLE
        binding.addcustomer.visibility = View.VISIBLE
        binding.searchView.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.customer.setNavigationOnClickListener { finish() }

        binding.addcustomer.setOnClickListener { showAddCustomerDialog() }
        setupSearchView()
        setupRecyclerView()
        fetchCustomerlist()
        setContentView(binding.root)
    }

    private fun showAddCustomerDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Customer")
        val dialogBinding = DialogAddCustomerBinding.inflate(LayoutInflater.from(this))
        builder.setView(dialogBinding.root)

        builder.setPositiveButton("Add") { dialog, which ->
            if (dialogBinding.Coustmername.editText?.text.isNullOrEmpty() ||
                dialogBinding.Contactno.editText?.text.isNullOrEmpty() ||
                dialogBinding.OpeningBalance.editText?.text.isNullOrEmpty()) {
                Toast.makeText(this, "Fill Details", Toast.LENGTH_SHORT).show()
            } else {
                val name = dialogBinding.Coustmername.editText?.text.toString().trim()
                val contact = dialogBinding.Contactno.editText?.text.toString().trim()
                val openingBalance = dialogBinding.OpeningBalance.editText?.text.toString().trim()
                val openingBalanceInt = openingBalance.toIntOrNull() ?: 0 // Default to 0 if null

                // Create the customer object
                val customer = CustomerId(name, contact, openingBalanceInt)
                val userId = Firebase.auth.currentUser?.uid.toString()

                db.collection("USER").document(userId).collection("CUSTOMER")
                    .document(contact).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            Toast.makeText(this, "Customer already exists", Toast.LENGTH_SHORT).show()
                        } else {
                            db.collection("USER").document(userId).collection("CUSTOMER")
                                .document(contact).set(customer)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Customer added successfully", Toast.LENGTH_SHORT).show()
                                    customerlist.add(customer) // Update the list
                                    customerAdapter.notifyItemInserted(customerlist.size - 1) // Notify adapter
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error adding customer: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error checking customer: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()
    }

    private fun setupSearchView() {
        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false
            binding.searchView.requestFocus()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("SearchView", "Text changed: $newText")
                filterItems(newText)
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        binding.partyrecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            customerAdapter = Customer_Adapter(customerlist, this@PartyActivity)
            adapter = customerAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0 && binding.addcustomer.isShown) {
                        binding.addcustomer.animate()
                            .translationY(binding.addcustomer.height.toFloat())
                            .alpha(0f).setDuration(300)
                            .withEndAction { binding.addcustomer.visibility = View.GONE }
                    } else if (dy < 0 && !binding.addcustomer.isShown) {
                        binding.addcustomer.visibility = View.VISIBLE
                        binding.addcustomer.animate().translationY(0f).alpha(1f).setDuration(300)
                    }
                }
            })
        }
    }

    private fun fetchCustomerlist() {
        db.collection("USER").document(Firebase.auth.currentUser?.uid.toString())
            .collection("CUSTOMER")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                } else {
                    snapshot?.let {
                        customerlist.clear()
                        for (document in snapshot.documents) {
                            document.toObject(CustomerId::class.java)?.let { customerlist.add(it) }
                        }
                        customerlist.sortBy { it.CustomerName }
                        customerAdapter.notifyDataSetChanged()
                    }
                }
            }
    }

    private fun filterItems(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            customerlist  // Show full list when query is empty
        } else {
            customerlist.filter {
                it.CustomerName?.contains(query, ignoreCase = true) ?:false ||
                        it.contactNumber?.contains(query, ignoreCase = true) ?: false
            }.toMutableList()
        }


        customerAdapter.updateList(filteredList)
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show() // Show feedback if no results
        }
    }



      override fun onCustomerClick(customer: CustomerId) {
          val intent = android.content.Intent(this, CustomerDetailActivity::class.java)
          intent.putExtra("CUSTOMER_DATA", customer) // Assuming CustomerId implements Parcelable
          startActivity(intent)
      }
}
