package com.example.japanvocalist

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.japanvocalist.MainActivity.Companion.db
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WordEditingActivity : AppCompatActivity() {

    private lateinit var editTextKanji: EditText
    private lateinit var editTextHiragana: EditText
    private lateinit var editTextKorean: EditText
    private lateinit var buttonCancel: Button
    private lateinit var buttonSave: Button

    private lateinit var word: Word

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_editing)
        initializeUIComponents()

        val wordId = intent.getIntExtra("WORD_ID", -1)

        lifecycleScope.launch(Dispatchers.IO) {
            word = db.wordDao().getWordById(wordId) ?: return@launch
            displayWord()
        }

        setupButtonListeners()
    }

    private fun initializeUIComponents() {
        editTextKanji = findViewById(R.id.editTextKanji)
        editTextHiragana = findViewById(R.id.editTextHiragana)
        editTextKorean = findViewById(R.id.editTextKorean)
        buttonCancel = findViewById(R.id.buttonCancel)
        buttonSave = findViewById(R.id.buttonSave)
    }

    private fun displayWord() {
        runOnUiThread {
            editTextKanji.setText(word.kanji)
            editTextHiragana.setText(word.hiragana)
            editTextKorean.setText(word.korean)
        }
    }

    private fun setupButtonListeners() {
        buttonCancel.setOnClickListener { finish() }
        buttonSave.setOnClickListener { handleSave() }
    }

    private fun handleSave() {
        lifecycleScope.launch(Dispatchers.IO) {
            val word = Word(
                id = word.id,
                kanji = editTextKanji.text.toString(),
                hiragana = editTextHiragana.text.toString(),
                korean = editTextKorean.text.toString(),
                categoryId = word.categoryId
            )
            db.wordDao().update(word)
            runOnUiThread {
                finish()
            }
        }
    }
}