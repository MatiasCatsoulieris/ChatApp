package android.example.com.chatapp.view.adapters

import android.example.com.chatapp.databinding.CardFilterBinding
import android.example.com.chatapp.model.Filter
import android.example.com.chatapp.util.AnimUtils
import android.example.com.chatapp.util.OnFilterListener
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class FilterAdapter(
    val filterList: ArrayList<Filter>,
    val imgUri: String, val onFilterListener: OnFilterListener
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private var lastHolderClicked: FilterViewHolder? = null
    private var positionFilter = 0

    class FilterViewHolder(val view: CardFilterBinding) : RecyclerView.ViewHolder(view.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = CardFilterBinding.inflate(inflater, parent, false)
        return FilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.view.txtFilterName.text = filterList[position].name
        if (filterList[position].transformation == null) {
            Glide.with(holder.view.root.context).load(imgUri).override(100, 150)
                .into(holder.view.imgFilter)
        } else {
            Glide.with(holder.view.root.context).load(imgUri)
                .apply(RequestOptions.bitmapTransform(filterList[position].transformation!!))
                .override(100, 150)
                .into(holder.view.imgFilter)
        }
        if (positionFilter == holder.adapterPosition) {
            AnimUtils.scaleView(holder.view.imgFilterUsed, 150, 1f)
            lastHolderClicked = holder
        } else {
            AnimUtils.scaleView(holder.view.imgFilterUsed, 150, 0f)
        }

        holder.itemView.setOnClickListener {
            if(lastHolderClicked != null) {
                AnimUtils.scaleView(lastHolderClicked!!.view.imgFilterUsed, 150, 0f)
            }
            AnimUtils.scaleView(holder.view.imgFilterUsed, 150, 1f)
            positionFilter = holder.adapterPosition
            lastHolderClicked = holder
            onFilterListener.onFilterClicked(filterList[position].transformation)
        }
    }

    override fun getItemCount(): Int = filterList.size


}