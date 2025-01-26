package com.example.japanvocalist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.japanvocalist.util.populateDatabaseFromCSV
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader


class MainActivity : AppCompatActivity() {

    private val openCsvFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { lifecycleScope.launch { insertCsvFileToDB(it) } }
    }

    private lateinit var db: WordRoomDatabase
    private lateinit var listViewAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = WordRoomDatabase.getDatabase(applicationContext)

        setupListView()
        observeCategories()
    }

    private fun setupListView() {
        val listViewLevels: ListView = findViewById(R.id.listViewLevels)
        listViewAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listViewLevels.adapter = listViewAdapter

        listViewLevels.setOnItemClickListener { _, _, position, _ ->
            val categoryDto = (listViewLevels.getItemAtPosition(position) as? Category)?.let {
                CategoryDto(it.id, it.name)
            }
            categoryDto?.let {
                val intent = Intent(this@MainActivity, WordRangeActivity::class.java)
                intent.putExtra("CATEGORY", categoryDto)
                startActivity(intent)
            }
        }
    }

    private fun observeCategories() {
        lifecycleScope.launch {
            db.categoryDao().getAll().collect { categories ->
                updateListView(categories)
            }
        }
    }

    private fun updateListView(categories: List<Category>) {
        listViewAdapter.clear()
        listViewAdapter.addAll(categories.map { it.name })
        listViewAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_word -> {
                openCsvFileLauncher.launch("*/*")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private suspend fun insertCsvFileToDB(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            populateDatabaseFromCSV(reader, db.wordDao(), db.categoryDao())
        }
    }
}