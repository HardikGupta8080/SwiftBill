package com.example.swiftbill.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.R

import com.example.swiftbill.model.Billdata


class BillAdapter(
    private var billist: MutableList<Billdata> = mutableListOf()
): RecyclerView.Adapter<BillAdapter.BillViewHolder>() {


    inner class BillViewHolder(Billview: View) : RecyclerView.ViewHolder(Billview) {
        val nameofcoustmer: TextView = Billview.findViewById(R.id.nameofcoustumer)
        val total: TextView = Billview.findViewById(R.id.total)
        val date: TextView = Billview.findViewById(R.id.date)
        val invoice: TextView = Billview.findViewById(R.id.invoiceno)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bill_layout, parent, false)
        return BillViewHolder(view)
    }

    override fun getItemCount(): Int {
        return billist.size
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = billist[position]
        holder.nameofcoustmer.text=bill.customerName
        holder.invoice.text="${bill.billId}"
        holder.total.text="${bill.totalAmount}"
        holder.date.text=bill.date
    }
}