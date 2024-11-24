package com.example.swiftbill.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.Adapter.BillAdapter
import com.example.swiftbill.Activity.AddsaleActivity
import com.example.swiftbill.Activity.PartyActivity
import com.example.swiftbill.R
import com.example.swiftbill.databinding.FragmentHomeBinding
import com.example.swiftbill.model.Billdata
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class HomeFragment : Fragment() {
    private lateinit var billList: MutableList<Billdata>
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
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
        syncMissingBillsFromFirebase()
        // Call this method when your app starts or when needed
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        // Initialize the billList
        billList = mutableListOf()

        // Set up RecyclerView
        binding.recyclerView2.apply {
            layoutManager = LinearLayoutManager(requireContext())
            billAdapter = BillAdapter(billList,requireContext())
            adapter = billAdapter

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

        // Fetch bills and set up the adapter
        fetchBills()

        // Creating a bill
        binding.sale.setOnClickListener {
            startActivity(Intent(requireContext(), AddsaleActivity::class.java))
        }

        // Set up button click listeners
        binding.invetory.setOnClickListener {
            // Handle inventory button click
                navController.navigate(R.id.action_home_to_items)

        }
        binding.party.setOnClickListener {
            // Handle party button click
            startActivity(Intent(requireContext(), PartyActivity::class.java))

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

    private fun fetchBills() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        db.collection("USER").document(currentUserUid).collection("BILL")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    billList.clear()
                    for (document in snapshot.documents) {
                        document.toObject(Billdata::class.java)?.let { billList.add(it) }
                    }
                    // Notify the adapter about data changes
                    billList.sortByDescending { it.billId }

                    billAdapter.notifyDataSetChanged()
                }
            }
    }
    fun syncMissingBillsFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storage = FirebaseStorage.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val localDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Bills")

        if (!localDirectory.exists()) {
            localDirectory.mkdirs() // Create the directory if it doesn't exist
        }

        // Query Firestore to get the list of bills
        firestore.collection("USER").document(userId).collection("pdfs")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val pdfUrl = document.getString("pdfUrl") ?: continue
                    val fileName = document.id // Assuming document ID matches "Bill_<billId>.pdf" format
                    val localFile = File(localDirectory, fileName)

                    // Check if the file is already downloaded
                    if (!localFile.exists()) {
                        // If file is missing, download it
                        downloadPdfFromFirebase(storage.getReferenceFromUrl(pdfUrl), localFile)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("SyncBills", "Error fetching documents: ", e)
            }
    }

    private fun downloadPdfFromFirebase(storageRef: StorageReference, destinationFile: File) {
        storageRef.getFile(destinationFile)

            .addOnFailureListener { e ->
                Log.e("DownloadPDF", "Error downloading file: ", e)
            }
    }




}
