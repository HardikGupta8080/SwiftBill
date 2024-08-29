package com.example.swiftbill.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.swiftbill.LogIn_Activity
import com.example.swiftbill.R
import com.example.swiftbill.databinding.FragmentAddItemsBinding
import com.example.swiftbill.databinding.FragmentMenuBinding
import com.google.firebase.auth.FirebaseAuth


class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signout.setOnClickListener {
            // Show a confirmation dialog before signing out
            AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes") { dialog, which ->
                    // If the user confirms, proceed with sign out
                    val firebaseAuth = FirebaseAuth.getInstance()
                    firebaseAuth.signOut()

                    // Redirect to the login screen
                    val intent = Intent(requireContext(), LogIn_Activity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish() // Optional: Close the current activity
                }
                .setNegativeButton("No") { dialog, which ->
                    // If the user cancels, just dismiss the dialog
                    dialog.dismiss()
                }
                .show()
        }

    }
}