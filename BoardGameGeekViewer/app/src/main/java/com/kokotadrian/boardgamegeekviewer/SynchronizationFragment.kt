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

    private val db get() = (activity as MainActivity).db

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val config = db.getConfig() ?: return

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

    private var synchronizedGames = false
    private var synchronizedExpansions = false
    private var collectionList = mutableListOf<CollectionItemInfo>()

    private fun synchronize(username: String) {
        val synchronizeButton = binding.synchronizeButton
        synchronizeButton.isEnabled = false
        val loadingProgressBar = binding.loading

        loadingProgressBar.visibility = View.VISIBLE


        val gamesDownloader = XmlApiDownloader(
            UserCollectionXmlParser(),
            ApiQueryBuilder().userGames(username)
        )

        val expansionsDownloader = XmlApiDownloader(
            UserCollectionXmlParser(),
            ApiQueryBuilder().userExpansions(username)
        )

        var showedActivity = false

        gamesDownloader.addOnFinishListener {
            if (it == null) {
                if (!showedActivity) {
                    (activity as MainActivity).showPleaseWaitToast()
                    showedActivity = true
                }

                loadingProgressBar.visibility = View.INVISIBLE
                synchronizeButton.isEnabled = true
            } else {
                this.synchronizedGames = true
                this.collectionList.addAll(0, it)

                if (this.synchronizedExpansions && this.synchronizedGames) {
                    db.sync(this.collectionList)
                    loadingProgressBar.visibility = View.INVISIBLE
                    synchronizeButton.isEnabled = true
                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                }
            }
        }

        expansionsDownloader.addOnFinishListener {
            if (it == null) {
                if (!showedActivity) {
                    (activity as MainActivity).showPleaseWaitToast()
                    showedActivity = true
                }

                loadingProgressBar.visibility = View.INVISIBLE
                synchronizeButton.isEnabled = true
            } else {
                this.synchronizedExpansions = true
                this.collectionList.addAll(0, it)

                if (this.synchronizedExpansions && this.synchronizedGames) {
                    db.sync(this.collectionList)
                    loadingProgressBar.visibility = View.INVISIBLE
                    synchronizeButton.isEnabled = true
                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                }
            }
        }

        if (!this.synchronizedGames) {
            gamesDownloader.execute()
        }
        if (!this.synchronizedExpansions) {
            expansionsDownloader.execute()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}