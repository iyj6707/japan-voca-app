package com.example.japanvocalist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WordDao {
    @Insert
    suspend fun insert(word: Word)

    @Query("DELETE FROM words WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Update
    suspend fun update(word: Word)

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Int): Word?

    @Query("SELECT COUNT(*) FROM words WHERE categoryId = :categoryId")
    suspend fun getCountByCategory(categoryId: Int): Int

    @Query("SELECT * FROM words WHERE categoryId = :categoryId")
    suspend fun getWordsByCategory(categoryId: Int): List<Word>
}