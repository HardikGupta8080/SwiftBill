package com.example.swiftbill.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.R
import com.example.swiftbill.model.Billdata
import com.example.swiftbill.model.ListItem
import com.example.swiftbill.model.Payments

class BillPaymentAdapter(private val items: List<ListItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_BILL = 0
        private const val TYPE_PAYMENT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.BillItem -> TYPE_BILL
            is ListItem.PaymentItem -> TYPE_PAYMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Check if the list is empty
        if (items.isEmpty()) {
            // Show Toast message
            Toast.makeText(parent.context, "No items to display", Toast.LENGTH_SHORT).show()

            // Return an empty ViewHolder to avoid any crash; can just return a basic View
            return object : RecyclerView.ViewHolder(View(parent.context)) {

            }
        } else {
            // Proceed with the usual ViewHolder creation based on view type
            return when (viewType) {
                TYPE_BILL -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_bill, parent, false)
                    BillViewHolder(view)
                }

                TYPE_PAYMENT -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_payment, parent, false)
                    PaymentViewHolder(view)
                }

                else -> throw IllegalArgumentException("Invalid view type")
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BillViewHolder -> holder.bind((items[position] as ListItem.BillItem).bill)
            is PaymentViewHolder -> holder.bind((items[position] as ListItem.PaymentItem).payment)
        }
    }

    override fun getItemCount(): Int = items.size

    class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val billIdTextView: TextView = itemView.findViewById(R.id.invoiceNo)
        private val totalAmountTextView: TextView = itemView.findViewById(R.id.total)
        private val invoicenotextview: TextView = itemView.findViewById(R.id.invoiceNo)
       private val date:TextView = itemView.findViewById(R.id.date)
       private val total:TextView = itemView.findViewById(R.id.total)
        private val balance :TextView = itemView.findViewById(R.id.balance)
        fun bind(bill: Billdata) {
            billIdTextView.text = bill.billId?.toString() ?: "N/A"
            totalAmountTextView.text = bill.totalAmount?.toString() ?: "0"
            invoicenotextview.text = "Invoice No #"+bill.billId?.toString() ?: "N/A"
           date.text = "Date :"+bill.date ?: "Unknown Date"
            total.text = bill.totalAmount?.toString() ?: "0"

            val totalAmount = bill.totalAmount ?: 0
            val amountPaid = bill.amountpaid ?: 0
            balance.text = (totalAmount-amountPaid).toString()
        }
    }

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val paymentIdTextView: TextView = itemView.findViewById(R.id.paymentid)
        private val amountPaidTextView: TextView = itemView.findViewById(R.id.totalp)

        fun bind(payment: Payments) {
            paymentIdTextView.text = payment.paymentId.toString()
            amountPaidTextView.text = payment.amountPaid.toString()
        }
    }
}
