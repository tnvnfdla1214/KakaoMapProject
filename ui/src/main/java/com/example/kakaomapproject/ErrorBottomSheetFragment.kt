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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 테마 적용
        setStyle(
            STYLE_NORMAL,
            R.style.AppBottomSheetDialogBorder24WhiteTheme  // 둥근 모서리 테마 적용
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentErrorBottomSheetBinding.inflate(inflater, container, false)

        val errorCode = arguments?.getInt(ARG_ERROR_CODE) ?: 0
        val errorMessage = arguments?.getString(ARG_ERROR_MESSAGE) ?: "Unknown Error"
        val errorPath = arguments?.getString(ARG_ERROR_PATH) ?: "No path info available"

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