package com.example.aidhub.fragments.tabFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aidhub.R
import com.example.aidhub.adapters.RequestAdapter
import com.example.data.dataStractures.Request
import com.example.aidhub.databinding.FragmentUserRequestsBinding
import com.example.aidhub.utilities.Constants
import com.example.aidhub.viewModels.RequestViewModel
import com.google.firebase.auth.FirebaseAuth

class UserRequestsFragment : Fragment() {


    private var _binding: FragmentUserRequestsBinding? = null
    private val binding get() = _binding!!

    private val requestViewModel: RequestViewModel by activityViewModels()
    private var openRequestsList = listOf<Request?>()
    private lateinit var openRequestsAdapter: RequestAdapter
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var profileMode = 0
    private var userId: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.getString(Constants.USER_ID_KEY) ?: currentUser?.uid

        profileMode = if (userId == currentUser?.uid)
            Constants.MODE_MY_PROFILE
        else
            Constants.MODE_OTHER_PROFILE



        if (userId!!.isNotEmpty()) {
            requestViewModel.getUserRequestsOpen(userId!!)
            observeRequests()

        }

        if (profileMode == Constants.MODE_MY_PROFILE)
            binding.btnMoreRequests.visibility = View.VISIBLE
        else
            binding.btnMoreRequests.visibility = View.GONE



        binding.btnMoreRequests.setOnClickListener {
            findNavController().navigate(
                R.id.action_profileFragment_to_allUserRequestsFragment
            )
        }
    }

    private fun observeRequests() {
        requestViewModel.openUserRequests.observe(viewLifecycleOwner) { requests ->
            openRequestsList = requests
            setupOpenRequestsRecyclerView()
        }
    }

    private fun setupOpenRequestsRecyclerView() {
        openRequestsAdapter = RequestAdapter(openRequestsList) { request ->
            val bundle = Bundle()
            bundle.putString(Constants.REQUEST_ID_KEY, request.requestId)
            bundle.putString(Constants.HELPER_ID_KEY, request.helperId)
            findNavController().navigate(
                R.id.action_userRequestsFragment_to_requestFragment,
                bundle
            )
        }

        binding.rvRequests.apply {
            adapter = openRequestsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        if (openRequestsList.isNotEmpty())
            binding.txtRequests.visibility = View.GONE
        else
            binding.txtRequests.visibility = View.VISIBLE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}