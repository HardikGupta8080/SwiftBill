package com.example.swiftbill.model

data class InventoryTransaction(
    var date: String? = null,
    var price: Int = 0,         // Can represent cost price or selling price based on transactionType
    var quantity: Int = 0,       // Positive for purchase, negative for sale
    var transactionType: String  // "purchase" or "sale"
){
    constructor() : this(null, 0, 0, "")
}
