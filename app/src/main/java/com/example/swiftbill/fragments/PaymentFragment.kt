package com.example.swiftbill.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.Adapter.PaymentAdapter
import com.example.swiftbill.R
import com.example.swiftbill.model.CustomerId
import com.example.swiftbill.model.Payments
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject

/**
 * A simple [Fragment] subclass.
 * Use the [PaymentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PaymentFragment : Fragment() {
    private var payments: MutableList<Payments> = mutableListOf()
    private lateinit var customer:CustomerId
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customer = requireArguments().getParcelable("CUSTOMER_DATA")
            ?: throw IllegalArgumentException("Customer data is required")
    }

    companion object {
        fun newInstance(customer: CustomerId): PaymentFragment {
            val fragment = PaymentFragment()
            val args = Bundle().apply {
                putParcelable("CUSTOMER_DATA", customer)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fetchPayment()
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPayment)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = PaymentAdapter(payments)
        return view
    }
    private var paymentsListener: ListenerRegistration? = null

    fun fetchPayment() {
        // Remove any previous listener if it exists to avoid duplicates
        paymentsListener?.remove()

        // Attach a real-time listener
        paymentsListener = FirebaseFirestore.getInstance()
            .collection("USER")
            .document(Firebase.auth.currentUser?.uid.toString())
            .collection("CUSTOMER")
            .document(customer.contactNumber.toString())
            .collection("PAYMENT")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    // Log or handle the error
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    payments.clear()
                    for (document in querySnapshot.documents) {
                        val payment = document.toObject(Payments::class.java)
                        payment?.let { payments.add(it) }
                    }
                    // Notify the adapter of the data changes
                    (view?.findViewById<RecyclerView>(R.id.recyclerViewPayment)?.adapter as? PaymentAdapter)?.notifyDataSetChanged()
                }
            }
    }

}