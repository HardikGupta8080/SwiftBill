package com.example.swiftbill.model


import android.os.Parcel
import android.os.Parcelable

data class CustomerId(
    var CustomerName: String? = null,
    var contactNumber: String? = null,
    var openingBal: Int? = null,
    var billlist: MutableList<Billdata>? = null,
    var payments:MutableList<Payments>? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.createTypedArrayList(Billdata.CREATOR),
        parcel.createTypedArrayList(Payments.CREATOR)
       // Assuming Billdata is also Parcelable
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(CustomerName)
        parcel.writeString(contactNumber)
        parcel.writeValue(openingBal)
        parcel.writeTypedList(billlist)
        parcel.writeTypedList(payments)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CustomerId> {
        override fun createFromParcel(parcel: Parcel): CustomerId {
            return CustomerId(parcel)
        }

        override fun newArray(size: Int): Array<CustomerId?> {
            return arrayOfNulls(size)
        }
    }
}
