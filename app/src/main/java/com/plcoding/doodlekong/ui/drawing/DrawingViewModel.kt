package com.plcoding.doodlekong.ui.drawing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.plcoding.doodlekong.R
import com.plcoding.doodlekong.data.remote.ws.DrawingApi
import com.plcoding.doodlekong.data.remote.ws.models.*
import com.plcoding.doodlekong.data.remote.ws.models.DrawAction.Companion.ACTION_UNDO
import com.plcoding.doodlekong.util.DispatcherProvider
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val api: DrawingApi,
    private val dispatchers: DispatcherProvider,
    private val gson: Gson
) : ViewModel() {


    sealed class SocketEvent {
        data class ChatMessageEvent(val data: ChatMessage) : SocketEvent()
        data class AnnouncementEvent(val data: Announcement) : SocketEvent()
        data class GameStateEvent(val data: GameState) : SocketEvent()
        data class DrawDataEvent(val data: DrawData) : SocketEvent()
        data class NewWordsEvent(val data: NewWords) : SocketEvent()
        data class ChosenWordEvent(val data: ChosenWord) : SocketEvent()
        data class GameErrorEvent(val data: GameError) : SocketEvent()
        data class RoundDrawInfoEvent(val data: RoundDrawInfo) : SocketEvent()
        object UndoEvent : SocketEvent()
    }

    private val _selectedColorButtonId = MutableStateFlow(R.id.rbBlack)
    val selectedColorButtonId: StateFlow<Int> = _selectedColorButtonId

    private val _connectionProgressVisible = MutableStateFlow(true)
    val connectionProgressVisible: StateFlow<Boolean> = _connectionProgressVisible

    private val _chooseWordOverlay = MutableStateFlow(false)
    val chooseWordOverlay: StateFlow<Boolean> = _chooseWordOverlay

    private val _connectionEventChannel = Channel<WebSocket.Event>()
    val connectionEvent = _connectionEventChannel.receiveAsFlow().flowOn(dispatchers.io)

    private val _socketEventChannel = Channel<SocketEvent>()
    val socketEvent = _socketEventChannel.receiveAsFlow().flowOn(dispatchers.io)


    init {
        observeEvents()
        observeBaseModels()
    }

    fun connectionProgressBarVisibility(isVisible: Boolean) {
        _connectionProgressVisible.value = isVisible
    }

    fun chooseWordOverlayVisibility(isVisible: Boolean) {
        _chooseWordOverlay.value = isVisible
    }

    fun checkRadioButton(id: Int) {
        _selectedColorButtonId.value = id
    }

    private fun observeEvents() {
        viewModelScope.launch(dispatchers.io) {
            api.observeEvents().collect { event ->
                _connectionEventChannel.send(event)
            }
        }
    }

    private fun observeBaseModels() {
        viewModelScope.launch(dispatchers.io) {
            api.observeBaseModels().collect { data ->
                when (data) {
                    is DrawData -> {
                        _socketEventChannel.send(SocketEvent.DrawDataEvent(data))
                    }
                    is DrawAction -> {
                        when (data.action) {
                            ACTION_UNDO -> _socketEventChannel.send(SocketEvent.UndoEvent)
                        }
                    }
                    is GameError -> _socketEventChannel.send(SocketEvent.GameErrorEvent(data))
                    is Ping -> sendBaseModel(Ping())
                }
            }
        }
    }

    fun sendBaseModel(data: BaseModel) {
        viewModelScope.launch(dispatchers.io) {
            api.sendBaseModel(data)
        }
    }

}