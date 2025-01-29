package com.example.japanvocalist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.japanvocalist.MainActivity.Companion.dataStore
import com.example.japanvocalist.util.buildIndexKey
import com.example.japanvocalist.util.buildKnownWordsKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RangeAdapter(
    private val categoryId: Int,
    context: Context,
    ranges: List<Pair<Int, Int>>,
) : ArrayAdapter<Pair<Int, Int>>(context, 0, ranges) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)!!
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_with_menu, parent, false)

        val textViewRange = view.findViewById<TextView>(R.id.textViewRange)
        val iconMenu = view.findViewById<ImageView>(R.id.iconMenu)

        textViewRange.text = "${item.first + 1}-${item.second}"

        iconMenu.setOnClickListener {
            val popup = PopupMenu(context, iconMenu)
            popup.inflate(R.menu.word_range_list_menu_options)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_reset -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            context.dataStore.edit { preferences ->
                                val indexKey = buildIndexKey(categoryId, item.first)
                                val knownWordsKey = buildKnownWordsKey(categoryId, item.first)
                                preferences.remove(stringSetPreferencesKey(indexKey))
                                preferences.remove(intPreferencesKey(knownWordsKey))
                            }
                        }
                        true
                    }

                    else -> false
                }
            }
            popup.show()
        }


        return view
    }
}