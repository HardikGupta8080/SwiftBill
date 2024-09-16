package com.example.swiftbill.model

class Billdata {
    var billId: Int?=null                // Unique identifier for the bill
    var customerName: String?=null
    var contactno: String?=null// Name of the customer
    var date: String?=null                  // Date of the bill
    var items: List<BillItem>?=null         // List of items in the bill
    var totalAmount: Int?=null
    var paid:Boolean?=null// Total amount of the bill
    var amountpaid:Int?=null
    var bal:Int?=null
    constructor()
    constructor(
        billId: Int?,
        customerName: String?,
        date: String?,
        items: List<BillItem>?,
        totalAmount: Int?,
        contactno: String?,
        paid:Boolean?,
        amountpaid:Int?,
        bal:Int?
    ) {
        this.billId = billId
        this.customerName = customerName
        this.date = date
        this.items = items
        this.totalAmount = totalAmount
        this.contactno = contactno
        this.paid=paid
        this.amountpaid=amountpaid
        this.bal = bal
    }

}