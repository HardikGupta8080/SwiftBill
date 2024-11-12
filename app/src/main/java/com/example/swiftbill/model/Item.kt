package com.example.swiftbill.model

import android.os.Parcel
import android.os.Parcelable

data class Item(
    var productname: String? = null,  // Name of the product
    var ratecp: Int? = null,           // Cost price
    var ratesp: Int? = null,           // Selling price
    var inStock: Int? = null,          // Quantity in stock
    var uidcode: String? = null,       // Unique identifier code
    var category: String? = null,
// Product category
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productname)
        parcel.writeValue(ratecp)
        parcel.writeValue(ratesp)
        parcel.writeValue(inStock)
        parcel.writeString(uidcode)
        parcel.writeString(category)
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
