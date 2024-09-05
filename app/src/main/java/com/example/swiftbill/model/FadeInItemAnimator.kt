package com.example.swiftbill.model
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
class FadeInItemAnimator : DefaultItemAnimator() {
    override fun animateAppearance(
        holder: RecyclerView.ViewHolder,
        preLayoutInfo: ItemHolderInfo?,
        postLayoutInfo: ItemHolderInfo
    ): Boolean {
        val view = holder.itemView
        view.alpha = 0f
        val animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
        animator.duration = 100
        animator.start()
        return true
    }

    override fun animateDisappearance(
        holder: RecyclerView.ViewHolder,
        preLayoutInfo: ItemHolderInfo,
        postLayoutInfo: ItemHolderInfo?
    ): Boolean {
        val view = holder.itemView
        val animator = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
        animator.duration = 100
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.alpha = 1f // Reset the alpha for reuse
            }
        })
        animator.start()
        return true
    }
}
