package com.example.japanvocalist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CategoryAdapter(context: Context, categories: MutableList<Category>) :
    ArrayAdapter<Category>(context, 0, categories) {

    private class ViewHolder(view: View) {
        val nameTextView: TextView = view.findViewById(R.id.categoryName)
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
        }

        return view
    }
}