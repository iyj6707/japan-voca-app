package com.example.japanvocalist

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class WordLearningActivity : AppCompatActivity() {

    private lateinit var textViewKanji: TextView
    private lateinit var textViewHiragana: TextView
    private lateinit var textViewKorean: TextView
    private lateinit var buttonShowHiragana: Button
    private lateinit var buttonShowKorean: Button
    private lateinit var buttonDontKnow: Button
    private lateinit var buttonKnow: Button
    private lateinit var buttonBack: Button
    private lateinit var db: WordRoomDatabase
    private lateinit var indexKey: String

    private var currentIndex by Delegates.notNull<Int>()

    private var wordById = mapOf<Int, Word>()
    private var indexList = mutableListOf<Int>()

    @SuppressLint("NewApi")
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

        val category = intent.getParcelableExtra("CATEGORY", CategoryDto::class.java)!!
        val offset = intent.getIntExtra("OFFSET", 0)
        val limit = intent.getIntExtra("LIMIT", 10)

        val preferences = getSharedPreferences("WordLearningPrefs", MODE_PRIVATE)
        indexKey = "indexList_${category.id}_$offset"
        val indexSet = preferences.getStringSet(indexKey, setOf()) ?: setOf()
        indexList += indexSet.map { it.toInt() }

        loadWords(category, offset, limit)

        buttonShowHiragana.setOnClickListener {
            textViewHiragana.text = wordById[currentIndex]?.hiragana ?: ""
        }

        buttonShowKorean.setOnClickListener {
            textViewKorean.text = wordById[currentIndex]?.korean ?: ""
        }

        buttonDontKnow.setOnClickListener {
            indexList.add(currentIndex)
            showNextWord()
        }

        buttonKnow.setOnClickListener {
            showNextWord()
        }

        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun loadWords(category: CategoryDto, offset: Int, limit: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val allWords = db.wordDao().getWordsByCategory(category.id)
            val words = allWords.drop(offset).take(limit).shuffled()
            wordById = words.associateBy { it.id }

            runOnUiThread {
                if (indexList.isEmpty()) {
                    indexList.addAll(words.map { it.id })
                }
                showNextWord()
            }
        }
    }

    private fun showNextWord() {
        if (indexList.isEmpty()) {
            displayCompletionMessage()
            return
        }

        currentIndex = indexList.removeAt(0)
        val word = wordById[currentIndex]

        textViewKanji.text = word?.kanji ?: "No words available"
        textViewHiragana.text = ""
        textViewKorean.text = ""
    }

    private fun displayCompletionMessage() {
        textViewKanji.text = "모든 단어를 암기했습니다!"
        textViewHiragana.text = ""
        textViewKorean.text = ""
    }

    override fun onPause() {
        super.onPause()
        val preferences = getSharedPreferences("WordLearningPrefs", MODE_PRIVATE)
        indexList.add(0, currentIndex)
        preferences.edit()
            .putStringSet(indexKey, indexList.map { it.toString() }.toSet())
            .apply()
    }
}