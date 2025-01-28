package com.example.japanvocalist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView

class CategoryAdapter(
    context: Context,
    categories: MutableList<Category>,
    private val onCategoryDelete: (Category) -> Unit
) :
    ArrayAdapter<Category>(context, 0, categories) {

    private class ViewHolder(view: View) {
        val nameTextView: TextView = view.findViewById(R.id.categoryName)
        val moreOptions: ImageView = view.findViewById(R.id.moreOptions)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_category, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = convertView.tag as ViewHolder
        }

        val category = getItem(position)
        category?.let {
            viewHolder.nameTextView.text = it.name

            viewHolder.moreOptions.setOnClickListener { v ->
                showPopupMenu(v, it)
            }
        }
        return view
    }


    private fun showPopupMenu(view: View, category: Category) {
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.category_menu_options, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    onCategoryDelete(category)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }
}