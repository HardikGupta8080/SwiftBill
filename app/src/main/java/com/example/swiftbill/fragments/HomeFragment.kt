package com.example.swiftbill.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.AddsaleActivity
import com.example.swiftbill.databinding.FragmentHomeBinding
import com.example.swiftbill.fragments.Adapter.BillAdapter
import com.example.swiftbill.model.Billdata
import com.example.swiftbill.model.FadeInItemAnimator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.time.LocalDate
import java.util.Collections.reverse

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var db = FirebaseFirestore.getInstance()
    private lateinit var navController: NavController
    private lateinit var billAdapter: BillAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val typingDelay = 100L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fetchAndAnimateText()
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        // Initialize the Billdata object


        // Set up RecyclerView
        billAdapter = BillAdapter(generateRandomBills())
        binding.recyclerView2.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = billAdapter

            // Add item animator for the fade effect
            itemAnimator = FadeInItemAnimator()

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy > 0 && binding.sale.isShown) {
                        binding.sale.animate()
                            .translationY(binding.sale.height.toFloat())
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction { binding.sale.visibility = View.GONE }
                    } else if (dy < 0 && !binding.sale.isShown) {
                        binding.sale.visibility = View.VISIBLE
                        binding.sale.animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(300)
                    }
                }
            })
        }


        //creating a bill
        binding.sale.setOnClickListener {
            startActivity(Intent(requireContext(), AddsaleActivity::class.java))
        }

        // Set up button click listeners
        binding.invetory.setOnClickListener {
            // Handle inventory button click
        }
        binding.party.setOnClickListener {
            // Handle party button click
        }
        binding.debt.setOnClickListener {
            // Handle debt button click
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchAndAnimateText() {
        val db = FirebaseFirestore.getInstance()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        db.collection("USER").document(currentUserUid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val name = documentSnapshot.getString("name") ?: "Name not available"
                    typeText(name)
                } else {
                    binding.nameuser.text = "No such document"
                }
            }
            .addOnFailureListener { exception ->
                binding.nameuser.text = "Error fetching data: ${exception.message}"
            }
    }

    private fun typeText(text: String) {
        val textView: TextView = binding.nameuser
        val length = text.length
        var index = 0

        val runnable = object : Runnable {
            override fun run() {
                val end = (index + 1).coerceAtMost(length)
                textView.text = text.substring(0, end)
                index = end

                if (index < length) {
                    handler.postDelayed(this, typingDelay)
                }
            }
        }
        handler.post(runnable)
    }

    private fun generateRandomBills(): MutableList<Billdata> {
        val billlist: MutableList<Billdata> = mutableListOf()
        val names = listOf(
            "Alice", "Bob", "Charlie", "David", "Eva", "Frank", "Grace", "Henry", "Ivy", "Jack",
            "Kara", "Liam", "Mona", "Nina", "Oscar", "Paul", "Quinn", "Rita", "Sam", "Tina"
        )

        for (i in 1..20) {
            val randomName = names.random()
            val randomAmount = (500..5000).random()
            val randomDate = LocalDate.now().minusDays((0..365).random().toLong()).toString()
            var bill = Billdata();
            bill.billId = i
            bill.customerName = randomName
            bill.totalAmount = randomAmount
            bill.date = randomDate

            billlist.add(bill)
        }

        reverse(billlist)
        return billlist
    }
}

