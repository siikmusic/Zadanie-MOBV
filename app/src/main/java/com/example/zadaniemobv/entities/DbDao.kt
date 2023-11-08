package com.example.zadaniemobv.entities

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DbDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofence(item: GeofenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserItems(items: List<UserEntity>)

    @Query("select * from users where uid=:uid")
    fun getUserItem(uid: String): LiveData<UserEntity?>

    @Query("select * from users")
    fun getUsers(): LiveData<List<UserEntity>?>

    @Query("select * from users")
    suspend fun getUsersList(): List<UserEntity>?

    @Query("delete from users")
    suspend fun deleteUserItems()

}