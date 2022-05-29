package com.kokotadrian.boardgamegeekviewer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kokotadrian.boardgamegeekviewer.databinding.FragmentFirstBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.system.exitProcess


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setBarTitle("Board Game Geek Viewer")

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val db get() = (activity as MainActivity).db

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val config = db.getConfig() ?: return

        val games = db.getGameList()
        val expansions = db.getExpansionList()

        val formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.GERMAN)
            .withZone(ZoneId.systemDefault())

        val helloTextView: TextView = binding.helloTextView
        val statisticTextView: TextView = binding.statisticTextView
        val lastSyncTextView: TextView = binding.lastSyncTextView

        helloTextView.text =
            Html.fromHtml("Hello <b>" + config.username + "</b>!", Html.FROM_HTML_MODE_COMPACT)


        if (config.lastSync != null) {
            statisticTextView.text = Html.fromHtml(
                "You have <b>" + games.size.toString() + "</b> games and <b>" + expansions.size.toString() + "</b> expansions.",
                Html.FROM_HTML_MODE_COMPACT
            )
            lastSyncTextView.text = Html.fromHtml(
                "Last sync was <b>" + formatter.format(config.lastSync) + "</b>",
                Html.FROM_HTML_MODE_COMPACT
            )
        }

        if (config.lastSync == null || games.size == 0) {
            binding.gameListButton.visibility = View.GONE
        }
        if (config.lastSync == null || expansions.size == 0) {
            binding.expansionListButton.visibility = View.GONE
        }

        binding.synchronizationButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.gameListButton.setOnClickListener {
            ListType.setToGameList()
            findNavController().navigate(R.id.action_FirstFragment_to_gameListFragment)
        }

        binding.expansionListButton.setOnClickListener {
            ListType.setToExpansionList()
            findNavController().navigate(R.id.action_FirstFragment_to_gameListFragment)
        }

        binding.eraseDataButton.setOnClickListener {
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setTitle("Are you sure?")
                    setMessage("Are you sure that you want to delete all saved data?")

                    setNegativeButton(
                        R.string.cancel,
                        DialogInterface.OnClickListener { _, _ ->
                        })

                    setPositiveButton(
                        R.string.delete,
                        DialogInterface.OnClickListener { _, _ ->
                            db.eraseData()
                            exitProcess(-1)
                        })
                }
                builder.create().show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}