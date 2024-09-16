// Items.kt

package com.example.swiftbill.fragments
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftbill.fragments.Adapter.item_adapter
import com.example.swiftbill.R
import com.example.swiftbill.databinding.FragmentItemsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import androidx.appcompat.widget.SearchView
import com.example.swiftbill.AddsaleActivity

import com.example.swiftbill.UpdateItemActivity
import com.example.swiftbill.model.Billdata
import com.example.swiftbill.model.Item
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Items : Fragment(), item_adapter.OnItemClickListener {

    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemAdapter: item_adapter
    private var itemList: MutableList<Item> = mutableListOf()
    private lateinit var navController: NavController
    private val db = FirebaseFirestore.getInstance()
    private var itemListener: ListenerRegistration? = null
    // ActivityResultLauncher to receive result from UpdateItemActivity
    private val updateItemLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data
            val documentData = data?.getSerializableExtra("UNDO_DATA") as? Map<String, Any>
            val itemId = data?.getStringExtra("ITEM_ID")

            // Show the Snackbar with the Undo action in this fragment
            Snackbar.make(binding.root, "Item deleted", Snackbar.LENGTH_LONG).setAction("Undo") {
                // Re-add the document if Undo is clicked
                val db = FirebaseFirestore.getInstance()
                val documentRef = db.collection("USER").document(Firebase.auth.currentUser?.uid.toString())
                    .collection("INVENTORY").document(itemId.toString())

                documentRef.set(documentData ?: hashMapOf<String, Any>())
                    .addOnSuccessListener {
                        Log.d("Firestore", "DocumentSnapshot successfully restored!")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error restoring document", e)
                    }
            }.show()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentItemsBinding.inflate(inflater, container, false)


        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false
            binding.searchView.requestFocus()
        }


        // Set up search view
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterItems(newText)
                return true
            }
        })
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchItemsFromFirestore()

        navController = Navigation.findNavController(view)
        binding.myButton.setOnClickListener {
            navController.navigate(R.id.action_items_to_addItemsFragment)
        }
        // Initialize the adapter with the itemList and set the click listener
        itemAdapter = item_adapter(itemList, this)

        // Set up the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0 && binding.myButton.isShown) {
                        binding.myButton.animate()
                            .translationY(binding.myButton.height.toFloat())
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction { binding.myButton.visibility = View.GONE }
                    } else if (dy < 0 && !binding.myButton.isShown) {
                        binding.myButton.visibility = View.VISIBLE
                        binding.myButton.animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(300)
                    }
                }
            })
        }//for scrolling of button.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Remove the Firestore listener when the view is destroyed
        itemListener?.remove()
    }

    private fun fetchItemsFromFirestore() {
        itemListener = db.collection("USER").document(Firebase.auth.currentUser?.uid.toString()).collection("INVETORY")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    itemList.clear()
                    for (document in snapshots.documents) {
                        val item: Item? = document.toObject(Item::class.java)
                        if (item != null) {
                            itemList.add(item)
                        }
                    }


                    // Sort the items after fetching
                    itemList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.productname!! })

                    // Notify the adapter about data change
                    itemAdapter.notifyDataSetChanged()

                }
            }
    }

    private fun filterItems(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            itemList
        } else {
            itemList.filter {
                it.productname?.contains(query, ignoreCase = true) == true
            }.toMutableList()
        }
        itemAdapter.updateList(filteredList)
    }

    override fun onItemClick(item: Item) {
        // Create an Intent to start the UpdateItemActivity
        val intent = Intent(activity, UpdateItemActivity::class.java)
        intent.putExtra("ITEM_NAME", item.productname)
        intent.putExtra("CP",item.ratecp.toString())
        intent.putExtra("SP",item.ratesp.toString())
        intent.putExtra("STOCK",item.inStock.toString())// Pass the item's ID or any other necessary data
        intent.putExtra("UID",item.uidcode)
        startActivity(intent)

    }
}
