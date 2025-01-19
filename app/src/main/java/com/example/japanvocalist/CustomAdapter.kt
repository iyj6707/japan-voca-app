package com.example.japanvocalist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView

class CustomAdapter(
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
            popup.inflate(R.menu.menu_item_options)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_reset -> {
                        val sharedPref =
                            context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            remove("your_preference_key")
                            apply()
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