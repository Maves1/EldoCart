package com.childrenofcorn.eldorado

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class ListAdapter(private val dataSet: ArrayList<Product>) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageViewProductImage: ImageView = view.findViewById(R.id.product_image)
        val textViewProductName: TextView = view.findViewById(R.id.item_name)
        val textViewProductPrice: TextView = view.findViewById(R.id.item_price)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.list_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        Picasso.get().load(dataSet[position].imageLink).into(viewHolder.imageViewProductImage)
        viewHolder.textViewProductName.text = dataSet[position].name
        viewHolder.textViewProductPrice.text = dataSet[position].price.toString()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun deleteItemAtPosition(position: Int) {
        this.dataSet.removeAt(position)
        this.notifyDataSetChanged()
    }

    fun getItemAtPositition(position: Int) : Product {
        return this.dataSet[position]
    }

    fun switchEmptyViewHolder() {

    }
}