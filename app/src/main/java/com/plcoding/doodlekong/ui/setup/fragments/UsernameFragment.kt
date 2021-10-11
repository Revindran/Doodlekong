package com.plcoding.doodlekong.ui.setup.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.plcoding.doodlekong.R
import com.plcoding.doodlekong.databinding.FragmentUsernameBinding
import com.plcoding.doodlekong.ui.setup.SetUpViewModel
import com.plcoding.doodlekong.ui.setup.UsernameViewModel
import com.plcoding.doodlekong.util.BindingFragment
import com.plcoding.doodlekong.util.Constants.MAX_USER_NAME_LENGTH
import com.plcoding.doodlekong.util.Constants.MIN_USER_NAME_LENGTH
import com.plcoding.doodlekong.util.navigateSafely
import com.plcoding.doodlekong.util.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class UsernameFragment : BindingFragment<FragmentUsernameBinding>() {
    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentUsernameBinding::inflate

    private val viewMode: UsernameViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenToEvent()
        binding.btnNext.setOnClickListener {
            viewMode.validateUserNameAndNavigateToSelectedRoom(binding.etUsername.text.toString())
        }
    }

    private fun listenToEvent() {
        lifecycleScope.launchWhenStarted {
            viewMode.setupEvent.collect { event ->
                when (event) {
                    is UsernameViewModel.SetupEvent.NavigateToSelectedRoomEvent -> {
                        findNavController().navigateSafely(
                            R.id.action_usernameFragment_to_selectRoomFragment,
                            args = Bundle().apply { putString("userName", event.username) }
                        )
                    }
                    is UsernameViewModel.SetupEvent.InputEmptyError -> {
                        snackbar(R.string.error_field_empty)
                    }
                    is UsernameViewModel.SetupEvent.InputTooShortEvent -> {
                        snackbar(getString(R.string.error_username_too_short, MIN_USER_NAME_LENGTH))
                    }
                    is UsernameViewModel.SetupEvent.InputTooLongEvent -> {
                        snackbar(getString(R.string.error_username_too_long, MAX_USER_NAME_LENGTH))
                    }
                }
            }
        }
    }
}