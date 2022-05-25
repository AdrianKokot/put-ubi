package com.kokotadrian.boardgamegeekviewer

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.kokotadrian.boardgamegeekviewer.databinding.FragmentSynchronizationBinding
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SynchronizationFragment : Fragment() {

    private var _binding: FragmentSynchronizationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setBarTitle("Synchronization")

        _binding = FragmentSynchronizationBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val config = (activity as MainActivity).db.getConfig() ?: return

        binding.synchronizeButton.setOnClickListener {


            if (config.lastSync?.isAfter(
                    Instant.now().minus(
                        24,
                        ChronoUnit.HOURS
                    )
                ) == true
            ) {

                activity?.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setTitle("Are you sure?")
                        setMessage("Last synchronization was less than 24h ago. Are you sure you want to proceed?")

                        setNegativeButton(
                            R.string.cancel,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                            })
                        setPositiveButton(
                            R.string.ok,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                synchronize(config.username)
                            })
                    }
                    builder.create().show()
                }
            } else {
                synchronize(config.username)
            }


        }
    }

    private fun synchronize(username: String) {

        binding.synchronizeButton.isEnabled = false
        val loadingProgressBar = binding.loading

        loadingProgressBar.visibility = View.VISIBLE


        val downloader = XmlApiDownloader(
            GamesXmlApiParser(),
            ApiQueryBuilder().userGames(username)
        )

        downloader.addOnFinishListener {
            if (it == null) {
                (activity as MainActivity).showPleaseWaitToast()
            } else {
                (activity as MainActivity).db.syncGameCollection(it)
                (activity as MainActivity).db.setLastSyncToNow()
                findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            }

            loadingProgressBar.visibility = View.INVISIBLE
            binding.synchronizeButton.isEnabled = true
        }

        downloader.execute()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}