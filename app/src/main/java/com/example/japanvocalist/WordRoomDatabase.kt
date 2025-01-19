package com.example.japanvocalist

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Word::class, Category::class], version = 1, exportSchema = false)
abstract class WordRoomDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: WordRoomDatabase? = null

        fun getDatabase(context: Context): WordRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordRoomDatabase::class.java,
                    "database"
                )
                    .addCallback(WordRoomDatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class WordRoomDatabaseCallback(
            private val context: Context
        ) : Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabaseFromCSV(context, database.wordDao(), database.categoryDao())
                    }
                }
            }
        }

        private suspend fun populateDatabaseFromCSV(
            context: Context,
            wordDao: WordDao,
            categoryDao: CategoryDao
        ) {
            val tempWords = mutableListOf<TempWord>()
            context.resources.openRawResource(R.raw.words).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split(',')
                    if (parts.size >= 3) {
                        tempWords.add(
                            TempWord(
                                hiragana = parts[0],
                                kanji = parts[1],
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
            val categoryNameById = categoryDao.getAll().associateBy { it.name }

            tempWords.forEach { tempWord ->
                val categoryId = categoryNameById[tempWord.categoryName]?.id ?: 0
                wordDao.insert(
                    Word(
                        hiragana = tempWord.hiragana,
                        kanji = tempWord.kanji,
                        korean = tempWord.korean,
                        categoryId = categoryId
                    )
                )
            }
        }

        class TempWord(
            val hiragana: String,
            val kanji: String,
            val korean: String,
            val categoryName: String
        )
    }
}