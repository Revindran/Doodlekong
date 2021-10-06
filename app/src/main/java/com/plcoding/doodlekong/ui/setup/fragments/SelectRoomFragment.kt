package com.plcoding.doodlekong.ui.setup.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.plcoding.doodlekong.databinding.FragmentSelectRoomBinding
import com.plcoding.doodlekong.util.BindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectRoomFragment : BindingFragment<FragmentSelectRoomBinding>() {
    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentSelectRoomBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}