package com.example.swiftbill.model

class Billdata {
    var billId: Int?=null                // Unique identifier for the bill
    var customerName: String?=null          // Name of the customer
    var date: String?=null                  // Date of the bill
    var items: List<Item>?=null         // List of items in the bill
    var totalAmount: Int?=null           // Total amount of the bill
    constructor()
    constructor(customerName: String?,date: String?,items: List<Item>?)
    {
        this.customerName=customerName
        this.date=date
        this.items=items

    }
}