package com.example.swiftbill.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.AddsaleActivity
import com.example.swiftbill.R
import com.example.swiftbill.databinding.ActivityAddsaleBinding
import com.example.swiftbill.databinding.DialogAddItemBinding
import com.example.swiftbill.model.BillItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.NonDisposableHandle
import kotlinx.coroutines.NonDisposableHandle.parent

class Bill_Item_Adapter(
    private val context: Context,
    private var itemlist: MutableList<BillItem> = mutableListOf(),
    private val onUpdateClick: (BillItem, Int) -> Unit,
    private val parentView: View // Pass the parent view for the Snackbar
) : RecyclerView.Adapter<Bill_Item_Adapter.BillVewHolder>() {

    inner class BillVewHolder(BillItem: View) : RecyclerView.ViewHolder(BillItem) {

        val productname: TextView = BillItem.findViewById(R.id.productname)
        val sp: TextView = BillItem.findViewById(R.id.sp)
        val discount: TextView = BillItem.findViewById(R.id.discount)
        val quantity: TextView = BillItem.findViewById(R.id.quantity)
        val delete: ImageView = BillItem.findViewById(R.id.delete)
        val update: ImageView = BillItem.findViewById(R.id.update)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillVewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.bill_item_layout, parent, false)
        return BillVewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemlist.size
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onBindViewHolder(holder: BillVewHolder, position: Int) {
        val item = itemlist[position]
        holder.productname.text = item.productname
        holder.discount.text = item.discount.toString()
        holder.sp.text = item.sp.toString()
        holder.quantity.text = item.quantity.toString()


        holder.delete.setOnClickListener {
            deleteItem(position)
            (context as AddsaleActivity).updateTotalAmount()
            // Call the delete function
        }

        holder.update.setOnClickListener {
            onUpdateClick(item, position)
        }
    }

    // Function to delete an item and show Snackbar with Undo
    private fun deleteItem(position: Int) {
        val deletedItem = itemlist[position]
        itemlist.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemlist.size)


        Snackbar.make(parentView, "${deletedItem.productname} deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                itemlist.add(position, deletedItem)
                notifyItemInserted(position)
                notifyItemRangeChanged(position, itemlist.size)
                (context as AddsaleActivity).updateTotalAmount()
            }.show()
    }

    // Function to add new items
    fun addItem(billItem: BillItem) {
        itemlist.add(billItem)
        notifyItemInserted(itemlist.size - 1)
        (context as AddsaleActivity).updateTotalAmount()
    }

    // Function to update an item
    fun updateItem(billItem: BillItem, position: Int) {
        itemlist[position] = billItem
        notifyItemChanged(position)
        (context as AddsaleActivity).updateTotalAmount()
    }
    fun getItems(): List<BillItem> {
        return itemlist
    }
}
