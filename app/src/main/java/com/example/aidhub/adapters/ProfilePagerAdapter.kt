package com.example.aidhub.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.aidhub.fragments.tabFragments.UserPostsFragment
import com.example.aidhub.fragments.tabFragments.UserRequestsFragment
import com.example.aidhub.fragments.tabFragments.UserReviewsFragment
import com.example.aidhub.utilities.Constants

class ProfilePagerAdapter(fragment: Fragment, private val userId: String) : FragmentStateAdapter(fragment) {

    private val postFragment = UserPostsFragment()
    private val requestsFragment = UserRequestsFragment()
    private val reviewsFragment = UserReviewsFragment()

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> requestsFragment
            1 -> postFragment
            2 -> reviewsFragment
            else -> postFragment
        }
        val bundle = Bundle()
        bundle.putString(Constants.USER_ID_KEY, userId)
        fragment.arguments = bundle
        return fragment
    }
}