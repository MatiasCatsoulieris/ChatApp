package android.example.com.chatapp.view.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.FragmentWatchGifBinding
import com.giphy.sdk.core.models.enums.RenditionType


class WatchGifFragment : Fragment() {

    //VIEW BINDING
    private var _binding : FragmentWatchGifBinding? = null
    private val binding get() = _binding!!

    val GIF_ID = "KeyGIFId"
    private lateinit var mediaId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWatchGifBinding.inflate(inflater, container, false)
        getExtras()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ghpMedia.setMediaWithId(mediaId, RenditionType.original, null)
    }

    private fun getExtras() {
        arguments?.let{
            mediaId = requireArguments().getString(this.GIF_ID, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}