package android.example.com.chatapp.view.chat

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.*
import android.example.com.chatapp.model.*
import android.example.com.chatapp.util.*
import android.example.com.chatapp.view.adapters.SavedStateAdapter
import android.example.com.chatapp.view.adapters.chat.*
import android.example.com.chatapp.viewModel.ChatViewModel
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.core.models.enums.RenditionType
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.themes.GPHTheme
import com.giphy.sdk.ui.themes.GridType
import com.giphy.sdk.ui.views.GiphyDialogFragment
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList


class ChatFragment : Fragment(), MessageSent, GiphyDialogFragment.GifSelectionListener,
    Handler.Callback {

    /////BINDING
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    /////VIEWMODEL
    private lateinit var viewModel: ChatViewModel

    //////
    private lateinit var uidReceiver: String
    private var isTypeMessageChanged = false
    private var isLayoutAttachFileInvisible = true
    private lateinit var dialog: Dialog

    //////Firebase RecyclerView Adapter
    private lateinit var chatAdapter: FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>

    //////
    private var mediaPlayer: MediaPlayer? = null
    private var playingPosition = -1
    private lateinit var uiUpdateHandler: Handler
    private lateinit var playingViewHolder: AudioMessageViewHolder
    private val MSG_UPDATE_SEEK_BAR = 12345

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NavigationUI.setupWithNavController(binding.toolbar, findNavController())
        binding.toolbar.inflateMenu(R.menu.menu_chat)
        setMenuListeners()
        Giphy.configure(requireContext(), getString(R.string.giphy_api_key), true)
        getExtras()
        setListeners()
        getMessages(savedInstanceState)
        viewModel.checkChatAsSeen(uidReceiver)
        observeViewModel()
    }


    private fun getExtras() {
        arguments?.let {
            uidReceiver = requireArguments().getString(UID, null)
        }
        viewModel.getReceiverData(uidReceiver)
    }

    private fun setMenuListeners() {
        binding.toolbar.menu.getItem(2).subMenu.getItem(6).subMenu.clearHeader()
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menuSaveChat -> Toast.makeText(
                    requireContext(),
                    "Save chat",
                    Toast.LENGTH_LONG
                ).show()
            }
            return@setOnMenuItemClickListener false
        }
    }

    private fun setListeners() {
//////////////////HandleOnBackPressed
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (!isLayoutAttachFileInvisible) {
                toggleViewAttachFile()
            } else {
                Navigation.findNavController(binding.root).navigateUp()
            }
        }
