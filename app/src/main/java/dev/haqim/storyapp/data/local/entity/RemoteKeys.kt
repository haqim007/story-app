package dev.haqim.storyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * To store information of fetched latest page
 *
 * @property id
 * @property prevKey
 * @property nextKey
 * @constructor Create empty Remote keys
 */
@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey val id: String,
    val prevKey: Int?,
    val nextKey: Int?
)