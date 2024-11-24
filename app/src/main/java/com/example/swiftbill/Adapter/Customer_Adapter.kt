package com.example.swiftbill.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.Activity.PartyActivity
import com.example.swiftbill.R
import com.example.swiftbill.model.CustomerId

class Customer_Adapter(
    private var customerlist: MutableList<CustomerId> = mutableListOf(),
    private val listener: PartyActivity // Update here
) : RecyclerView.Adapter<Customer_Adapter.CustomerViewHolder>() {
    interface OnCustomerClickListener {
        fun onCustomerClick(customerId: CustomerId)
    }
    inner class CustomerViewHolder(CustomerView: View) : RecyclerView.ViewHolder(CustomerView) {
        val nameofcoustmer: TextView = CustomerView.findViewById(R.id.customername)
        val balance: TextView = CustomerView.findViewById(R.id.balance)
        val date: TextView = CustomerView.findViewById(R.id.date_customer)

        init {
            CustomerView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedItem = customerlist[position]
                    listener.onCustomerClick(clickedItem) // Notify listener
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.customer_layout, parent, false)
        return CustomerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return customerlist.size
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customerlist[position]
        holder.nameofcoustmer.text = customer.CustomerName
        holder.date.text = customer.contactNumber
        holder.balance.text = customer.openingBal.toString()
    }

    fun updateList(filteredList: MutableList<CustomerId>) {
        customerlist = filteredList
        notifyDataSetChanged()
    }
}
