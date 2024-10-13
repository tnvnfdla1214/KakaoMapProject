package com.example.kakaomapproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kakaomapproject.databinding.FragmentErrorBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ErrorBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentErrorBottomSheetBinding

    var onDismissCallback: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentErrorBottomSheetBinding.inflate(inflater, container, false)

        val errorCode = arguments?.getInt(ARG_ERROR_CODE) ?: UNKNOWN_ERROR_CODE
        val errorMessage = arguments?.getString(ARG_ERROR_MESSAGE) ?: UNKNOWN_ERROR
        val errorPath = arguments?.getString(ARG_ERROR_PATH) ?: NO_PATH_INFO

        binding.path.text = errorPath
        binding.code.text = errorCode.toString()
        binding.message.text = errorMessage

        binding.closeButton.setOnClickListener {
            onDismissCallback?.invoke()
            dismiss()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDismissCallback?.invoke()
    }


    companion object {
        private const val ARG_ERROR_CODE = "error_code"
        private const val ARG_ERROR_MESSAGE = "error_message"
        private const val ARG_ERROR_PATH = "error_path"
        const val TAG = "ErrorBottomSheet"
        const val UNKNOWN_ERROR = "Unknown Error"
        const val NO_PATH_INFO = "No path info"
        const val UNKNOWN_ERROR_CODE = -1

        fun newInstance(
            errorCode: Int,
            errorMessage: String,
            errorPath: String?,
            onDismissCallback: (() -> Unit)? = null
        ): ErrorBottomSheetFragment {
            val fragment = ErrorBottomSheetFragment()
            fragment.onDismissCallback = onDismissCallback
            val args = Bundle().apply {
                putInt(ARG_ERROR_CODE, errorCode)
                putString(ARG_ERROR_MESSAGE, errorMessage)
                putString(ARG_ERROR_PATH, errorPath)
            }
            fragment.arguments = args
            return fragment
        }
    }
}