package android.example.com.chatapp.view.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.example.com.chatapp.R
import android.example.com.chatapp.databinding.FragmentWatchDocPdfBinding
import android.example.com.chatapp.viewModel.WatchDocPDFViewModel
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider


class WatchDocPDFFragment : Fragment() {

    private var _binding : FragmentWatchDocPdfBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WatchDocPDFViewModel

    private lateinit var pdfUrl : String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWatchDocPdfBinding.inflate(inflater, container, false)
        getExtras()
        viewModel = ViewModelProvider(this).get(WatchDocPDFViewModel::class.java)
        downloadPDF()
        return binding.root
    }

    private fun downloadPDF() {
        viewModel.downloadPdf(pdfUrl)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun getExtras() {
        arguments?.let {
            pdfUrl = it.getString("PdfUrl")!!

        }
    }

    private fun observeViewModel() {
        viewModel.pdf.observe(viewLifecycleOwner, {
            binding.pdfView.fromBytes(it).load()
        })
        viewModel.downloadFailed.observe(viewLifecycleOwner, {
            if(it){
                Toast.makeText(requireContext(), "Failed to download file.", Toast.LENGTH_SHORT).show()
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}