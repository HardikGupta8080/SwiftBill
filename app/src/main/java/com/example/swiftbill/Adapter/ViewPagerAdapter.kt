package com.example.swiftbill.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.swiftbill.fragments.PaymentFragment
import com.example.swiftbill.fragments.SaleFragment
import com.example.swiftbill.model.CustomerId

class ViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val customer: CustomerId // Pass the customer data here
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SaleFragment.newInstance(customer)
            1 -> PaymentFragment.newInstance(customer)
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
}

