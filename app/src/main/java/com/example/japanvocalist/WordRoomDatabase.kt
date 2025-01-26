package com.example.japanvocalist

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.japanvocalist.util.populateDatabaseFromCSV
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
                        populateDatabaseFromCSV(
                            context.resources.openRawResource(R.raw.words).bufferedReader(),
                            database.wordDao(),
                            database.categoryDao()
                        )
                    }
                }
            }
        }
    }
}