//////////////////Message TextWatcher
        binding.txtMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val message = getMessage()
                if (message.isNotEmpty()) {
                    if (!isTypeMessageChanged) {
                        AnimUtils.changeIconButtonSendMessage(
                            R.drawable.ic_action_send, binding.imgBtnSendMessage
                        )
                        AnimUtils.animateIconsChat(
                            binding.btnAttachFile, binding.btnSendPhoto,
                            38, requireContext()
                        )
                        isTypeMessageChanged = true
                    }
                } else {
                    AnimUtils.changeIconButtonSendMessage(
                        R.drawable.ic_mic,
                        binding.imgBtnSendMessage
                    )
                    AnimUtils.animateIconsChat(
                        binding.btnAttachFile,
                        binding.btnSendPhoto,
                        0,
                        requireContext()
                    )
                    isTypeMessageChanged
                    isTypeMessageChanged = false
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
//////////////   AttachFileLayoutListeners
        binding.btnAttachFile.setOnClickListener {
            toggleViewAttachFile()
        }
        binding.layoutAttachFiles.fabAttachFile.setOnClickListener {
            toggleViewAttachFile()
            val intent = Intent().setAction(Intent.ACTION_GET_CONTENT).setType("application/pdf")
            startActivityForResult(intent, CODE_DOC_PDF)
        }
        binding.layoutAttachFiles.fabAttachVideo.setOnClickListener {
            toggleViewAttachFile()
            val intent = Intent().setAction(Intent.ACTION_GET_CONTENT).setType("video/mp4")
            startActivityForResult(intent, CODE_VIDEO)
        }
        binding.layoutAttachFiles.fabAttachGallery.setOnClickListener {
            toggleViewAttachFile()
            val intent = Intent().setAction(Intent.ACTION_GET_CONTENT).setType("image/*")
            startActivityForResult(intent, CODE_IMAGE)
        }
        binding.layoutAttachFiles.fabAttachAudio.setOnClickListener {
            toggleViewAttachFile()
            val intent = Intent().setAction(Intent.ACTION_GET_CONTENT).setType("audio/*")
            startActivityForResult(intent, CODE_AUDIO)
        }
//////////////
        binding.btnSendPhoto.setOnClickListener {
            Navigation.findNavController(it).navigate(
                ChatFragmentDirections.actionChatToCamera(
                    uidReceiver,
                    CAMERA_ACTION_MESSAGE
                )
            )
        }
        binding.btnChatSendMessage.setOnClickListener {
            val messageText = getMessage()
            if (messageText.isNotEmpty()) {
                val message = Message(
                    FbUser.getUserId(), uidReceiver,
                    messageText, null, null, null,
                    MessageType.TYPE_TEXT
                )
                viewModel.addUserAndSendMessage(uidReceiver, message)
            } else {
                startActivityVoiceToText()
            }
        }
        binding.btnChatSendGif.setOnClickListener {
            val settings = GPHSettings().apply {
                gridType = GridType.waterfall
                theme = GPHTheme.Dark
                stickerColumnCount = 3
                mediaTypeConfig = arrayOf(
                    GPHContentType.gif, GPHContentType.sticker,
                    GPHContentType.emoji, GPHContentType.text
                )

            }

            val gdl = GiphyDialogFragment.Companion.newInstance(settings)
            gdl.gifSelectionListener = this
            gdl.show(parentFragmentManager, ChatFragment::class.java.name)


        }
        binding.txtUsernameChat.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(ChatFragmentDirections.actionChatToUserProfile(uidReceiver))
        }
    }

    private fun getMessages(savedInstanceState: Bundle?) {
        val options: FirestoreRecyclerOptions<Message> = FirestoreRecyclerOptions.Builder<Message>()
            .setSnapshotArray(viewModel.array)
            .setLifecycleOwner(this)
            .build()


        chatAdapter = object : SavedStateAdapter<Message, RecyclerView.ViewHolder>(
            options, savedInstanceState, binding.recyclerViewMessages
        ) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val inflater = LayoutInflater.from(requireContext())
                return when (viewType) {
                    MessageType.TYPE_TEXT.type -> {
                        val view = CardMessageTypeTextBinding.inflate(inflater)
                        TextMessageViewHolder(view)
                    }
                    MessageType.TYPE_GIF.type -> {
                        val view = CardMessageTypeGifBinding.inflate(inflater)
                        GifMessageViewHolder(view)
                    }
                    MessageType.TYPE_DOC_PDF.type -> {
                        val view = CardMessageTypePdfBinding.inflate(inflater)
                        DocPDFMessageViewHolder(view)
                    }
                    MessageType.TYPE_VIDEO.type -> {
                        val view = CardMessageTypeVideoBinding.inflate(inflater)
                        VideoMessageViewHolder(view)
                    }
                    MessageType.TYPE_PHOTO.type -> {
                        val view = CardMessageTypeImageBinding.inflate(inflater)
                        ImageMessageViewHolder(view)
                    }
                    MessageType.TYPE_AUDIO.type -> {
                        val view = CardMessageTypeAudioBinding.inflate(inflater)
                        AudioMessageViewHolder(view)
                    }
                    else -> throw IllegalArgumentException("Invalid View Type....")
                }
            }

            override fun getItemViewType(position: Int): Int {
                return getItem(position).messageType!!.type
            }

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                model: Message
            ) {
                when (holder.itemViewType) {
                    MessageType.TYPE_TEXT.type -> {
                        (holder as TextMessageViewHolder).view.textMessageTimestamp.text =
                            getItem(position).timestamp?.let {
                                showMessageTimestamp(
                                    getItem(position).message!!,
                                    it.time
                                )
                            }

                        changeMessageDesign(
                            getItem(position).uidAuthor!!, holder.view.cardViewMessage,
                            holder.view.textMessageTimestamp, requireContext()
                        )
                    }
                    MessageType.TYPE_GIF.type -> {
                        (holder as GifMessageViewHolder).view.textMessageTimestamp.text =
                            getItem(position).timestamp?.let {
                                showMessageTimestamp(
                                    getItem(position).message!!,
                                    it.time
                                )
                            }
                        changeMessageDesign(
                            getItem(position).uidAuthor!!, holder.view.cardViewMessage,
                            holder.view.textMessageTimestamp, requireContext()
                        )
                        holder.view.gphMediaView.setMediaWithId(
                            getItem(position).giphyMediaId!!, RenditionType.original
                        )
                        holder.view.gphMediaView.setOnClickListener {
                            Navigation.findNavController(it).navigate(
                                ChatFragmentDirections
                                    .actionChatToWatchGif(getItem(position).giphyMediaId!!)
                            )
                        }
                    }
                    MessageType.TYPE_DOC_PDF.type -> {
                        (holder as DocPDFMessageViewHolder).view.textMessageTimestamp.text =
                            getItem(position).timestamp?.let {
                                showMessageTimestamp(
                                    getItem(position).message!!,
                                    it.time
                                )
                            }
                        changeMessageDesign(
                            getItem(position).uidAuthor!!, holder.view.cardViewMessage,
                            holder.view.textMessageTimestamp, requireContext()
                        )
                        holder.itemView.setOnClickListener {
                            Navigation.findNavController(it)
                                .navigate(
                                    ChatFragmentDirections.actionChatToWatchDocPdf(
                                        getItem(
                                            position
                                        ).dataUrl!!
                                    )
                                )
                        }
                    }
                    MessageType.TYPE_VIDEO.type -> {
                        (holder as VideoMessageViewHolder).view.textMessageTimestamp.text =
                            getItem(position).timestamp?.let {
                                showMessageTimestamp(
                                    getItem(position).message!!,
                                    it.time
                                )
                            }
                        changeMessageDesign(
                            getItem(position).uidAuthor!!, holder.view.cardViewMessage,
                            holder.view.textMessageTimestamp, requireContext()
                        )
                        holder.view.thumbnailVideo.loadImage(
                            getItem(position).videoThumbnailUrl,
                            getProgressDrawable(requireContext())
                        )

                        holder.itemView.setOnClickListener {
                            val intent =
                                Intent(requireContext(), WatchVideoPIPModeActivity::class.java)
                            intent.putExtra("keyVideoUrl", getItem(position).dataUrl)
                            startActivity(intent)
                        }

                    }
                    MessageType.TYPE_PHOTO.type -> {
                        (holder as ImageMessageViewHolder).view.textMessageTimestamp.text =
                            getItem(position).timestamp?.let {
                                showMessageTimestamp(
                                    getItem(position).message!!,
                                    it.time
                                )
                            }
                        changeMessageDesign(
                            getItem(position).uidAuthor!!, holder.view.cardViewMessage,
                            holder.view.textMessageTimestamp, requireContext()
                        )
                        holder.view.imgChat.loadImage(
                            getItem(position).dataUrl,
                            getProgressDrawable(requireContext())
                        )
                        holder.view.imgChat.transitionName = getItem(position).dataUrl!!
                        holder.itemView.setOnClickListener {
                            val pairImage = FragmentNavigatorExtras(
                                Pair(holder.view.imgChat, getItem(position).dataUrl!!)
                            )
                            val bundle = Bundle()
                            bundle.putString("ImageUrl", getItem(position).dataUrl)
                            Navigation.findNavController(it)
                                .navigate(
                                    R.id.actionChatToWatchImage, bundle, null,
                                    pairImage
                                )
                        }
                    }
                    MessageType.TYPE_AUDIO.type -> {
                        (holder as AudioMessageViewHolder).view.audioMessageTimestamp.text =
                            getItem(position).timestamp?.let {
                                showMessageTimestamp(
                                    getItem(position).message!!,
                                    it.time
                                )
                            }
                        changeMessageDesign(
                            getItem(position).uidAuthor!!, holder.view.cardViewMessage,
                            holder.view.audioMessageTimestamp, requireContext()
                        )
                        if (position == playingPosition) {
                            playingViewHolder = holder
                            updatePlayingView()
                        } else {
                            updateNonPlayingView(holder)
                        }
                        holder.view.seekBarAudio.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                seekBar: SeekBar?,
                                progress: Int,
                                fromUser: Boolean
                            ) {
                                if (fromUser) {
                                    mediaPlayer?.seekTo(progress)
                                }
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {

                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar?) {

                            }

                        })
                        holder.view.btnPlayPauseAudio.setOnClickListener {
                            if (!this@ChatFragment::uiUpdateHandler.isInitialized) {
                                uiUpdateHandler = Handler(Looper.getMainLooper(), this@ChatFragment)

                            }
                            if (holder.adapterPosition == playingPosition) {
                                if (mediaPlayer!!.isPlaying) {
                                    mediaPlayer!!.pause()
                                } else {
                                    mediaPlayer!!.start()
                                }
                            } else {
                                playingPosition = holder.adapterPosition
                                if (mediaPlayer != null) {
                                    if (this@ChatFragment::playingViewHolder.isInitialized) {
                                        updateNonPlayingView(holder)
                                    }
                                    mediaPlayer!!.release()
                                }
                                playingViewHolder = holder
                                startMediaPlayer(getItem(position).dataUrl!!)
                            }
                            updatePlayingView()
                        }

                    }
                }
            }

            override fun onDataChanged() {
                super.onDataChanged()
                val messagesSize = itemCount
                if (messagesSize == 0) {
                    binding.txtEmptychat.visibility = View.VISIBLE
                } else {
                    binding.txtEmptychat.visibility = View.GONE
                    binding.recyclerViewMessages.smoothScrollToPosition(chatAdapter.itemCount)
                }
            }
        }
        binding.recyclerViewMessages.apply {
            adapter = chatAdapter
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
    }


    private fun getMessage(): String {
        return binding.txtMessage.text.toString()
    }

    private fun observeViewModel() {
        viewModel.receiverLiveData.observe(viewLifecycleOwner, { user ->
            user?.let {
                binding.imgUserChat.loadImageRounded(
                    it.imageUrl,
                    getProgressDrawable(requireContext())
                )
                binding.txtUsernameChat.text = it.userName
            }
        })
        viewModel.isMessageSent.observe(viewLifecycleOwner, {
            it?.let {
                onMessageSent(it)

            }
        })
        viewModel.fileUploadProgress.observe(viewLifecycleOwner, {
            val messageDialogTextView = dialog.findViewById<TextView>(R.id.messageDialog)
            messageDialogTextView.text = "Upload progress: " + it.toInt() + "%"

        })
        viewModel.fileUploadSuccessful.observe(viewLifecycleOwner, {
            if (!it) {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
            }
        })
        viewModel.videoUploadProgress.observe(viewLifecycleOwner, {
            it?.let {
                val messageDialogTextView = dialog.findViewById<TextView>(R.id.messageDialog)
                messageDialogTextView.text = "Upload progress: " + it.toInt() + "%"
            }
        })
        viewModel.isVideoSent.observe(viewLifecycleOwner, {
            dialog.dismiss()
            if (!it) {
                Toast.makeText(
                    requireContext(), "There was an error trying to upload the video",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun toggleViewAttachFile() {
        AnimUtils.showViewAttachFiles(
            binding.layoutAttachFiles.root,
            isLayoutAttachFileInvisible,
            requireContext()
        )
        isLayoutAttachFileInvisible = !isLayoutAttachFileInvisible
    }

    private fun startActivityVoiceToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...")
        try {
            startActivityForResult(intent, CODE_RECOGNIZE_SPEECH)
        } catch (e: ActivityNotFoundException) {
            e.cause
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CODE_RECOGNIZE_SPEECH -> {
                if (resultCode == RESULT_OK && data != null) {
                    val result =
                        ArrayList<String>(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS))
                    binding.txtMessage.setText(result[0])
                }
            }
            CODE_DOC_PDF -> {
                if (resultCode == RESULT_OK) {
                    try {
                        val uri = data?.data
                        uri?.let {
                            dialog = createCustomDialog(
                                requireContext(),
                                "Sending file", null
                            )
                            dialog.show()
                            viewModel.sendFileMessage(
                                uri, uidReceiver, "PDF Document",
                                "docPDF", ".pdf", MessageType.TYPE_DOC_PDF
                            )
                        }
                    } catch (e: NullPointerException) {
                        e.cause
                    }
                }
            }
            CODE_VIDEO -> {
                if (resultCode == RESULT_OK) {
                    try {
                        data?.let {
                            val uri = data.data
                            dialog = createCustomDialog(
                                requireContext(), null,
                                "Uploading video"
                            )
                            dialog.show()
                            getThumbnailFromVideo(uidReceiver, uri!!)
                        }
                    } catch (e: NullPointerException) {
                        e.cause
                    }
                }
            }
            CODE_IMAGE -> {
                if (resultCode == RESULT_OK) {
                    try {
                        data?.let {
                            val uri = data.data
                            dialog = createCustomDialog(
                                requireContext(),
                                "Sending image...", null
                            )
                            dialog.show()
                            viewModel.sendFileMessage(
                                uri!!, uidReceiver, "Image",
                                "photos", ".jpeg", MessageType.TYPE_PHOTO
                            )
                        }
                    } catch (e: NullPointerException) {
                        e.cause
                    }
                }
            }
            CODE_AUDIO -> {
                if (resultCode == RESULT_OK) {
                    try {
                        data?.let {
                            val uri = data.data
                            dialog = createCustomDialog(
                                requireContext(),
                                "Sending audio file...", null
                            )
                            dialog.show()
                            viewModel.sendFileMessage(
                                uri!!, uidReceiver, "AUDIO",
                                "audio", ".mp3", MessageType.TYPE_AUDIO
                            )
                        }
                    } catch (e: NullPointerException) {
                        e.cause
                    }
                }
            }
        }
    }

    private fun getThumbnailFromVideo(uirReceiver: String, videoUri: Uri) {
        Glide.with(requireContext())
            .asBitmap()
            .load(videoUri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val thumbnailUri = BitmapUtils.getUriforBitmap(resource, requireContext())
                    viewModel.sendThumbnailAndVideoToMedia(uirReceiver, videoUri, thumbnailUri)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    Toast.makeText(
                        requireContext(), "Error getting thumbnail from video",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    ////Message Interface
    override fun onMessageSent(isMessageSent: Boolean) {
        super.onMessageSent(isMessageSent)
        if (isMessageSent) {
            binding.txtMessage.text?.clear()
        } else {
            Toast.makeText(requireContext(), "Error trying to send message", Toast.LENGTH_SHORT)
                .show()
        }
    }


    /////Gif Interface
    override fun didSearchTerm(term: String) {

    }

    override fun onDismissed() {

    }

    override fun onGifSelected(media: Media, searchTerm: String?) {
        val title = media.title ?: "GIF"
        val message = Message(
            FbUser.getUserId(), uidReceiver, title, null,
            media.id, null, MessageType.TYPE_GIF
        )
        viewModel.addUserAndSendMessage(uidReceiver, message)
    }

    /// AUDIO MESSAGES
    override fun handleMessage(msg: android.os.Message): Boolean {

        when (msg.what) {
            MSG_UPDATE_SEEK_BAR -> {
                playingViewHolder.view.seekBarAudio.progress = mediaPlayer!!.currentPosition
                uiUpdateHandler.sendEmptyMessageDelayed(this.MSG_UPDATE_SEEK_BAR, 100)
                return true
            }
        }
        return false
    }

    private fun updatePlayingView() {
        playingViewHolder.view.seekBarAudio.apply {
            max = mediaPlayer!!.duration
            progress = mediaPlayer!!.currentPosition
            isEnabled = true
        }
        if (mediaPlayer!!.isPlaying) {
            Log.e("ADD", "Playing")
            uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100)
            playingViewHolder.view.btnPlayPauseAudio.setImageResource(R.drawable.ic_pause)
        } else {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR)
            playingViewHolder.view.btnPlayPauseAudio.setImageResource(R.drawable.ic_play)
        }

    }

    private fun updateNonPlayingView(holder: AudioMessageViewHolder) {
        if (this::playingViewHolder.isInitialized) {
            if (holder == playingViewHolder) {
                if (this::uiUpdateHandler.isInitialized) {
                    uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR)
                }
            }
        }
        holder.view.seekBarAudio.apply {
            isEnabled = false
            progress = 0
        }
        holder.view.btnPlayPauseAudio.setImageResource(R.drawable.ic_play)
    }

    private fun startMediaPlayer(dataUrl: String) {
        mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(dataUrl))
        mediaPlayer!!.setOnCompletionListener {
            releaseMediaPlayer()
        }
        mediaPlayer?.start()
    }

    private fun stopMediaPlayer() {
        if (mediaPlayer != null) {
            releaseMediaPlayer()
        }
    }

    private fun releaseMediaPlayer() {
        if (this::playingViewHolder.isInitialized) {
            updateNonPlayingView(playingViewHolder)
            mediaPlayer?.release()
            mediaPlayer = null
            playingPosition = -1

        }
    }

    override fun onPause() {
        super.onPause()
        stopMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}