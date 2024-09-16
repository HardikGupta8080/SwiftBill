package com.example.swiftbill.model

import android.os.Parcel
import android.os.Parcelable

class Item() : Parcelable {
    var productname: String? = null
    var ratecp: Int? = null
    var ratesp: Int? = null
    var inStock: Int? = null
    var uidcode: String? = null

    // Secondary constructor
    constructor(productname: String?, ratecp: Int?, ratesp: Int?, instock: Int?, code: String?) : this() {
        this.productname = productname
        this.ratecp = ratecp
        this.ratesp = ratesp
        this.inStock = instock
        this.uidcode = code

    }

    // Parcelable implementation
    constructor(parcel: Parcel) : this() {
        productname = parcel.readString()
        ratecp = parcel.readValue(Int::class.java.classLoader) as? Int
        ratesp = parcel.readValue(Int::class.java.classLoader) as? Int
        inStock = parcel.readValue(Int::class.java.classLoader) as? Int
        uidcode = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productname)
        parcel.writeValue(ratecp)
        parcel.writeValue(ratesp)
        parcel.writeValue(inStock)
        parcel.writeString(uidcode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item {
            return Item(parcel)
        }

        override fun newArray(size: Int): Array<Item?> {
            return arrayOfNulls(size)
        }
    }
}
