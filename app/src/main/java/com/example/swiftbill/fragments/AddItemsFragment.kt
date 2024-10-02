package com.example.swiftbill.fragments


import android.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.swiftbill.databinding.FragmentAddItemsBinding
import com.example.swiftbill.model.Item
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
class AddItemsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var navController: NavController
    private var _binding: FragmentAddItemsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        val categories = mutableListOf(
            "Electronics",
            "Clothing",
            "Groceries",
            "Furniture",
            "Toys",
            "Books",
            "Appliances",
            "Beauty & Personal Care",
            "Health & Wellness",
            "Home Decor",
            "Office Supplies",
            "Sports Equipment",
            "Stationery",
            "Automotive",
            "Jewelry & Accessories",
            "Footwear",
            "Pet Supplies",
            "Hardware & Tools",
            "Garden & Outdoor",
            "Kitchenware"
        )
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, categories)
        (binding.Catgory.editText as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            threshold = 1
            setOnItemClickListener { _, view, position, _ ->
                val selectedText = (view as TextView).text.toString()
                binding.Catgory.editText?.setText(selectedText)
            }
        }

        // Optional: Load categories from Firestore
        val userid = Firebase.auth.currentUser?.uid.toString()
        db.collection("USER").document(userid).collection("CATEGORIES")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val category = document.getString("categoryName")
                    if (category != null) {
                        categories.add(category)
                    }
                }
                adapter.notifyDataSetChanged()
            }

        binding.add.setOnClickListener {
            val product = Item().apply {
                productname = binding.itemname.editText?.text.toString().trim()
                val costPriceString = binding.costprice.editText?.text.toString().trim()
                val sellPriceString = binding.sellprice.editText?.text.toString().trim()
                val stockstring = binding.stock.editText?.text.toString().trim()
                ratecp = costPriceString?.toIntOrNull() ?: 0
                ratesp = sellPriceString?.toIntOrNull() ?: 0
                inStock = stockstring?.toIntOrNull() ?: 0
                uidcode = UUID.randomUUID().toString()

                // Capture the category
                category = binding.Catgory.editText?.text.toString().trim()
            }

            if (product.productname.isNullOrEmpty() || product.ratecp!! <= 0 || product.ratesp!! <= 0) {
                binding.itemname.error = if (product.productname.isNullOrEmpty()) "Please enter a product name" else null
                binding.costprice.error = if (product.ratecp!! <= 0) "Please enter a valid cost price" else null
                binding.sellprice.error = if (product.ratesp!! <= 0) "Please enter a valid sell price" else null
            } else {
                db.collection("USER").document(userid).collection("INVETORY")
                    .whereEqualTo("productname", product.productname)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            db.collection("USER").document(userid).collection("INVETORY")
                                .document(product.uidcode.toString())
                                .set(product)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Item added successfully", Toast.LENGTH_SHORT).show()
                                    navController.navigateUp()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Error adding item: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(requireContext(), "Item with the same name already exists", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error checking item: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                // Add new category if it's not in the list
                if (!categories.contains(product.category) && product.category!!.isNotEmpty()) {
                    categories.add(product.category!!)
                    adapter.notifyDataSetChanged()

                    val categoryData = hashMapOf("categoryName" to product.category)
                    db.collection("USER").document(userid).collection("CATEGORIES")
                        .document(product.category!!)
                        .set(categoryData)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
