package com.example.swiftbill.model

class CustomerId {
    var CustomerName: String?=null
    var contactNumber: String?= null
    var billlist: MutableList<Billdata>?= null
    var openingBal: Int?=null


    constructor()
    constructor(name:String?,contact:String?,OB:Int?,bill:MutableList<Billdata>?,paid:Boolean?){
        CustomerName=name
        contactNumber=contact
        openingBal=OB
        this.billlist=bill
    }
    constructor(name:String?,OB:Int?,bill:MutableList<Billdata>?,paid:Boolean?){
        CustomerName=name
        this.billlist=bill
    }
    constructor(name:String?,contact:String?,OB:Int?){
        CustomerName=name
        contactNumber=contact
        openingBal=OB

    }
}