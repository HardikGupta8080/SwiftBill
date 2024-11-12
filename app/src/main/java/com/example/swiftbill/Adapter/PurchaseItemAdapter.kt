package com.example.swiftbill.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.R
import com.example.swiftbill.model.PurchaseItem

class PurchaseItemAdapter(
    private val purchaseItemList: List<PurchaseItem>
) : RecyclerView.Adapter<PurchaseItemAdapter.ViewHolder>() {

    // ViewHolder class to represent each item view
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.datepurchase)
        val cp: TextView = view.findViewById(R.id.cppurchase)
        val stock: TextView = view.findViewById(R.id.quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.purchase_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return purchaseItemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val purchaseData = purchaseItemList[position]
        holder.date.text = purchaseData.date ?: "N/A"
        holder.cp.text = purchaseData.cp?.toString() ?: "0"
        holder.stock.text = purchaseData.quantity?.toString() ?: "0"

    }
}
