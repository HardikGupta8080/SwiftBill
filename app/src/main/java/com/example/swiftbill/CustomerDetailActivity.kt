package com.example.swiftbill

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swiftbill.Adapter.BillPaymentAdapter
import com.example.swiftbill.databinding.ActivityCustomerDetailBinding
import com.example.swiftbill.model.Billdata
import com.example.swiftbill.model.CustomerId
import com.example.swiftbill.model.ListItem
import com.example.swiftbill.model.Payments


class CustomerDetailActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityCustomerDetailBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: BillPaymentAdapter
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
        val customer:CustomerId = intent.getParcelableExtra("CUSTOMER_DATA")!!
        binding.name.setText(customer.CustomerName.toString())
        binding.contact.setText(customer.contactNumber.toString())
        binding.amt.setText(customer.openingBal.toString())
        var BillList: MutableList<Billdata>
        BillList=customer.billlist!!

        val listItem = BillList.map { ListItem.BillItem(it) }
        val paymentList = mutableListOf<Payments>() // Assuming you have a list of payments

// Create ListItem.PaymentItem objects and combine them with ListItem.BillItem


        binding.addsale.setOnClickListener {
            startActivity(Intent(this,AddsaleActivity::class.java))
        }


        val combinedListItems = listItem + paymentList.map { ListItem.PaymentItem(it) }

// Now pass combinedListItems to the adapter
        if (combinedListItems.isEmpty()) {
            // Show Toast
            Toast.makeText(this, "No items to display", Toast.LENGTH_SHORT).show()
        } else {
            // Set adapter only if list is not empty
            adapter = BillPaymentAdapter(combinedListItems)
            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(this@CustomerDetailActivity)
                adapter = this@CustomerDetailActivity.adapter
            }
        }


    }
}

