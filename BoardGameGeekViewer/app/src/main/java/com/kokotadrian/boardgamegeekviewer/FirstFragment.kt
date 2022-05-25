package com.kokotadrian.boardgamegeekviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kokotadrian.boardgamegeekviewer.databinding.FragmentFirstBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*



/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val config = (activity as MainActivity).db.getConfig() ?: return

        val games = (activity as MainActivity).db.getGameList()

        val formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.GERMAN)
            .withZone(ZoneId.systemDefault())

        binding.helloTextView.setText("Hello " + config.username + " you have " + games.size + " games")
        if (config.lastSync != null) {
            binding.lastSyncTextView.setText("Last sync: " + formatter.format(config.lastSync))
        }

        binding.synchronizationButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.buttonFirst2.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_gameListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}