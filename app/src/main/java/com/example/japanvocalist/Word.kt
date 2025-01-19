package com.example.japanvocalist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hiragana: String,
    val kanji: String,
    val korean: String,
    val categoryId: Int,
)