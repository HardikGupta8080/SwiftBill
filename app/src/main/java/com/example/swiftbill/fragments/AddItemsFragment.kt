package com.example.swiftbill.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        navController = findNavController() // Initialize NavController

        binding.add.setOnClickListener {
            val product = Item().apply {
                productname = binding.itemname.editText?.text.toString()?.trim()
                val costPriceString = binding.costprice.editText?.text.toString()?.trim()
                val sellPriceString = binding.sellprice.editText?.text.toString()?.trim()
                val stockstring = binding.stock.editText?.text.toString()?.trim()
                ratecp = costPriceString!!.toIntOrNull() ?: 0
                ratesp = sellPriceString!!.toIntOrNull() ?: 0
                inStock = stockstring!!.toIntOrNull() ?:0
                uidcode= UUID.randomUUID().toString()

            }


            if (product.productname.isNullOrEmpty() || product.ratecp!! <= 0 || product.ratesp!! <= 0) {

                binding.itemname.error = if (product.productname.isNullOrEmpty()) "Please enter a product name" else null
                binding.costprice.error = if (product.ratecp!! <= 0) "Please enter a valid cost price" else null
                binding.sellprice.error = if (product.ratesp!! <= 0) "Please enter a valid sell price" else null
            } else {
                // Proceed with adding the item to the database
                val userid=Firebase.auth.currentUser?.uid.toString()
                db.collection("USER").document(userid).collection("INVETORY")
                    .whereEqualTo("productname", product.productname)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            // No item with the same name found, add the new item

                            db.collection("USER").document(userid).collection("INVETORY").document(product.uidcode.toString())
                                .set(product)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Item added successfully", Toast.LENGTH_SHORT).show()
                                    navController.navigateUp() // Navigate back
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Error adding item: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // Item with the same name already exists
                            Toast.makeText(requireContext(), "Item with the same name already exists", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error checking item: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
