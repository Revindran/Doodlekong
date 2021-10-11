package com.plcoding.doodlekong.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.doodlekong.data.remote.ws.Room
import com.plcoding.doodlekong.repository.SetupRepository
import com.plcoding.doodlekong.util.Constants.MAX_ROOM_NAME_LENGTH
import com.plcoding.doodlekong.util.Constants.MIN_ROOM_NAME_LENGTH
import com.plcoding.doodlekong.util.DispatcherProvider
import com.plcoding.doodlekong.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateRoomViewModel @Inject constructor(
    private val repository: SetupRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    sealed class SetupEvent {
        object InputEmptyError : SetupEvent()
        object InputTooShortEvent : SetupEvent()
        object InputTooLongEvent : SetupEvent()

        data class CreateRoomEvent(val room: Room) : SetupEvent()
        data class CreateRoomErrorEvent(val error: String) : SetupEvent()
        data class JoinRoomEvent(val roomName: String) : SetupEvent()
        data class JoinRoomErrorEvent(val error: String) : SetupEvent()
    }

    private val _setupEvent = MutableSharedFlow<SetupEvent>()
    val setupEvent: SharedFlow<SetupEvent> = _setupEvent


    fun createRoom(room: Room) {
        viewModelScope.launch(dispatcherProvider.main) {
            val trimmedRoomName = room.name.trim()
            when {
                trimmedRoomName.isEmpty() -> _setupEvent.emit(SetupEvent.InputEmptyError)
                trimmedRoomName.length < MIN_ROOM_NAME_LENGTH -> _setupEvent.emit(SetupEvent.InputTooShortEvent)
                trimmedRoomName.length > MAX_ROOM_NAME_LENGTH -> _setupEvent.emit(SetupEvent.InputTooLongEvent)
                else -> {
                    val result = repository.createRoom(room)
                    if (result is Resource.Success) {
                        _setupEvent.emit(SetupEvent.CreateRoomEvent(room))
                    } else {
                        _setupEvent.emit(
                            SetupEvent.CreateRoomErrorEvent(
                                result.message ?: return@launch
                            )
                        )
                    }
                }
            }
        }
    }


    fun joinRoom(username: String, roomName: String) {
        viewModelScope.launch(dispatcherProvider.main) {
            val result = repository.joinRoom(userName = username, roomName)
            if (result is Resource.Success) {
                _setupEvent.emit(SetupEvent.JoinRoomEvent(roomName))
            } else {
                _setupEvent.emit(
                    SetupEvent.JoinRoomErrorEvent(
                        result.message ?: return@launch
                    )
                )
            }
        }
    }


}