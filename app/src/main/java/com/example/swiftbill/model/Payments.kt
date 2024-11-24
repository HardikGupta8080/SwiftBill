package com.example.swiftbill.model
import android.os.Parcel
import android.os.Parcelable

data class Payments(
    val paymentId: Int? = null,
    val amountPaid: Int? = null,
    val paymentDate: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(paymentId)
        parcel.writeValue(amountPaid)
        parcel.writeString(paymentDate)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Payments> {
        override fun createFromParcel(parcel: Parcel): Payments = Payments(parcel)
        override fun newArray(size: Int): Array<Payments?> = arrayOfNulls(size)
    }
}
