package com.example.swiftbill.Adapter

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView

import com.example.swiftbill.R
import com.example.swiftbill.model.Billdata
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageReference
import java.io.File
import com.google.firebase.firestore.FirebaseFirestore



class BillAdapter(
    private var billist: MutableList<Billdata> = mutableListOf(),
            private val context: Context
): RecyclerView.Adapter<BillAdapter.BillViewHolder>() {


    inner class BillViewHolder(Billview: View) : RecyclerView.ViewHolder(Billview) {
        val nameofcoustmer: TextView = Billview.findViewById(R.id.nameofcoustumer)
        val total: TextView = Billview.findViewById(R.id.total)
        val date: TextView = Billview.findViewById(R.id.date)
        val invoice: TextView = Billview.findViewById(R.id.invoiceno)
        val print: ImageView = Billview.findViewById(R.id.print)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bill_layout, parent, false)
        return BillViewHolder(view)
    }

    override fun getItemCount(): Int {
        return billist.size
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = billist[position]

        // Set the text for the customer details
        holder.nameofcoustmer.text = bill.customerName
        holder.invoice.text = "${bill.billId}"
        holder.total.text = "${bill.totalAmount}"
        holder.date.text = bill.date

        // Set up the click listener for the print ImageView
        holder.print.setOnClickListener {
            // Fetch and open the PDF for this specific bill
            //openBillPdf(bill.billId.toString())
            if (hasStoragePermission()) {
                openPdflocal(bill.billId.toString())
            } else {
                requestStoragePermission()
            }
        }
    }

    // Function to fetch and open the PDF file
   /* private fun openBillPdf(billId: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        if (userId != null) {
            val documentRef = db.collection("USER").document(userId)
                .collection("pdfs").document("Bill_#${billId}.pdf")

            documentRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val pdfUrl = document.getString("pdfUrl") // Retrieve PDF URL
                        if (pdfUrl != null) {
                            openPdfFromUrl(pdfUrl) // Call function to open PDF from URL
                        } else {
                            Toast.makeText(context, "PDF URL not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error fetching PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Function to open PDF from a URL
    private fun openPdfFromUrl(pdfUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No PDF viewer installed", Toast.LENGTH_SHORT).show()
        }
    }*/
    private fun openPdflocal(billId: String) {
        if (!hasStoragePermission()) {
            requestStoragePermission()
            return
        }

        // Define the file path based on the billId or invoice number
        val fileName = "Bill_#$billId.pdf"
        val documentsDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Bills")
        val file = File(documentsDirectory, fileName)

        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                context,
                "com.example.swiftbill.provider", // Must match the provider authority in manifest
                file
            )

            // Create an intent to open the PDF
            val pdfIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            }

            // Use createChooser to prompt the user to choose an app
            val chooser = Intent.createChooser(pdfIntent, "Open PDF with:")

            try {
                context.startActivity(chooser)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No PDF viewer found. Please install one.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "PDF not found in local storage.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }


    private fun requestStoragePermission() {
        if (context is Activity) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                100
            )
        } else {
            Toast.makeText(context, "Unable to request permission in non-Activity context", Toast.LENGTH_SHORT).show()
        }
    }

}