package com.example.japanvocalist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WordDao {
    @Insert
    suspend fun insert(word: Word)

    @Query("SELECT COUNT(*) FROM words WHERE categoryId = :categoryId")
    suspend fun getCountByCategory(categoryId: Int): Int

    @Query("SELECT * FROM words WHERE categoryId = :categoryId")
    suspend fun getWordsByCategory(categoryId: Int): List<Word>
}