package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMatchDao {
    @Query("SELECT * FROM favorite_matches")
    fun getAllFavoriteMatches(): Flow<List<FavoriteMatch>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_matches WHERE matchId = :matchId LIMIT 1)")
    fun isMatchFavorite(matchId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteMatch(match: FavoriteMatch)

    @Query("DELETE FROM favorite_matches WHERE matchId = :matchId")
    suspend fun deleteFavoriteMatch(matchId: String)
}

@Dao
interface FavoriteChannelDao {
    @Query("SELECT * FROM favorite_channels")
    fun getAllFavoriteChannels(): Flow<List<FavoriteChannel>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_channels WHERE channelId = :channelId LIMIT 1)")
    fun isChannelFavorite(channelId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteChannel(channel: FavoriteChannel)

    @Query("DELETE FROM favorite_channels WHERE channelId = :channelId")
    suspend fun deleteFavoriteChannel(channelId: String)
}

@Dao
interface ChatCommentDao {
    @Query("SELECT * FROM chat_comments WHERE targetId = :targetId ORDER BY timestamp ASC")
    fun getCommentsForTarget(targetId: String): Flow<List<ChatComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: ChatComment)

    @Query("DELETE FROM chat_comments WHERE targetId = :targetId")
    suspend fun clearCommentsForTarget(targetId: String)
}
