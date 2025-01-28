package com.example.japanvocalist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.japanvocalist.util.populateDatabaseFromCSV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader


class MainActivity : AppCompatActivity() {

    private lateinit var db: WordRoomDatabase
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = WordRoomDatabase.getDatabase(applicationContext)

        setupListView()
        observeCategories()
    }

    private fun setupListView() {
        val categoryListView: ListView = findViewById(R.id.categoryListView)
        categoryAdapter = CategoryAdapter(this, mutableListOf(), this::deleteCategory)
        categoryListView.adapter = categoryAdapter

        categoryListView.setOnItemClickListener { _, _, position, _ ->
            val categoryDto = (categoryListView.getItemAtPosition(position) as? Category)?.let {
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
        categoryAdapter.clear()
        categoryAdapter.addAll(categories)
        categoryAdapter.notifyDataSetChanged()
    }

    private fun deleteCategory(category: Category) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.categoryDao().deleteById(category.id)
            }
            categoryAdapter.remove(category)
            categoryAdapter.notifyDataSetChanged()
        }
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

    private val openCsvFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { lifecycleScope.launch { insertCsvFileToDB(it) } }
    }

    private suspend fun insertCsvFileToDB(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            populateDatabaseFromCSV(reader, db.wordDao(), db.categoryDao())
        }
    }
}