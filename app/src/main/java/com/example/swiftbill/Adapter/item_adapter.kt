package com.example.swiftbill.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.R
import com.example.swiftbill.model.Item

class item_adapter(
    private var itemList: MutableList<Item> = mutableListOf(),
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<item_adapter.ItemViewHolder>() {

    // Define an interface for item clicks
    interface OnItemClickListener {
        fun onItemClick(item: Item)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewCp: TextView = itemView.findViewById(R.id.textViewAge)
        val textViewSp: TextView = itemView.findViewById(R.id.textViewDob)
        val stock: TextView = itemView.findViewById(R.id.instock)
        val category : TextView= itemView.findViewById(R.id.Catgory)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedItem = itemList[position]
                    listener.onItemClick(clickedItem)
                }
            }
        }
    }

    fun updateList(newItems: MutableList<Item> = mutableListOf()) {
        itemList = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.textViewName.text = item.productname
        holder.textViewCp.text = "${item.ratecp}"
        holder.textViewSp.text = "${item.ratesp}"
        holder.stock.text = "${item.inStock}"
        holder.category.text=item.category
    }

    override fun getItemCount()=itemList.size
}
