package android.example.com.chatapp.view.adapters.chat

import android.example.com.chatapp.databinding.*
import androidx.recyclerview.widget.RecyclerView

class TextMessageViewHolder(val view: CardMessageTypeTextBinding): RecyclerView.ViewHolder(view.root) {}

class GifMessageViewHolder(val view: CardMessageTypeGifBinding): RecyclerView.ViewHolder(view.root) {}

class DocPDFMessageViewHolder(val view: CardMessageTypePdfBinding): RecyclerView.ViewHolder(view.root) {}

class VideoMessageViewHolder(val view: CardMessageTypeVideoBinding): RecyclerView.ViewHolder(view.root) {}

class ImageMessageViewHolder(val view: CardMessageTypeImageBinding): RecyclerView.ViewHolder(view.root) {}

class AudioMessageViewHolder(val view: CardMessageTypeAudioBinding): RecyclerView.ViewHolder(view.root) {}

