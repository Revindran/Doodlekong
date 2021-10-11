package com.plcoding.doodlekong.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.doodlekong.data.remote.ws.Room
import com.plcoding.doodlekong.repository.SetupRepository
import com.plcoding.doodlekong.util.Constants.MAX_ROOM_NAME_LENGTH
import com.plcoding.doodlekong.util.Constants.MAX_USER_NAME_LENGTH
import com.plcoding.doodlekong.util.Constants.MIN_ROOM_NAME_LENGTH
import com.plcoding.doodlekong.util.Constants.MIN_USER_NAME_LENGTH
import com.plcoding.doodlekong.util.DispatcherProvider
import com.plcoding.doodlekong.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetUpViewModel @Inject constructor(
    private val repository: SetupRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    sealed class SetupEvent {
        object InputEmptyError : SetupEvent()
        object InputTooShortEvent : SetupEvent()
        object InputTooLongEvent : SetupEvent()

        data class CreateRoomEvent(val room: Room) : SetupEvent()
        data class CreateRoomErrorEvent(val error: String) : SetupEvent()

        data class NavigateToSelectedRoomEvent(val username: String) : SetupEvent()

        data class GetRoomEvent(val rooms: List<Room>) : SetupEvent()
        data class GetRoomErrorEvent(val error: String) : SetupEvent()

        object GetRoomLoadingEvent : SetupEvent()
        object GetRoomEmptyEvent : SetupEvent()

        data class JoinRoomEvent(val roomName: String) : SetupEvent()
        data class JoinRoomErrorEvent(val error: String) : SetupEvent()
    }

    private val _setupEvent = MutableSharedFlow<SetupEvent>()
    val setupEvent: SharedFlow<SetupEvent> = _setupEvent

    private val _rooms = MutableStateFlow<SetupEvent>(SetupEvent.GetRoomEmptyEvent)
    val room: StateFlow<SetupEvent> = _rooms


    fun validateUserNameAndNavigateToSelectedRoom(username: String) {
        viewModelScope.launch(dispatcherProvider.main) {
            val trimmedUserName = username.trim()
            when {
                trimmedUserName.isEmpty() -> _setupEvent.emit(SetupEvent.InputEmptyError)
                trimmedUserName.length < MIN_USER_NAME_LENGTH -> _setupEvent.emit(SetupEvent.InputTooShortEvent)
                trimmedUserName.length > MAX_USER_NAME_LENGTH -> _setupEvent.emit(SetupEvent.InputTooLongEvent)
                else -> _setupEvent.emit(SetupEvent.NavigateToSelectedRoomEvent(username))
            }
        }
    }


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
                        _rooms.emit(SetupEvent.CreateRoomEvent(room))
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


    fun getRooms(searchQuery: String) {
        _rooms.value = SetupEvent.GetRoomLoadingEvent
        viewModelScope.launch(dispatcherProvider.main) {
            val result = repository.getRooms(searchQuery)
            if (result is Resource.Success) {
                _rooms.value = SetupEvent.GetRoomEvent(result.data ?: return@launch)
            } else {
                _setupEvent.emit(
                    SetupEvent.GetRoomErrorEvent(
                        result.message ?: return@launch
                    )
                )
            }
        }
    }

    fun joinRoom(username: String, roomName: String) {
        viewModelScope.launch(dispatcherProvider.main) {
            val result = repository.joinRoom(userName = username, roomName)
            if (result is Resource.Success) {
                _rooms.value = SetupEvent.JoinRoomEvent(roomName)
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