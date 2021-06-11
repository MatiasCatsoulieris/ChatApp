package android.example.com.chatapp.viewModel

import android.example.com.chatapp.model.FirebaseStorage
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class WatchDocPDFViewModel : ViewModel() {

    val pdf = Transformations.map(FirebaseStorage.pdfFile) { it }
    val downloadFailed = Transformations.map(FirebaseStorage.downloadFailed) { it }

    fun downloadPdf(url: String) {
        FirebaseStorage.downloadPdf(url)

    }
}