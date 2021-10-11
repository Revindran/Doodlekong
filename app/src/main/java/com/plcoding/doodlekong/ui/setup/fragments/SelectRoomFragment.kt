package com.plcoding.doodlekong.ui.setup.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.plcoding.doodlekong.R
import com.plcoding.doodlekong.adapters.RoomAdapter
import com.plcoding.doodlekong.databinding.FragmentSelectRoomBinding
import com.plcoding.doodlekong.ui.setup.SelectRoomViewModel
import com.plcoding.doodlekong.util.BindingFragment
import com.plcoding.doodlekong.util.Constants.SEARCH_DELAY
import com.plcoding.doodlekong.util.navigateSafely
import com.plcoding.doodlekong.util.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectRoomFragment : BindingFragment<FragmentSelectRoomBinding>() {
    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentSelectRoomBinding::inflate

    private val viewModel: SelectRoomViewModel by viewModels()
    private val args: SelectRoomFragmentArgs by navArgs()

    @Inject
    lateinit var roomAdapter: RoomAdapter

    private var updatedJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        subscribeToObservers()
        listenToEvents()
        viewModel.getRooms("")

        var searchJob: Job? = null
        binding.etRoomName.addTextChangedListener {
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(SEARCH_DELAY)
                viewModel.getRooms(it.toString())
            }
        }

        binding.ibReload.setOnClickListener {
            binding.roomsProgressBar.isVisible = true
            binding.tvNoRoomsFound.isVisible = false
            binding.ivNoRoomsFound.isVisible = false
            viewModel.getRooms(binding.etRoomName.text.toString())
        }

        binding.btnCreateRoom.setOnClickListener {
            findNavController().navigateSafely(
                R.id.action_selectRoomFragment_to_createRoomFragment,
                Bundle().apply { putString("userName", args.userName) }
            )
        }

        roomAdapter.setOnRoomClickListener {
            viewModel.joinRoom(args.userName, it.name)
        }
    }


    private fun listenToEvents() = lifecycleScope.launchWhenStarted {
        viewModel.setupEvent.collect { event ->
            when (event) {
                is SelectRoomViewModel.SetupEvent.JoinRoomEvent -> {
                    findNavController().navigateSafely(
                        R.id.action_selectRoomFragment_to_drawingActivity,
                        args = Bundle().apply {
                            putString("userName", args.userName)
                            putString("roomName", event.roomName)
                        }
                    )
                }
                is SelectRoomViewModel.SetupEvent.JoinRoomErrorEvent -> {
                    snackbar(event.error)
                }
                is SelectRoomViewModel.SetupEvent.GetRoomErrorEvent -> {
                    binding.roomsProgressBar.isVisible = false
                    binding.tvNoRoomsFound.isVisible = false
                    binding.ivNoRoomsFound.isVisible = false
                    snackbar(event.error)
                }
                else -> Unit
            }
        }
    }

    private fun subscribeToObservers() = lifecycleScope.launchWhenStarted {
        viewModel.room.collect { event ->
            when (event) {
                is SelectRoomViewModel.SetupEvent.GetRoomLoadingEvent -> {
                    binding.roomsProgressBar.isVisible = true
                }
                is SelectRoomViewModel.SetupEvent.GetRoomEvent -> {
                    binding.roomsProgressBar.isVisible = false
                    val isRoomEmpty = event.rooms.isEmpty()
                    binding.ivNoRoomsFound.isVisible = isRoomEmpty
                    binding.tvNoRoomsFound.isVisible = isRoomEmpty
                    updatedJob?.cancel()
                    updatedJob = lifecycleScope.launch {
                        roomAdapter.updateDataSet(event.rooms)
                    }
                }
                else -> Unit
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvRooms.apply {
            adapter = roomAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
}