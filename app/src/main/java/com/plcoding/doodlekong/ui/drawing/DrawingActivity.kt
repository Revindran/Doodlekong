package com.plcoding.doodlekong.ui.drawing

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.plcoding.doodlekong.R
import com.plcoding.doodlekong.adapters.ChatMessageAdapter
import com.plcoding.doodlekong.data.remote.ws.models.DrawAction
import com.plcoding.doodlekong.data.remote.ws.models.GameError
import com.plcoding.doodlekong.data.remote.ws.models.JoinRoomHandshake
import com.plcoding.doodlekong.databinding.ActivityDrawingBinding
import com.plcoding.doodlekong.util.Constants
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class DrawingActivity : AppCompatActivity() {

    lateinit var binding: ActivityDrawingBinding

    private val viewModel: DrawingViewModel by viewModels()

    private val args: DrawingActivityArgs by navArgs()

    @Inject
    lateinit var clientID: String

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var rvPlayers: RecyclerView

    private lateinit var chatMessageAdapter: ChatMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToUIUpdates()
        listenToConnectionEvents()
        listenToSocketEvents()
        setupRecyclerView()

        binding.colorGroup.setOnCheckedChangeListener { _, i ->
            viewModel.checkRadioButton(i)
        }

        binding.drawingView.roomName = args.roomName

        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        toggle.syncState()

        val header = layoutInflater.inflate(R.layout.nav_drawer_header, binding.navView)
        rvPlayers = header.findViewById(R.id.rvPlayers)
        binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        binding.ibPlayers.setOnClickListener {
            binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            binding.root.openDrawer(GravityCompat.START)
        }

        binding.root.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit
            override fun onDrawerOpened(drawerView: View) = Unit

            override fun onDrawerClosed(drawerView: View) {
                binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }

            override fun onDrawerStateChanged(newState: Int) = Unit
        })

        binding.ibUndo.setOnClickListener {
            if (binding.drawingView.isUserDrawing) {
                binding.drawingView.undo()
                viewModel.sendBaseModel(DrawAction(DrawAction.ACTION_UNDO))
            }
        }

        binding.drawingView.setOnDrawListener {
            if (binding.drawingView.isUserDrawing) {
                viewModel.sendBaseModel(it)
            }
        }

    }

    private fun listenToConnectionEvents() = lifecycleScope.launchWhenStarted {
        viewModel.connectionEvent.collect { event ->
            when (event) {
                is WebSocket.Event.OnConnectionOpened<*> -> {
                    viewModel.sendBaseModel(
                        JoinRoomHandshake(args.userName, args.roomName, clientID)
                    )
                    viewModel.connectionProgressBarVisibility(false)
                }
                is WebSocket.Event.OnConnectionClosed -> {
                    viewModel.connectionProgressBarVisibility(false)
                }
                is WebSocket.Event.OnConnectionFailed -> {
                    viewModel.connectionProgressBarVisibility(false)
                    event.throwable.printStackTrace()
                    Snackbar.make(
                        binding.root,
                        R.string.error_coonection_falied,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> Unit
            }
        }
    }

    private fun selectColor(color: Int) {
        binding.drawingView.setColor(color)
        binding.drawingView.setThickness(Constants.DEFAULT_PAINT_THICKNESS)
    }

    private fun subscribeToUIUpdates() {
        lifecycleScope.launchWhenStarted {
            viewModel.selectedColorButtonId.collect { id ->
                binding.colorGroup.check(id)
                when (id) {
                    R.id.rbBlack -> selectColor(Color.BLACK)
                    R.id.rbBlue -> selectColor(Color.BLUE)
                    R.id.rbGreen -> selectColor(Color.GREEN)
                    R.id.rbOrange -> selectColor(
                        ContextCompat.getColor(this@DrawingActivity, R.color.orange)
                    )
                    R.id.rbRed -> selectColor(Color.RED)
                    R.id.rbYellow -> selectColor(Color.YELLOW)
                    R.id.rbEraser -> {
                        binding.drawingView.setColor(Color.WHITE)
                        binding.drawingView.setThickness(40f)
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.chooseWordOverlay.collect { isVisible ->
                binding.chooseWordOverlay.isVisible = isVisible
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.connectionProgressVisible.collect { isVisible ->
                binding.connectionProgressBar.isVisible = isVisible
            }
        }
    }

    private fun listenToSocketEvents() = lifecycleScope.launchWhenStarted {
        viewModel.socketEvent.collect { event ->
            when (event) {
                is DrawingViewModel.SocketEvent.DrawDataEvent -> {
                    val drawData = event.data
                    if (!binding.drawingView.isUserDrawing) {
                        when (drawData.motionEvent) {
                            MotionEvent.ACTION_DOWN -> binding.drawingView.startedTouchExternally(
                                drawData
                            )
                            MotionEvent.ACTION_MOVE -> binding.drawingView.movedTouchExternally(
                                drawData
                            )
                            MotionEvent.ACTION_UP -> binding.drawingView.releasedTouchExternally(
                                drawData
                            )
                        }
                    }
                }
                is DrawingViewModel.SocketEvent.UndoEvent -> {
                    binding.drawingView.undo()
                }
                is DrawingViewModel.SocketEvent.GameErrorEvent -> {
                    when (event.data.errorType) {
                        GameError.ERROR_ROOM_NOT_FOUND -> finish()
                    }
                }
                else -> Unit
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() = binding.rvChat.apply {
        chatMessageAdapter = ChatMessageAdapter(args.userName)
        adapter = chatMessageAdapter
        layoutManager = LinearLayoutManager(this@DrawingActivity)
    }

}