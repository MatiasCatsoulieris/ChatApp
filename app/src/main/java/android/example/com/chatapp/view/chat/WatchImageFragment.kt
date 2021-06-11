package android.example.com.chatapp.view.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.FragmentWatchImageBinding
import android.example.com.chatapp.util.getProgressDrawable
import android.example.com.chatapp.util.loadImage
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet

class WatchImageFragment : Fragment() {

    private var _binding: FragmentWatchImageBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageUrl: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWatchImageBinding.inflate(inflater, container, false)
        getExtras()
        sharedElementEnterTransition = getEnterTransitionWatchImage()
        return binding.root
    }

    private fun getEnterTransitionWatchImage(): TransitionSet {
        val transitionSet = TransitionSet()
        transitionSet.addTransition(
            TransitionInflater.from(requireContext())
                .inflateTransition(android.R.transition.move)
        )

        return transitionSet
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadImage()
    }

    private fun getExtras() {
        arguments?.let {
            imageUrl = requireArguments().getString("ImageUrl", null)
            binding.imageChat.transitionName = imageUrl
        }

    }

    private fun loadImage() {
        binding.imageChat.loadImage(imageUrl, getProgressDrawable(requireContext()))
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}