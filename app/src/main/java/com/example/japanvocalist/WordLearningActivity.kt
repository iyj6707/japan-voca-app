package com.example.japanvocalist

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.example.japanvocalist.MainActivity.Companion.dataStore
import com.example.japanvocalist.util.buildIndexKey
import com.example.japanvocalist.util.buildKnownWordsKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

@SuppressLint("NewApi")
class WordLearningActivity : AppCompatActivity() {

    private lateinit var textViewKanji: TextView
    private lateinit var textViewHiragana: TextView
    private lateinit var textViewKorean: TextView
    private lateinit var buttonShowHiragana: Button
    private lateinit var buttonShowKorean: Button
    private lateinit var buttonDontKnow: Button
    private lateinit var buttonKnow: Button
    private lateinit var buttonBack: Button
    private lateinit var textViewCounter: TextView
    private lateinit var db: WordRoomDatabase
    private lateinit var category: CategoryDto

    private var offset by Delegates.notNull<Int>()
    private var limit by Delegates.notNull<Int>()
    private var currentIndex by Delegates.notNull<Int>()
    private var wordById = mapOf<Int, Word>()
    private var knownWordsCount = 0

    private val indexList = mutableListOf<Int>()
    private val indexKey: String by lazy {
        buildIndexKey(category.id, offset)
    }
    private val knownWordsKey: String by lazy {
        buildKnownWordsKey(category.id, offset)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_learning)

        db = WordRoomDatabase.getDatabase(applicationContext)

        textViewKanji = findViewById(R.id.textViewKanji)
        textViewHiragana = findViewById(R.id.textViewHiragana)
        textViewKorean = findViewById(R.id.textViewKorean)
        buttonShowHiragana = findViewById(R.id.buttonShowHiragana)
        buttonShowKorean = findViewById(R.id.buttonShowKorean)
        buttonDontKnow = findViewById(R.id.buttonDontKnow)
        buttonKnow = findViewById(R.id.buttonKnow)
        buttonBack = findViewById(R.id.buttonBack)
        textViewCounter = findViewById(R.id.textViewCounter)

        category = intent.getParcelableExtra("CATEGORY", CategoryDto::class.java) ?: return
        offset = intent.getIntExtra("OFFSET", 0)
        limit = intent.getIntExtra("LIMIT", 10)

        lifecycleScope.launch(Dispatchers.IO) {
            loadWords(category, offset, limit)
        }

        buttonShowHiragana.setOnClickListener {
            textViewHiragana.text = wordById[currentIndex]?.hiragana ?: ""
        }

        buttonShowKorean.setOnClickListener {
            textViewKorean.text = wordById[currentIndex]?.korean ?: ""
        }

        buttonDontKnow.setOnClickListener {
            indexList.add(currentIndex)
            showWord(isNext = true)
        }

        buttonKnow.setOnClickListener {
            knownWordsCount++
            updateCounter()
            showWord(isNext = true)
        }

        buttonBack.setOnClickListener {
            finish()
        }
    }

    private suspend fun loadWords(category: CategoryDto, offset: Int, limit: Int) {
        val indexSet = dataStore.data.map {
            it[stringSetPreferencesKey(indexKey)] ?: emptySet()
        }.first()
        indexList.addAll(indexSet.map { it.toInt() })

        knownWordsCount = dataStore.data.map {
            it[intPreferencesKey(knownWordsKey)] ?: 0
        }.first()

        val allWords = db.wordDao().getWordsByCategory(category.id)
        val words = allWords.drop(offset).take(limit).shuffled()
        wordById = words.associateBy { it.id }

        if (indexList.isEmpty()) {
            indexList.addAll(words.map { it.id })
        }

        withContext(Dispatchers.Main) {
            updateCounter()
        }

        showWord(isNext = false)
    }

    private fun updateCounter() {
        textViewCounter.text = "$knownWordsCount/$limit"
    }

    private fun showWord(isNext: Boolean) {
        if (indexList.isEmpty()) {
            displayCompletionMessage()
            return
        }

        if (isNext) {
            indexList.removeAt(0)
        }
        currentIndex = indexList.first()
        val word = wordById[currentIndex]

        displayWord(word)
        saveProgress()
    }

    private fun displayWord(word: Word?) {
        runOnUiThread {
            textViewKanji.text = word?.kanji ?: "No words available"
            textViewHiragana.text = ""
            textViewKorean.text = ""
        }
    }

    private fun displayCompletionMessage() {
        runOnUiThread {
            textViewKanji.text = "모든 단어를 암기했습니다!"
            textViewHiragana.text = ""
            textViewKorean.text = ""
        }
    }

    private fun saveProgress() {
        lifecycleScope.launch(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[stringSetPreferencesKey(indexKey)] =
                    indexList.map { it.toString() }.toSet()
                preferences[intPreferencesKey(knownWordsKey)] = knownWordsCount
            }
        }
    }
}