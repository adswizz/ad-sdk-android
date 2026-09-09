package com.adswizz.podcastsample

import android.graphics.Color
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date

const val TIMESTAMP_FORMAT = "HH:mm:ss.SSS"

class LogAdapter : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {


    private val items = mutableListOf<String>()

    fun add(item: String) {

        val timeStamp: String = SimpleDateFormat(TIMESTAMP_FORMAT).format(Date())
        val entryLine = "$timeStamp $item"

        items.add(0, entryLine)
        notifyItemInserted(0)
    }

    fun clear() {
        val count = items.size
        items.clear()
        notifyItemRangeRemoved(0, count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        return LogViewHolder(
            LayoutInflater.from(parent.context).inflate(
                android.R.layout.simple_spinner_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {

        val ssBuilder = SpannableStringBuilder(items[position])
        val spanEnd = TIMESTAMP_FORMAT.length

        ssBuilder.setSpan(RelativeSizeSpan(0.7f), 0, spanEnd, 0)
        ssBuilder.setSpan(ForegroundColorSpan(Color.DKGRAY), 0, spanEnd, 0)
        ssBuilder.setSpan(TypefaceSpan("sans-serif-condensed"), 0, spanEnd, 0)

        holder.label.textSize = 12F
        holder.label.isElegantTextHeight = true
        holder.label.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        holder.label.isSingleLine = false
        holder.label.text = ssBuilder
    }

    override fun getItemCount() = items.size

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label: TextView = itemView.findViewById(android.R.id.text1)
    }
}