package com.example.swiftbill.Activity


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.swiftbill.Adapter.PaymentAdapter
import com.example.swiftbill.Adapter.ViewPagerAdapter
import com.example.swiftbill.R
import com.example.swiftbill.databinding.ActivityCustomerDetailBinding
import com.example.swiftbill.fragments.SaleFragment
import com.example.swiftbill.model.Billdata
import com.example.swiftbill.model.CustomerId
import com.example.swiftbill.model.ListItem
import com.example.swiftbill.model.Payments
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class CustomerDetailActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityCustomerDetailBinding.inflate(layoutInflater)
    }
    private lateinit var customer:CustomerId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)
        //Setup for Toolbar
        setSupportActionBar(binding.party)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.party.setNavigationOnClickListener { finish() }
        val intent = intent
         customer = intent.getParcelableExtra("CUSTOMER_DATA")!!
        fetchCustomerData()
        var BillList: MutableList<Billdata>
        BillList=customer.billlist!!

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        // Set up ViewPager2 with adapter
        val adapter = ViewPagerAdapter(this,customer)
        viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Sales"
                1 -> "Payments"
                else -> null
            }
        }.attach()

        val listItem = BillList.map { ListItem.BillItem(it) }
        val paymentList = mutableListOf<Payments>() // Assuming you have a list of payments

// Create ListItem.PaymentItem objects and combine them with ListItem.BillItem


        binding.addsale.setOnClickListener {
            startActivity(Intent(this, AddsaleActivity::class.java))
        }
        binding.makepayment.setOnClickListener {
            Toast.makeText(this, "No items to display", Toast.LENGTH_SHORT).show()
            showInputDialog()
        }


        val combinedListItems = listItem + paymentList.map { ListItem.PaymentItem(it) }

// Now pass combinedListItems to the adapter


    }
    fun fetchCustomerData() {
        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) {
            // Handle unauthenticated state
            throw IllegalStateException("User not authenticated")
        }

        FirebaseFirestore.getInstance().collection("USER")
            .document(userId)
            .collection("CUSTOMER") // Fix potential typo here
            .document(customer.contactNumber.toString())
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    // Log or handle the error
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val name = documentSnapshot.getString("customerName") ?: "N/A"
                    val contact = customer.contactNumber.toString() // Assuming it's passed correctly
                    val openingBalance = documentSnapshot.getLong("openingBal") ?: 0.0

                    // Update UI with fetched data
                    binding.name.setText(name)
                    binding.contact.setText(contact)
                    binding.amt.setText(openingBalance.toString())
                } else {
                    // Handle document not existing
                    binding.name.setText("N/A")
                    binding.contact.setText("N/A")
                    binding.amt.setText("0.0")
                }
            }
    }

    private fun showInputDialog() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.make_payment, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dilogtitle)
        val dateview = dialogView.findViewById<TextView>(R.id.date)
        val idview = dialogView.findViewById<TextView>(R.id.id)
        val inputField = dialogView.findViewById<EditText>(R.id.inputField)  // EditText for user input
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)  // Button to handle cancel action
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

        // Set the date to today's date
        dateview.setText(getTodayDate())

        // Fetch the previous payment ID asynchronously
        getPreviousPaymentId { previousPaymentId ->
            // Increment the previous payment ID
            val newPaymentId = if (previousPaymentId == "0") {
                1
            } else {
                previousPaymentId.toInt() + 1
            }
            // Update the ID TextView with the new payment ID
            idview.setText(newPaymentId.toString())
        }

        // Create the dialog and display it
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        // Cancel button action
        btnCancel.setOnClickListener {
            dialog.dismiss()  // Dismiss the dialog
        }

        // Submit button action
        btnSubmit.setOnClickListener {
            val id = idview.text.toString()  // Get the payment ID from the TextView
            val date = dateview.text.toString()  // Get the date from the TextView
            val amount = inputField.text.toString().toInt()  // Get the amount from the input field

            // Create a Payments object
            val payments = Payments(id.toInt(), amount, date)

            // Save the payment data to Firestore

            val db = FirebaseFirestore.getInstance()
                .collection("USER")
                .document(Firebase.auth.currentUser?.uid.toString())
            db.collection("PAYMENTS")
                .document(id).set(payments)
                db.collection("CUSTOMER")
                .document(customer.contactNumber.toString())
                .collection("PAYMENT")
                .document(id)
                .set(payments)
                .addOnSuccessListener {
                    updatebalance(payments.amountPaid!!)
                    Toast.makeText(this, "Payment Saved", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()  // Optionally dismiss the dialog after successful save
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error saving payment: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Show the dialog
        dialog.show()
    }


    fun getTodayDate(): String {
        // Get the current date
        val today = LocalDate.now()

        // Format the date (optional)
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy") // Change format as needed
        val formattedDate = today.format(formatter)

        return formattedDate
    }
    fun getPreviousPaymentId(callback: (String) -> Unit) {
        // Reference to the PAYMENTS collection
        val db = FirebaseFirestore.getInstance()
            .collection("USER")
            .document(Firebase.auth.currentUser?.uid.toString()) // Current user's document
            .collection("PAYMENTS")


        // Query to get the latest payment, with secondary sorting by paymentId
        // Primary sort by paymentDate
            db.orderBy("paymentId", Query.Direction.DESCENDING) // Secondary sort by paymentId
            .limit(1) // Limit to the most recent record
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Get the document ID of the most recent payment
                    val latestPaymentId = querySnapshot.documents[0].id
                    callback(latestPaymentId) // Return the payment ID
                } else {
                    // No payment exists, return "0"
                    callback("0")
                }
            }
            .addOnFailureListener { exception ->
                // Handle query failure
                exception.printStackTrace()
                callback("0") // Return "0" on failure
            }
    }
    fun updatebalance(paymentAmount:Int){
        val db=FirebaseFirestore.getInstance()
            .collection("USER")
            .document(Firebase.auth.currentUser?.uid.toString())
            .collection("CUSTOMER")
            .document(customer.contactNumber.toString())
        db.get().addOnSuccessListener {it->
            if(it.exists())
            {
                val currentBalance=it.getLong("openingBal")?:0
                val updatedBalance = currentBalance - paymentAmount
                db.update("openingBal",updatedBalance).addOnSuccessListener {  }
            }
        }

    }




}

