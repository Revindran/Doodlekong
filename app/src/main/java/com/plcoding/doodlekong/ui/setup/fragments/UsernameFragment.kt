package com.plcoding.doodlekong.ui.setup.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.plcoding.doodlekong.databinding.FragmentUsernameBinding
import com.plcoding.doodlekong.util.BindingFragment

class UsernameFragment : BindingFragment<FragmentUsernameBinding>() {
    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentUsernameBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}