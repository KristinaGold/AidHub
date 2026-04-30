package com.example.aidhub.managers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.SearchView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.example.aidhub.R
import com.example.aidhub.utilities.dpToPx

object AnimationManager {

    private const val DEFAULT_DURATION = 300L

    fun toggleAddMenu(
        button: View,
        container: View,
        isOpen: Boolean,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        val rotationDegree = if (isOpen) 45f else 0f
        button.animate()
            .rotation(rotationDegree)
            .setDuration(DEFAULT_DURATION)
            .start()

        slideFade(container, isOpen)
    }

    fun slideFade(view: View, isOpen: Boolean, fromTop: Boolean = false) {
        val translationValue = if (fromTop) -100f else 100f

        if (isOpen) {
            view.visibility = View.VISIBLE
            view.alpha = 0f
            view.translationY = translationValue
            view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(DEFAULT_DURATION)
                .setListener(null)
                .start()
        } else {
            view.animate()
                .translationY(translationValue)
                .alpha(0f)
                .setDuration(DEFAULT_DURATION)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                    }
                })
                .start()
        }
    }

    fun fadeIn(view: View) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.animate().alpha(1f).setDuration(DEFAULT_DURATION).start()
    }

    fun transformSearchToIcon(searchView: SearchView, container: View, isCollapsed: Boolean) {
        val targetWidth = if (isCollapsed) 52.dpToPx() else ViewGroup.LayoutParams.WRAP_CONTENT
        if (searchView.layoutParams.width == targetWidth) return

        val transition = ChangeBounds()
        transition.duration = DEFAULT_DURATION
        transition.interpolator = DecelerateInterpolator()
        TransitionManager.beginDelayedTransition(container.parent as ViewGroup, transition)

        val params = searchView.layoutParams
        params.width = targetWidth
        searchView.layoutParams = params
        searchView.setIconifiedByDefault(isCollapsed)
        //searchView.isIconified = isCollapsed
    }
}

fun View.startBlinkAnimation() {
    val animation = AnimationUtils.loadAnimation(context, R.anim.blink_animation)
    this.startAnimation(animation)
}