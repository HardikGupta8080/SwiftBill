package com.example.swiftbill.model

class CustomerId {
    var CustomerName: String?=null
    var contactNumber: String?= null
    var openingBal: Int?=null
    constructor()
    constructor(name:String?,contact:String?,OB:Int?){
        CustomerName=name
        contactNumber=contact
        openingBal=OB

    }
    constructor(CustomerName: String?,OB: Int?) {
        this.CustomerName = CustomerName
        openingBal=OB
    }

}