package com.adswizz.testSDKRequest

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adswizz.testSDKRequest.databinding.ItemListBinding


class ItemsAdapter(
    private val data: MutableList<Item> = mutableListOf(),
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolder(val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            ItemListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemData = data[position]
        val itemHolder = holder as ViewHolder

        itemHolder.binding.title.text = itemData.title
        if ( itemData.isHeading ){
            itemHolder.binding.title.typeface = Typeface.DEFAULT_BOLD
        }
        else{
            itemHolder.binding.title.typeface = Typeface.DEFAULT
            itemHolder.binding.root.setOnClickListener {
                itemData.onClick?.invoke(itemData.title)
            }
        }
    }

    override fun getItemCount() = data.size
}

data class Item(
    val title: String,
    val isHeading: Boolean,
    var onClick: ((title: String) -> Unit)? = null
)

