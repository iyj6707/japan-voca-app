package com.example.japanvocalist.util

import com.example.japanvocalist.Category
import com.example.japanvocalist.CategoryDao
import com.example.japanvocalist.Word
import com.example.japanvocalist.WordDao
import kotlinx.coroutines.flow.first
import java.io.BufferedReader

suspend fun populateDatabaseFromCSV(
    csvReader: BufferedReader,
    wordDao: WordDao,
    categoryDao: CategoryDao
) {
    val tempWords = mutableListOf<TempWord>()
    csvReader.useLines { lines ->
        lines.forEach { line ->
            val parts = line.split(',')
            if (parts.size >= 3) {
                tempWords.add(
                    TempWord(
                        kanji = parts[0],
                        hiragana = parts[1],
                        korean = parts[2],
                        categoryName = parts[3]
                    )
                )
            }
        }
    }
    val categoryNames = tempWords.map { it.categoryName }.distinct()
    categoryNames.forEach { categoryName ->
        categoryDao.insert(Category(name = categoryName))
    }
    val categoryNameById = categoryDao.getAll().first().associateBy { it.name }

    tempWords.forEach { tempWord ->
        val categoryId = categoryNameById[tempWord.categoryName]?.id ?: 0
        wordDao.insert(
            Word(
                kanji = tempWord.kanji,
                hiragana = tempWord.hiragana,
                korean = tempWord.korean,
                categoryId = categoryId
            )
        )
    }
}

class TempWord(
    val kanji: String,
    val hiragana: String,
    val korean: String,
    val categoryName: String
)