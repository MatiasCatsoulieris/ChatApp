package android.example.com.chatapp.view.adapters

import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.CardGalleryBinding
import android.example.com.chatapp.databinding.CardGallerySmallBinding
import android.example.com.chatapp.util.*
import android.example.com.chatapp.view.HomeFragmentDirections
import android.example.com.chatapp.view.pagerFragments.CameraFragment
import android.example.com.chatapp.view.pagerFragments.CameraFragmentDirections
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class GalleryAdapter(val imgList: List<String>, val layoutType: Int, val typeAction: String?,
                     var isClickable: Boolean, val uidReceiver: String?, val cameraFragment: CameraFragment?,
                     val adapterType: String)
    : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {


    class GalleryViewHolder(view: CardGalleryBinding?, view2: CardGallerySmallBinding?)
        : RecyclerView.ViewHolder(view?.root ?: view2!!.root) {
        var imgView = view?.imgGallery ?: view2!!.imgGallery

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if(adapterType == "Grid") {
            val view = CardGalleryBinding.inflate(inflater, parent, false)
            GalleryViewHolder(view, null)
        } else {
            val view = CardGallerySmallBinding.inflate(inflater, parent, false)
            GalleryViewHolder(null, view)
        }

    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val imgPath = imgList[position]
        holder.imgView.loadImage(imgPath, getProgressDrawable(holder.imgView.context))
        holder.imgView.setOnClickListener {
            if(isClickable) {
                when(typeAction) {
                    CAMERA_ACTION_MESSAGE -> {
                        Navigation.findNavController(holder.imgView)
                            .navigate(HomeFragmentDirections.actionHomeToPhotoMessage(uidReceiver, imgPath))
                    }
                    CAMERA_ACTION_STATE -> {
                        Navigation.findNavController(holder.imgView)
                            .navigate(CameraFragmentDirections.actionCameraToImageState(imgPath))
                    }
                    CAMERA_ACTION_UPDATE_PICTURE -> {
                        if(cameraFragment != null) {
                            val openFile = Uri.parse(imgPath)
                            CropImage.imageCropFromFragment(
                                openFile,
                                holder.imgView.context, cameraFragment)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = imgList.size


    fun isItemClickable ( isItemClickable: Boolean) {
        isClickable = isItemClickable
    }

}

