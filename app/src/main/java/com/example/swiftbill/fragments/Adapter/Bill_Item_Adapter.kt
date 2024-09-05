package com.example.swiftbill.fragments.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.R
import com.example.swiftbill.model.BillItem

class Bill_Item_Adapter(
    private var itemlist: MutableList<BillItem> = mutableListOf()
): RecyclerView.Adapter<Bill_Item_Adapter.BillVewHolder>() {

    inner class BillVewHolder (BillItem:View):RecyclerView.ViewHolder(BillItem){
        val productname: TextView = BillItem.findViewById(R.id.productname)
        val sp: TextView = BillItem.findViewById(R.id.sp)
        val discount: TextView = BillItem.findViewById(R.id.discount)
        val quantity: TextView = BillItem.findViewById(R.id.quantity)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillVewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bill_item_layout, parent, false)
        return BillVewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemlist.size
    }

    override fun onBindViewHolder(holder: BillVewHolder, position: Int) {
        val item = itemlist[position]
        holder.productname.text=item.productname
        holder.discount.text=item.discount.toString()
        holder.sp.text=item.sp.toString()
        holder.quantity.text=item.quantity.toString()
    }
}