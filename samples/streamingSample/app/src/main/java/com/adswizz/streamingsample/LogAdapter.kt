package com.adswizz.streamingsample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LogAdapter : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    private val items = mutableListOf<String>()

    fun add(item: String) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun clearAll() {
        val previousSize = items.size
        items.clear()
        notifyItemRangeRemoved(0, previousSize)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        return LogViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.log_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.label.text = items[position]
    }

    override fun getItemCount() = items.size

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label: TextView = itemView.findViewById(R.id.text)
    }
}