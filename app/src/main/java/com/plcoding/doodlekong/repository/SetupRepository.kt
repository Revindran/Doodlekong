package com.plcoding.doodlekong.repository

import com.plcoding.doodlekong.data.remote.ws.Room
import com.plcoding.doodlekong.util.Resource

interface SetupRepository {
    suspend fun createRoom(room: Room): Resource<Unit>
    suspend fun joinRoom(userName: String, roomName: String): Resource<Unit>
    suspend fun getRooms(searchQuery: String): Resource<List<Room>>
}