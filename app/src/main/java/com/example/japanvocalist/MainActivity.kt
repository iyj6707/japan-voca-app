package com.example.japanvocalist

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var db: WordRoomDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = WordRoomDatabase.getDatabase(applicationContext)

        val listViewLevels: ListView = findViewById(R.id.listViewLevels)
        CoroutineScope(Dispatchers.IO).launch {
            val categories = db.categoryDao().getAll().toList()

            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_list_item_1,
                    categories.map { it.name }
                )
                listViewLevels.adapter = adapter
            }

            listViewLevels.setOnItemClickListener { _, _, position, _ ->
                val categoryDto = categories[position].let {
                    CategoryDto(it.id, it.name)
                }
                val intent = Intent(this@MainActivity, WordRangeActivity::class.java)
                intent.putExtra("CATEGORY", categoryDto)
                startActivity(intent)
            }
        }
    }
}