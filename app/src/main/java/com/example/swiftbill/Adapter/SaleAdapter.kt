package com.example.swiftbill.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.R
import com.example.swiftbill.model.Billdata
import com.example.swiftbill.model.CustomerId

class SaleAdapter(private val costumer:MutableList<Billdata>):
RecyclerView.Adapter<SaleAdapter.SaleViewHolder>(){
    class SaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val InvoiceNo: TextView = itemView.findViewById(R.id.invoiceNo)
        val Date: TextView = itemView.findViewById(R.id.date)
        val Total: TextView = itemView.findViewById(R.id.total)
        val balance: TextView = itemView.findViewById(R.id.balance)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill,parent,false)
        return SaleViewHolder(view)
    }

    override fun getItemCount(): Int {
        return costumer.size
    }
    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
       val sale = costumer[position]
        holder.InvoiceNo.text="INOVICE NO :#"+sale.billId.toString()
        holder.Date.text="DATE :"+sale.date
        holder.Total.text=sale.totalAmount.toString()
        holder.balance.text=sale.bal.toString()
    }
}