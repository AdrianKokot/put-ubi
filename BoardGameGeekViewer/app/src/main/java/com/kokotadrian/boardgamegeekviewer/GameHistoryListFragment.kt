package com.kokotadrian.boardgamegeekviewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kokotadrian.boardgamegeekviewer.databinding.FragmentGameHistoryListBinding
import com.kokotadrian.boardgamegeekviewer.databinding.FragmentGameListBinding
import java.lang.Exception
import java.time.format.FormatStyle

class GameHistoryListFragment : Fragment() {

    private var generatedRowsCount: Int = 0
    private lateinit var historyList: MutableList<GameRank>
    private var paddingX: Int = 0
    private var paddingY: Int = 0
    private lateinit var table: TableLayout
    private lateinit var gameInfo: CollectionItemInfo

    private var _binding: FragmentGameHistoryListBinding? = null
    private val binding get() = _binding!!

    private val db get() = (activity as MainActivity).db

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        gameInfo = HistoryDetailsView.viewedGame ?: throw Exception("Game not selected!")
        setBarTitle(gameInfo.name)

        _binding = FragmentGameHistoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        table = binding.tableLayout
        paddingX = resources.getDimensionPixelOffset(R.dimen.rowPaddingX)
        paddingY = resources.getDimensionPixelOffset(R.dimen.rowPaddingY)

        historyList = db.getGameRankList(gameInfo.id)

        val scrollView = binding.scrollView

        val scrollBuffer = 200

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val bottomPos = scrollView.scrollY + scrollView.height

            if (bottomPos + scrollBuffer >= table.height) {
                generateRows()
            }
        }

        generateRows()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun generateRows(count: Int = 30) {
        val unt = (generatedRowsCount + count).coerceAtMost(historyList.size)

        for (i in generatedRowsCount until unt) {
            val row = TableRow(activity)

            // No. view
            val noView = TextView(activity)
            noView.text = (i + 1).toString()
            row.addView(noView)

            // Rank
            val rankView = TextView(activity)
            rankView.text = historyList[i].rank.toString()
            row.addView(rankView)

            // Rank
            val dateView = TextView(activity)
            dateView.text = historyList[i].getFormattedDate()
            row.addView(dateView)

            // Row
            row.setPadding(0, paddingY, 0, paddingY * 4)

            table.addView(row)
        }

        generatedRowsCount += count + 1
    }

}