package com.example.japanvocalist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val RANGE_STEP = 50

class WordRangeActivity : AppCompatActivity() {

    private lateinit var buttonBack: Button
    private lateinit var db: WordRoomDatabase

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_range)

        db = WordRoomDatabase.getDatabase(applicationContext)

        buttonBack = findViewById(R.id.buttonBack)
        val listViewRanges: ListView = findViewById(R.id.listViewRanges)
        val categoryDto = intent.getParcelableExtra("CATEGORY", CategoryDto::class.java)!!

        CoroutineScope(Dispatchers.IO).launch {
            val wordCountByCategory = db.wordDao().getCountByCategory(categoryDto.id)
            val startToEnd = (0 until wordCountByCategory step RANGE_STEP).map {
                it to minOf(it + RANGE_STEP, wordCountByCategory)
            }

            withContext(Dispatchers.Main) {
                val adapter = CustomAdapter(
                    categoryDto.id,
                    this@WordRangeActivity,
                    startToEnd,
                )
                listViewRanges.adapter = adapter

                listViewRanges.setOnItemClickListener { _, _, position, _ ->
                    val range = startToEnd[position]
                    val intent = Intent(this@WordRangeActivity, WordLearningActivity::class.java)
                    intent.putExtra("CATEGORY", categoryDto)
                    intent.putExtra("OFFSET", range.first)
                    intent.putExtra("LIMIT", RANGE_STEP)
                    startActivity(intent)
                }
            }
        }

        buttonBack.setOnClickListener {
            finish()
        }
    }
}
