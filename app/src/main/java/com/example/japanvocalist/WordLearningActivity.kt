package com.example.japanvocalist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
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
    private lateinit var buttonGotIt: Button
    private lateinit var buttonHardToRemember: Button
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

    private val indexKey: String by lazy { buildIndexKey(category.id, offset) }
    private val knownWordsKey: String by lazy { buildKnownWordsKey(category.id, offset) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_learning)
        initializeUIComponents()
        initializeWordDatabase()

        category = intent.getParcelableExtra("CATEGORY", CategoryDto::class.java) ?: return
        offset = intent.getIntExtra("OFFSET", 0)
        limit = intent.getIntExtra("LIMIT", 10)

        if (category.id == HARD_TO_REMEMBER_CATEGORY_ID) {
            buttonGotIt.visibility = View.VISIBLE
            buttonHardToRemember.visibility = View.GONE
        } else {
            buttonGotIt.visibility = View.GONE
            buttonHardToRemember.visibility = View.VISIBLE
        }

        lifecycleScope.launch(Dispatchers.IO) {
            loadWords()
        }

        setupButtonListeners()
    }

    private fun initializeUIComponents() {
        textViewKanji = findViewById(R.id.textViewKanji)
        textViewHiragana = findViewById(R.id.textViewHiragana)
        textViewKorean = findViewById(R.id.textViewKorean)
        buttonShowHiragana = findViewById(R.id.buttonShowHiragana)
        buttonShowKorean = findViewById(R.id.buttonShowKorean)
        buttonDontKnow = findViewById(R.id.buttonDontKnow)
        buttonKnow = findViewById(R.id.buttonKnow)
        buttonHardToRemember = findViewById(R.id.buttonHardToRemember)
        buttonGotIt = findViewById(R.id.buttonGotIt)
        buttonBack = findViewById(R.id.buttonBack)
        textViewCounter = findViewById(R.id.textViewCounter)
    }

    private fun initializeWordDatabase() {
        db = WordRoomDatabase.getDatabase(applicationContext)
    }

    private fun setupButtonListeners() {
        buttonShowHiragana.setOnClickListener { showHiragana() }
        buttonShowKorean.setOnClickListener { showKorean() }
        buttonDontKnow.setOnClickListener { handleDontKnow() }
        buttonKnow.setOnClickListener { handleKnow() }
        buttonHardToRemember.setOnClickListener { handleHardToRemember() }
        buttonGotIt.setOnClickListener { handleGotIt() }
        buttonBack.setOnClickListener { finish() }
    }

    private fun showHiragana() {
        textViewHiragana.text = wordById[currentIndex]?.hiragana ?: ""
    }

    private fun showKorean() {
        textViewKorean.text = wordById[currentIndex]?.korean ?: ""
    }

    private fun handleDontKnow() {
        indexList.add(currentIndex)
        showNextWord()
    }

    private fun handleKnow() {
        knownWordsCount++
        updateCounter()
        showNextWord()
    }

    private fun handleHardToRemember() {
        indexList.add(currentIndex)
        lifecycleScope.launch(Dispatchers.IO) {
            db.wordDao().insert(
                Word(
                    kanji = wordById[currentIndex]?.kanji ?: "",
                    hiragana = wordById[currentIndex]?.hiragana ?: "",
                    korean = wordById[currentIndex]?.korean ?: "",
                    categoryId = HARD_TO_REMEMBER_CATEGORY_ID
                )
            )
            showNextWord()
        }
    }

    private fun handleGotIt() {
        knownWordsCount++
        updateCounter()
        lifecycleScope.launch(Dispatchers.IO) {
            db.wordDao().deleteById(currentIndex)
            showNextWord()
        }
    }

    private suspend fun loadWords() {
        val indexSet =
            dataStore.data.map { it[stringSetPreferencesKey(indexKey)] ?: emptySet() }.first()
        indexList.addAll(indexSet.map { it.toInt() })
        knownWordsCount = dataStore.data.map { it[intPreferencesKey(knownWordsKey)] ?: 0 }.first()

        val allWords = db.wordDao().getWordsByCategory(category.id)
        val words = allWords.drop(offset).take(limit).shuffled()
        wordById = words.associateBy { it.id }

        if (indexList.isEmpty()) {
            indexList.addAll(words.map { it.id })
        }

        updateCounter()
        showNextWord(isNext = false)
    }

    private fun updateCounter() {
        runOnUiThread { textViewCounter.text = "$knownWordsCount/${wordById.size}" }
    }

    private fun showNextWord(isNext: Boolean = true) {
        if (isNext) {
            indexList.removeAt(0)
        }

        if (indexList.isEmpty()) {
            displayCompletionMessage()
            return
        }

        currentIndex = indexList.first()
        displayWord(wordById[currentIndex])
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
            // TODO : 순서만 다르게 한 set을 저장할 방법이 없어서 임시방편으로 짠 코드
            dataStore.edit { preferences ->
                preferences.remove(stringSetPreferencesKey(indexKey))
            }
            dataStore.edit { preferences ->
                preferences[stringSetPreferencesKey(indexKey)] =
                    indexList.map { it.toString() }.toSet()
                preferences[intPreferencesKey(knownWordsKey)] = knownWordsCount
            }
        }
    }
}