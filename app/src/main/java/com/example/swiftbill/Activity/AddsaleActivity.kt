package com.example.swiftbill.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swiftbill.databinding.ActivityAddsaleBinding
import com.example.swiftbill.databinding.DialogAddItemBinding
import com.example.swiftbill.Adapter.Bill_Item_Adapter
import com.example.swiftbill.R
import com.example.swiftbill.model.BillItem
import com.example.swiftbill.model.Billdata
import com.example.swiftbill.model.CustomerId
import com.example.swiftbill.model.InventoryTransaction
import com.example.swiftbill.model.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException



class AddsaleActivity : AppCompatActivity() {
    private lateinit var billItemAdapter: Bill_Item_Adapter
    private val db = FirebaseFirestore.getInstance()
    private val binding by lazy {
        ActivityAddsaleBinding.inflate(layoutInflater)
    }
    private var  customerList:MutableList<CustomerId> = mutableListOf()
    private var itemlist: MutableList<Item> = mutableListOf()
    private val initialBillItems: MutableList<BillItem> = mutableListOf()
    private val BillItemsList: MutableList<BillItem> = mutableListOf()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.blue)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        binding.Date.text = currentDate

        getLastInvoiceNumber { binding.invoiceNo.text = "#$it" }

        // Switch logic for customer
        binding.paidamt.setText("0")
        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            binding.switch1.text = if (isChecked) "Cash" else "Credit"

            if(isChecked){
                binding.paidamt.visibility=GONE

                binding.textView16.visibility= GONE
                binding.Coustmername.editText?.setText( "Cash")
            }
            else{

                binding.paidamt.visibility= VISIBLE
                binding.textView16.visibility= VISIBLE
                binding.Coustmername.editText?.setText("")
            }
        }

        binding.amount.text = " ₹......."
        // Fetch inventory from Firestore
        fetchInventory()
        //Featch costumer Data
        fetchCustomers()
        // Initialize RecyclerView with empty data
        initializeRecyclerView()
        // Handle item selection in AutoCompleteTextView
        handleAutoCompleteSelection()
        if (binding.Coustmername.editText?.text.toString().isEmpty())
        {
            binding.balance.setText("")
        }

        binding.save.setOnClickListener {

            val customerName = binding.Coustmername.editText?.text.toString()

            if (customerName.isNotBlank()) {

                saveBillAndCustomerDetails(currentDate)
                generateBillPdf()
            } else {
                Toast.makeText(this, "Please fill in the Customer Name", Toast.LENGTH_LONG).show()
            }
        }
        binding.saveandnew.setOnClickListener {
            val customerName = binding.Coustmername.editText?.text.toString()

            if (customerName.isNotBlank()) {
                // Save the bill (same logic as 'save')
                saveBillAndCustomerDetails(currentDate)
                generateBillPdf()

                // Refresh the activity for a new entry
                val intent = Intent(this, AddsaleActivity::class.java)
                finish()  // Finish the current instance of the activity
                startActivity(intent)  // Start a new instance of the activity
            } else {
                Toast.makeText(this, "Please fill in the Customer Name", Toast.LENGTH_LONG).show()
            }

        }

    }

    private fun fetchInventory() {
        db.collection("USER").document(Firebase.auth.currentUser?.uid.toString())
            .collection("INVETORY")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e)
                    return@addSnapshotListener
                }

                snapshots?.let {
                    itemlist.clear()
                    for (document in snapshots.documents) {
                        document.toObject(Item::class.java)?.let { itemlist.add(it) }
                    }

                    setupAutoCompleteTextView()
                }
            }
    }
    private fun setupAutoCompleteTextView() {
        val productNames = itemlist.mapNotNull { it.productname }
        if (productNames.isNotEmpty()) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                productNames
            )
            binding.autoCompleteItems.setAdapter(adapter)
            binding.autoCompleteItems.threshold = 0
        } else {
            Toast.makeText(this, "No items available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeRecyclerView() {
        billItemAdapter = Bill_Item_Adapter(this, initialBillItems, { billItem, position ->
            showAddItemDialog(
                selectedItem = Item(),
                billItemToUpdate = billItem,
                position = position
            )
        }, binding.root)

        binding.recyclerViewItems.apply {
            layoutManager = LinearLayoutManager(this@AddsaleActivity)
            adapter = billItemAdapter
        }
    }


    private fun saveBillAndCustomerDetails(currentDate: String) {
        var billdata: Billdata
        // Get the current state of the switch
        val isSwitchOn = binding.switch1.isChecked // Check if switch is on
        val Paidamount = if (isSwitchOn) {
            updateTotalAmount()
        } else {
            binding.paidamt.text.toString().toIntOrNull() ?: 0
        }
        getLastInvoiceNumber { newInvoiceNumber ->
            billdata = Billdata().apply {
                customerName = binding.Coustmername.editText?.text.toString()
                date = currentDate
                billId = newInvoiceNumber
                contactno = binding.Contactno.editText?.text.toString()
                totalAmount = updateTotalAmount()
                items = billItemAdapter.getItems()
                amountpaid=Paidamount
                bal = totalAmount!! - amountpaid!!
                paid = bal!! <=0
                profit=calculatetotalprofit()
            }

            val customer = CustomerId().apply {
                CustomerName = binding.Coustmername.editText?.text.toString()
                contactNumber = binding.Contactno.editText?.text.toString()
                openingBal = updateTotalAmount()- Paidamount
            }
            saveBillToFirestore(billdata, customer)
        }
    }
    private fun saveBillToFirestore(bill: Billdata, customer: CustomerId) {
        val userId = Firebase.auth.currentUser?.uid.toString()
        val billRef = db.collection("USER").document(userId).collection("BILL").document(bill.billId.toString())

        // Save the bill
        billRef.set(bill)
            .addOnSuccessListener {
                Toast.makeText(this, "Bill saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving bill: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }

        // Check if the customer exists using name and contact number
        val customerQuery = db.collection("USER").document(userId).collection("CUSTOMER")
            .whereEqualTo("contactNumber", customer.contactNumber)

        customerQuery.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Customer exists, update their document
                    val document = querySnapshot.documents.first()
                    val existingCustomer = document.toObject(CustomerId::class.java)
                    val updatedBills = existingCustomer?.billlist?.toMutableList() ?: mutableListOf()
                    updatedBills.add(bill)
                    existingCustomer?.apply {
                        billlist = updatedBills
                        openingBal = (openingBal ?: 0) + (bill.bal ?: 0)
                    }

                    // Update the existing customer document
                    db.collection("USER").document(userId).collection("CUSTOMER").document(customer.contactNumber.toString())
                        .set(existingCustomer!!)
                        .addOnSuccessListener {
                            Log.d("TAG", "Customer details updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update customer details: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Customer does not exist, create a new document
                    customer.billlist = mutableListOf(bill)

                    // Add the new customer document
                    db.collection("USER").document(userId).collection("CUSTOMER").document(customer.contactNumber.toString())
                        .set(customer)
                        .addOnSuccessListener { documentReference ->

                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to save customer details: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error checking customer existence", e)
                Toast.makeText(this, "Error checking customer existence: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }

        // Update the inventory for each item in the bill
        for (item in bill.items ?: emptyList()) { // Handle null case for items
            val itemRef = db.collection("USER").document(userId).collection("INVETORY").document(item.uid ?: "")
            itemRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentStock = document.getLong("inStock") ?: 0
                        val updatedStock = currentStock - (item.quantity ?: 0)

                        itemRef.update("inStock", updatedStock)
                            .addOnSuccessListener {
                                Log.d("TAG", "Inventory updated for item: ${item.productname}")
                            }
                            .addOnFailureListener { e ->
                                Log.w("TAG", "Error updating inventory for item: ${item.productname}", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error retrieving item: ${item.productname}", e)
                }
        }
    }


    private fun showAddItemDialog(
        selectedItem: Item,
        billItemToUpdate: BillItem? = null,
        position: Int? = null
    ) {
        val dialogBinding = DialogAddItemBinding.inflate(LayoutInflater.from(this))
        val dialogTitle = if (billItemToUpdate != null) "Update Item" else "Add Item to Bill"

        dialogBinding.etSalePrice.setText(
            billItemToUpdate?.sp?.toString() ?: selectedItem.ratesp?.toString()
        )
        dialogBinding.etDiscount.setText(billItemToUpdate?.discount?.toString() ?: "0")
        dialogBinding.etQuantity.setText(billItemToUpdate?.quantity?.toString() ?: "")

        val dialog = AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogBinding.root)
            .setPositiveButton(if (billItemToUpdate != null) "Update" else "Add") { _, _ ->
                val quantity = dialogBinding.etQuantity.text.toString()
                val discount = dialogBinding.etDiscount.text.toString()
                val salePrice = dialogBinding.etSalePrice.text.toString()

                if (quantity.isNotEmpty() && discount.isNotEmpty() && salePrice.isNotEmpty()) {
                    val billItem = BillItem(
                        productname = billItemToUpdate?.productname ?: selectedItem.productname,
                        quantity = quantity.toInt(),
                        discount = discount.toInt(),
                        sp = salePrice.toInt(),
                        uid = selectedItem.uidcode,
                        cp = selectedItem.ratecp
                    )

                    if (billItemToUpdate != null && position != null) {
                        billItemAdapter.updateItem(billItem, position)
                    } else {
                        billItemAdapter.addItem(billItem)
                    }
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }

                if (billItemToUpdate == null) binding.autoCompleteItems.text.clear()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun fetchCustomers() {
        db.collection("USER").document(Firebase.auth.currentUser?.uid.toString())
            .collection("CUSTOMER")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e)
                    return@addSnapshotListener
                }

                snapshots?.let {
                    val newCustomerList = mutableListOf<CustomerId>()
                    for (document in snapshots.documents) {
                        document.toObject(CustomerId::class.java)?.let { newCustomerList.add(it) }
                    }

                    // Update local customerList
                    customerList.clear()
                    customerList.addAll(newCustomerList)

                    // Update UI
                    setupAutoCompleteForCustomer()
                }
            }
    }

    private fun setupAutoCompleteForCustomer() {
        val customerNamesWithContactAndBalance = customerList.map {
            "${it.CustomerName} (${it.contactNumber}) - ₹${it.openingBal}"
        }
        val customerContactsWithNameAndBalance = customerList.map {
            "${it.contactNumber} (${it.CustomerName}) - ₹${it.openingBal}"
        }

        // Set up auto-complete for customer name
        val nameAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            customerNamesWithContactAndBalance
        )
        (binding.Coustmername.editText as? AutoCompleteTextView)?.apply {
            setAdapter(nameAdapter)
            threshold = 1
            setOnItemClickListener { _, view, position, _ ->
                // Extract contact number and balance from the selected item
                val selectedText = (view as TextView).text.toString()
                val contactNumber = selectedText.substringAfter("(").substringBefore(")")
                val selectedCustomer = customerList.find { it.contactNumber == contactNumber }

                selectedCustomer?.let { customer ->
                    // Set only the customer's name
                    binding.Coustmername.editText?.setText(customer.CustomerName)
                    binding.Contactno.editText?.setText(customer.contactNumber)
                    binding.balance.setText("₹${customer.openingBal}")
                }
            }
        }

        // Set up auto-complete for contact number


    }


    fun updateTotalAmount(): Int {
        val totalAmount = calculateTotalAmount()
        binding.amount.text = " ₹ $totalAmount"
        return totalAmount
    }

    private fun calculateTotalAmount(): Int {
        return billItemAdapter.getItems()
            .sumOf { (it.sp?.minus(it.discount!!))?.times(it.quantity!!) ?: 0 }
    }


    private fun getLastInvoiceNumber(callback: (Int) -> Unit) {
        db.collection("USER").document(Firebase.auth.currentUser?.uid.toString()).collection("BILL")
            .orderBy("billId", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                val lastInvoiceNumber = documents.documents.firstOrNull()?.getLong("billId") ?: 0
                callback((lastInvoiceNumber + 1).toInt())
            }
    }
    fun generateBillPdf(): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.textSize = 12f


        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.BLACK // Ensure text color is black
        canvas.drawText("Invoice", pageInfo.pageWidth / 2f, 50f, paint)

        // Invoice details
        paint.textAlign = Paint.Align.LEFT
        val leftMargin = 20f
        canvas.drawText("Invoice Number: ${binding.invoiceNo.text}", leftMargin, 100f, paint)
        canvas.drawText("Customer Name: ${binding.Coustmername.editText?.text}", leftMargin, 120f, paint)
        canvas.drawText("Date: ${binding.Date.text}", leftMargin, 140f, paint)

        // Table column headers
        val topMargin = 180f
        val rowHeight = 40f
        val colWidths = floatArrayOf(40f, 120f, 70f, 100f, 100f, 100f) // Adjusted column widths for Sr No.

        // Draw table header row
        paint.style = Paint.Style.STROKE // For drawing borders
        paint.color = Color.BLACK // Border color
        drawFullTableHeader(canvas, paint, colWidths, topMargin)

        // Table header text
        paint.style = Paint.Style.FILL // For drawing text
        val headerY = topMargin + rowHeight / 2f + 6f
        drawTextInTable(canvas, "Sr No.", 16f, headerY, colWidths[0], paint)
        drawTextInTable(canvas, "Product Name", leftMargin + colWidths[0], headerY, colWidths[1], paint)
        drawTextInTable(canvas, "Quantity", leftMargin + colWidths[0] + colWidths[1], headerY, colWidths[2], paint)
        drawTextInTable(canvas, "Price", leftMargin + colWidths[0] + colWidths[1] + colWidths[2], headerY, colWidths[3], paint)
        drawTextInTable(canvas, "Discount", leftMargin + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3], headerY, colWidths[4], paint)
        drawTextInTable(canvas, "Total", leftMargin + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] + colWidths[4], headerY, colWidths[5], paint)

        // Draw table rows
        var currentY = topMargin + rowHeight
        var serialNo = 1
        for (item in billItemAdapter.getItems()) {
            // Draw table row borders first
            paint.style = Paint.Style.STROKE // Ensure we are drawing borders
            drawFullTableRow(canvas, paint, colWidths, currentY)

            // Draw the text inside the table cells
            paint.style = Paint.Style.FILL // Switch back to fill for text
            val rowCenterY = currentY + rowHeight / 2f + 6f
            drawTextInTable(canvas, serialNo.toString(), leftMargin, rowCenterY, colWidths[0], paint)
            drawTextInTable(canvas, item.productname ?: "", leftMargin + colWidths[0], rowCenterY, colWidths[1], paint)
            drawTextInTable(canvas, item.quantity.toString(), leftMargin + colWidths[0] + colWidths[1], rowCenterY, colWidths[2], paint)
            item.sp?.toString()?.let { drawTextInTable(canvas, it, leftMargin + colWidths[0] + colWidths[1] + colWidths[2], rowCenterY, colWidths[3], paint) }
            val discount = (item.discount?.times(item.quantity!!)).toString()
            drawTextInTable(canvas, discount, leftMargin + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3], rowCenterY, colWidths[4], paint)
            val total = (item.sp?.times(item.quantity!!) ?: 0) - (item.discount?.times(item.quantity!!) ?: 0)
            drawTextInTable(canvas, total.toString(), leftMargin + colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] + colWidths[4], rowCenterY, colWidths[5], paint)

            currentY += rowHeight
            serialNo++
        }

        // Total Amount
        currentY += 20f
        paint.textAlign = Paint.Align.LEFT
        paint.style = Paint.Style.FILL // Make sure total amount text is filled, not stroked
        canvas.drawText("Total Amount: ₹${updateTotalAmount()}", leftMargin, currentY, paint)

        pdfDocument.finishPage(page)

        // Save to public Documents directory
        val documentsDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Bills")
        val file = File(documentsDirectory,"Bill_${binding.invoiceNo.text}.pdf")

        return try {
            pdfDocument.writeTo(FileOutputStream(file))

            uploadPdfToFirebase(file, "Bill_${binding.invoiceNo.text}.pdf")

            file // Return the file if successful

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
            null // Return null if there's an error
        } finally {
            pdfDocument.close()
        }
    }
    fun uploadPdfToFirebase(file: File, fileName: String) {
        addSaleTransaction()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknownUser"

        // Create a unique path for each user based on their UID
        val storageRef = FirebaseStorage.getInstance().reference.child("users/$userId/pdfs/$fileName")
        val uri = Uri.fromFile(file)

        storageRef.putFile(uri)
            .addOnSuccessListener {
                // Get the download URL after the upload completes
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val pdfUrl = downloadUri.toString()
                    // Save the URL to Firestore for the current user
                    val db = FirebaseFirestore.getInstance()
                    val documentRef = db.collection("USER").document(userId)
                        .collection("pdfs").document(fileName)  // Using billId as the document ID

                    documentRef.set(mapOf("pdfUrl" to pdfUrl)) // Update the pdfUrl field for the user
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error saving PDF URL: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error uploading PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    // Function to draw the full table header (borders)
    fun drawFullTableHeader(canvas: Canvas, paint: Paint, colWidths: FloatArray, topMargin: Float) {
        var xPos = 20f // Starting position
        for (colWidth in colWidths) {
            canvas.drawRect(xPos, topMargin, xPos + colWidth, topMargin + 40f, paint) // Draw header border
            xPos += colWidth
        }
    }

    // Function to draw the full table rows (borders)
    fun drawFullTableRow(canvas: Canvas, paint: Paint, colWidths: FloatArray, currentY: Float) {
        var xPos = 20f // Starting position
        for (colWidth in colWidths) {
            canvas.drawRect(xPos, currentY, xPos + colWidth, currentY + 40f, paint) // Draw row border
            xPos += colWidth
        }
    }

    // Helper function to draw text inside a table cell
    fun drawTextInTable(canvas: Canvas, text: String, xPos: Float, yPos: Float, colWidth: Float, paint: Paint) {
        paint.color = Color.BLACK // Ensure text is always black
        canvas.drawText(text, xPos + 10f, yPos, paint)
//hardik

    }

    private fun handleAutoCompleteSelection() {
        binding.autoCompleteItems.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem =
                itemlist.firstOrNull { it.productname == parent.getItemAtPosition(position) }
            selectedItem?.let {
                val isItemAlreadyInRecyclerView = billItemAdapter.getItems().any { item ->
                    item.productname == it.productname
                }

                if (isItemAlreadyInRecyclerView) {
                    AlertDialog.Builder(this)
                        .setTitle("Item already added")
                        .setMessage("This item is already in the list.")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    showAddItemDialog(it)
                }
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    itemlist.mapNotNull { item -> item.productname }
                )
                binding.autoCompleteItems.setAdapter(adapter)
            }
        }
    }
    fun addSaleTransaction() {


        // Get the user and item IDs
        val userId = Firebase.auth.currentUser?.uid.toString()
        for(item in initialBillItems) {
            var itemId = item.uid
            // Replace with the actual item ID
            val saleTransaction = InventoryTransaction(
                date = LocalDate.now().toString(),
                price = item.sp.toString().toInt(),
                quantity = item.quantity.toString().toInt(),
                transactionType = "Sale"
            )

        // Reference to the transactions collection
        db.collection("USER")
            .document(userId)
            .collection("INVETORY")
            .document(itemId!!)
            .collection("TRANSACTIONS")
            .document()
            .set(saleTransaction)
            .addOnSuccessListener {
                // Optionally, update the total inventory quantity here

            }
    }
    }
    fun calculatetotalprofit(): Int {
        var profit =0
        for (item in initialBillItems){
            profit=profit+(item.sp!!-item.cp!!-item.discount!!)*item.quantity!!
        }
        return profit
    }

}