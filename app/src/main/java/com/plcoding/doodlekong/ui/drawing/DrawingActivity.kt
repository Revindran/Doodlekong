package com.plcoding.doodlekong.ui.drawing

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.plcoding.doodlekong.databinding.ActivityDrawingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class DrawingActivity : AppCompatActivity() {

    lateinit var binding: ActivityDrawingBinding

    private val viewModel: DrawingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToUIUpdates()
        binding.colorGroup.setOnCheckedChangeListener { _, i ->
            viewModel.checkRadioButton(i)
        }

    }

    private fun subscribeToUIUpdates() {
        lifecycleScope.launchWhenStarted {
            viewModel.selectedColorButtonId.collect { id ->
                binding.colorGroup.check(id)
            }
        }
    }

}