package com.example.swiftbill.model

import android.os.Parcel
import android.os.Parcelable

data class Billdata(
    var billId: Int? = null,                  // Unique identifier for the bill
    var customerName: String? = null,         // Name of the customer
    var contactno: String? = null,             // Contact number of the customer
    var date: String? = null,                  // Date of the bill
    var items: List<BillItem>? = null,        // List of items in the bill
    var totalAmount: Int? = null,             // Total amount of the bill
    var paid: Boolean? = null,                 // Whether the bill is paid
    var amountpaid: Int? = null,              // Amount paid
    var bal: Int? = null                       // Remaining balance
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(BillItem.CREATOR), // Assuming BillItem is also Parcelable
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(billId)
        parcel.writeString(customerName)
        parcel.writeString(contactno)
        parcel.writeString(date)
        parcel.writeTypedList(items)
        parcel.writeValue(totalAmount)
        parcel.writeValue(paid)
        parcel.writeValue(amountpaid)
        parcel.writeValue(bal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Billdata> {
        override fun createFromParcel(parcel: Parcel): Billdata {
            return Billdata(parcel)
        }

        override fun newArray(size: Int): Array<Billdata?> {
            return arrayOfNulls(size)
        }
    }
}
