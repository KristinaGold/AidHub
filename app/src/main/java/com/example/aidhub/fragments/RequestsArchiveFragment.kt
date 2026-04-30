package com.example.aidhub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.R
import com.example.aidhub.adapters.RequestAdapter
import com.example.data.dataStractures.Request
import com.example.aidhub.databinding.FragmentAllUserRequestsBinding
import com.example.aidhub.fragments.base.BaseFragment
import com.example.aidhub.utilities.Constants
import com.example.aidhub.viewModels.RequestViewModel

class RequestsArchiveFragment : BaseFragment<FragmentAllUserRequestsBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAllUserRequestsBinding.inflate(inflater, container, false)

    override fun setupTopBar() {
        setTopBarTitle(Constants.ALL_USER_REQUESTS_SCREEN_TITLE)
        showBackButton()
    }

    private val requestViewModel: RequestViewModel by activityViewModels()
    private var allRequestsList = listOf<Request?>()
    private lateinit var allRequestsAdapter: RequestAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestViewModel.getUserRequestsArchive(AuthManager.getUid()!!)
        observeRequests()

    }

    private fun observeRequests() {
        requestViewModel.userRequests.observe(viewLifecycleOwner) { requests ->
            allRequestsList = requests
            setupAllRequestsRecyclerView()
        }
    }


    private fun setupAllRequestsRecyclerView() {
        allRequestsAdapter = RequestAdapter(allRequestsList) { request ->
            val bundle = Bundle()
            bundle.putString(Constants.REQUEST_ID_KEY, request.requestId)
            bundle.putString(Constants.HELPER_ID_KEY, request.helperId)
            findNavController().navigate(
                R.id.action_allUserRequestsFragment_to_requestFragment,
                bundle
            )
        }

        binding.rvAllRequests.apply {
            adapter = allRequestsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
}