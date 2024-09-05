package com.example.swiftbill.model

class BillItem {
    var productname : String?=null
    var quantity : Int?= null
    var discount : Int?= null
    var sp : Int?=null
    constructor()
    constructor(productname: String?, quantity: Int?, discount: Int?, sp: Int?) {
        this.productname = productname
        this.quantity = quantity
        this.discount = discount
        this.sp = sp
    }

}