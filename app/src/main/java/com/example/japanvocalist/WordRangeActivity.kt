package com.example.japanvocalist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class WordRangeActivity : AppCompatActivity() {

    private lateinit var buttonBack: Button
    private lateinit var db: WordRoomDatabase
    private lateinit var listViewRanges: ListView
    private lateinit var categoryDto: CategoryDto
    private var rangeStep by Delegates.notNull<Int>()

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_range)

        db = WordRoomDatabase.getDatabase(applicationContext)

        buttonBack = findViewById(R.id.buttonBack)
        listViewRanges = findViewById(R.id.listViewRanges)
        categoryDto = intent.getParcelableExtra("CATEGORY", CategoryDto::class.java)!!

        val preferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE)
        rangeStep = preferences.getInt(buildRangeStepKey(categoryDto.id), 50)

        updateListViewRanges(categoryDto.id)

        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun updateListViewRanges(categoryId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val wordCountByCategory = db.wordDao().getCountByCategory(categoryId)
            val startToEnd = (0 until wordCountByCategory step rangeStep).map {
                it to minOf(
                    it + rangeStep,
                    wordCountByCategory
                )
            }

            withContext(Dispatchers.Main) {
                val adapter = CustomAdapter(
                    categoryId,
                    this@WordRangeActivity,
                    startToEnd
                )
                listViewRanges.adapter = adapter
                listViewRanges.setOnItemClickListener { _, _, position, _ ->
                    val range = startToEnd[position]
                    val intent = Intent(this@WordRangeActivity, WordLearningActivity::class.java)
                    intent.putExtra("CATEGORY", categoryDto)
                    intent.putExtra("OFFSET", range.first)
                    intent.putExtra("LIMIT", rangeStep)
                    startActivity(intent)
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.word_range_menu_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_set_range -> {
                showRangeInputDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showRangeInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("범위 설정")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "숫자를 입력하세요"
        builder.setView(input)

        builder.setPositiveButton("확인") { dialog, _ ->
            val inputText = input.text.toString()
            val newRangeStep = inputText.toIntOrNull()
            if (newRangeStep != null && newRangeStep > 0) {
                setNewRangeStep(newRangeStep)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun setNewRangeStep(newRangeStep: Int) {
        rangeStep = newRangeStep
        val preferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE)
        preferences.edit()
            .putInt(buildRangeStepKey(categoryDto.id), rangeStep)
            .apply()
        updateListViewRanges(categoryDto.id)
    }

    private fun buildRangeStepKey(categoryId: Int): String {
        return "range_step:${categoryId}"
    }
}
