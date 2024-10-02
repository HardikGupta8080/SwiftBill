package com.example.swiftbill.model

import android.os.Parcel
import android.os.Parcelable

data class BillItem(
            // Unique identifier code
    var productname: String? = null,      // Name of the product
    var quantity: Int? = null,            // Quantity of the product
    var discount: Int? = null,             // Discount on the product
    var sp: Int? = null,
    var uid: String? = null, // Selling price


) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),

    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {

        parcel.writeString(productname)
        parcel.writeValue(quantity)
        parcel.writeValue(discount)
        parcel.writeValue(sp)
        parcel.writeString(uid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BillItem> {
        override fun createFromParcel(parcel: Parcel): BillItem {
            return BillItem(parcel)
        }

        override fun newArray(size: Int): Array<BillItem?> {
            return arrayOfNulls(size)
        }
    }
}
