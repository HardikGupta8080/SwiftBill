package com.example.swiftbill.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.Adapter.SaleAdapter
import com.example.swiftbill.R
import com.example.swiftbill.model.CustomerId

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SaleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SaleFragment : Fragment() {
    private lateinit var customer:CustomerId
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customer = requireArguments().getParcelable("CUSTOMER_DATA")
            ?: throw IllegalArgumentException("Customer data is required")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sale, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSale)
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = SaleAdapter(customer.billlist!!)
        return view
    }
    companion object {
        fun newInstance(customer: CustomerId): SaleFragment {
            val fragment = SaleFragment()
            val args = Bundle().apply {
                putParcelable("CUSTOMER_DATA", customer)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
