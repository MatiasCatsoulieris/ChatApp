package android.example.com.chatapp.view.chat


import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.example.com.chatapp.databinding.ActivityWatchVideoPipmodeBinding
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.MediaController
import java.lang.NullPointerException


class WatchVideoPIPModeActivity : AppCompatActivity() {

    private var _binding: ActivityWatchVideoPipmodeBinding? = null
    private val binding get() = _binding!!

    val VIDEO_URL = "keyVideoUrl"
    private lateinit var videoUrl: String
    private lateinit var mediaController: MediaController
    private lateinit var pipBuilder: PictureInPictureParams.Builder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWatchVideoPipmodeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        getExtras()
        init()
        setMController()
        setListeners()
        playVideo(videoUrl)
    }


    private fun setListeners() {
        binding.btnEnterInPIPMode.setOnClickListener {
            startPIPMode()
        }
    }

    private fun init() {
        mediaController = MediaController(binding.root.context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pipBuilder = PictureInPictureParams.Builder()
        }
    }

    private fun getExtras() {
        try {
            intent.extras?.let {
                videoUrl = intent.extras!!.getString("keyVideoUrl", null)
                Log.e("ASA", "$videoUrl")
            }
        } catch (e: NullPointerException) {
            e.cause
        }
    }

    private fun setMController() {
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)
    }

    private fun startPIPMode() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode(pipBuilder.build())

        }
    }

    private fun playVideo(videoUrl: String) {
        binding.videoView.setVideoPath(videoUrl)
        binding.videoView.requestFocus()
        binding.videoView.start()
    }

    override fun onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!isInPictureInPictureMode) {
                startPIPMode()
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        if(isInPictureInPictureMode) {
            binding.btnEnterInPIPMode.visibility = View.GONE
        } else {
            binding.btnEnterInPIPMode.visibility = View.VISIBLE
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        playNextVideo(intent)
    }

    private fun playNextVideo(intent: Intent?) {
        intent?.let {
            val nextVideoUrl = intent.extras?.getString(VIDEO_URL, null)
            if(nextVideoUrl == null) {
                finish()
            } else {
                playVideo(nextVideoUrl)
            }
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}