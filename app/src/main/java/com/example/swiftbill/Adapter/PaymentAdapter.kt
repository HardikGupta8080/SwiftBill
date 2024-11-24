package com.example.swiftbill.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.R

import com.example.swiftbill.model.Payments

class PaymentAdapter(private val payment:MutableList<Payments>):
    RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    class PaymentViewHolder(itemView:View) :RecyclerView.ViewHolder(itemView){
        val date:TextView=itemView.findViewById(R.id.date)
        val amount:TextView=itemView.findViewById(R.id.totalp)
        val id:TextView=itemView.findViewById(R.id.paymentid)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment,parent, false)
        return PaymentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return payment.size
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val item=payment[position]
        holder.amount.text=item.amountPaid.toString()
        holder.date.text="DATE :"+item.paymentDate
        holder.id.text="PAYMENT ID :"+item.paymentId.toString()
    }


}