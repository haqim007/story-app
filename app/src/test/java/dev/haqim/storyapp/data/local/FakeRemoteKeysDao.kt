package dev.haqim.storyapp.data.local

import dev.haqim.storyapp.data.local.entity.RemoteKeys
import dev.haqim.storyapp.data.local.room.RemoteKeysDao

class FakeRemoteKeysDao : RemoteKeysDao {
    private var remoteKeys = mutableListOf<RemoteKeys>()
    override suspend fun insertAll(remoteKey: List<RemoteKeys>) {
        this.remoteKeys.addAll(remoteKey)
    }

    override suspend fun getRemoteKeyById(id: String): RemoteKeys? {
        return this.remoteKeys.find { it.id == id }
    }

    override suspend fun clearRemoteKeys() {
        this.remoteKeys.clear()
    }


}