package com.example.swiftbill.model


class Item() {
    var productname: String? = null
    var ratecp: Int? = null
    var ratesp: Int? = null
    var inStock: Int? = null
    var uidcode: String? = null
    var category: String? = null

    // Secondary constructor
    constructor(
        productname: String?,
        ratecp: Int?,
        ratesp: Int?,
        instock: Int?,
        code: String?,
        category: String?
    ) : this() {
        this.productname = productname
        this.ratecp = ratecp
        this.ratesp = ratesp
        this.inStock = instock
        this.uidcode = code
        this.category = category
    }

    // Parcelable implementation
}
