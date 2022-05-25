package com.kokotadrian.boardgamegeekviewer.ui.login

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.kokotadrian.boardgamegeekviewer.*
import com.kokotadrian.boardgamegeekviewer.databinding.FragmentSetupAppBinding
import kotlinx.coroutines.withTimeout
import java.time.Duration
import java.util.*
import kotlin.concurrent.schedule

class SetupAppFragment : Fragment() {


    private var _binding: FragmentSetupAppBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSetupAppBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide();
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.show();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameEditText: EditText = binding.username
        val loginButton = binding.login
        val loadingProgressBar = binding.loading
        val validationMessageText = binding.errorMessage

        usernameEditText.doAfterTextChanged {
            loginButton.isEnabled = !it.isNullOrEmpty()
            validationMessageText.text = ""
        }

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE

            val downloader = XmlApiDownloader(
                UserXmlApiParser(),
                ApiQueryBuilder().userProfile(usernameEditText.text.toString())
            )

            downloader.addOnFinishListener {
                if (it == null) {
                    (activity as MainActivity).showPleaseWaitToast()

                } else {
                    if (it.id == -1L) {
                        validationMessageText.text = "User with given name doesn't exist"
                    } else {

                        (activity as MainActivity).db.assignUsername(it.name)
                        findNavController().navigate(R.id.action_setupAppFragment_to_FirstFragment)
                    }
                }

                loadingProgressBar.visibility = View.INVISIBLE
            }

            downloader.execute()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}