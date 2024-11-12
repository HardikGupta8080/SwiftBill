package com.example.swiftbill.model

sealed class ListItem {
    data class BillItem(val bill: Billdata) : ListItem()
    data class PaymentItem(val payment: Payments) : ListItem()
}