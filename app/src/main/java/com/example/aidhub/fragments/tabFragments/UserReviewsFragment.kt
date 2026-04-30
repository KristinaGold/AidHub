package com.example.aidhub.fragments.tabFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aidhub.adapters.ReviewAdapter
import com.example.data.dataStractures.Review
import com.example.aidhub.databinding.FragmentUserReviewsBinding
import com.example.aidhub.utilities.Constants
import com.example.aidhub.viewModels.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

class UserReviewsFragment : Fragment() {


    private var _binding: FragmentUserReviewsBinding? = null
    private val binding get() = _binding!!

    private var reviewList = listOf<Review?>()
    private lateinit var reviewAdapter: ReviewAdapter
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private var userId: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.getString(Constants.USER_ID_KEY) ?: currentUser?.uid
        getUserReviews()


    }

    private fun getUserReviews() {
        profileViewModel.getUserReviews(userId!!)
        profileViewModel.userReviews.observe(viewLifecycleOwner) { reviews ->

            this.reviewList = reviews
            setupReviewsRecyclerView()
        }
    }

    private fun setupReviewsRecyclerView() {
        reviewAdapter = ReviewAdapter(reviewList)

        binding.rvReviews.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(context)
        }

        if (reviewList.isNotEmpty())
            binding.txtReviews.visibility = View.GONE
        else
            binding.txtReviews.visibility = View.VISIBLE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}