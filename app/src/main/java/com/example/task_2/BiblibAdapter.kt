package com.example.task_2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import name.ank.lab4.BibDatabase
import name.ank.lab4.BibEntry
import name.ank.lab4.Keys
import java.io.InputStream
import java.io.InputStreamReader

class BiblibAdapter(inputStream: InputStream): RecyclerView.Adapter<BiblibAdapter.ViewHolder>() {
    private lateinit var database: BibDatabase

    init {
        InputStreamReader(inputStream).use { reader ->
            database = BibDatabase(reader)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
        val author: TextView

        init {
            // Define click listener for the ViewHolder's View.
            title = view.findViewById(R.id.title)
            author = view.findViewById(R.id.author)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry: BibEntry = database.getEntry(position % database.count())
        holder.title.text = entry.getField(Keys.TITLE)
        holder.author.text = entry.getField(Keys.AUTHOR)
    }


}