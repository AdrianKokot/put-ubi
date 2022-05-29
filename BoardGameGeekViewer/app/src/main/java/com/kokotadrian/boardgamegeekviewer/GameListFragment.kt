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
import com.kokotadrian.boardgamegeekviewer.databinding.FragmentGameListBinding

class GameListFragment : Fragment() {

    private lateinit var itemList: MutableList<CollectionItemInfo>

    private var _binding: FragmentGameListBinding? = null
    private val binding get() = _binding!!

    private val db get() = (activity as MainActivity).db

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setBarTitle(if (ListType.isGameList()) "Game List" else "Expansion List")
        _binding = FragmentGameListBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var generatedRowsCount = 0
    private var table: TableLayout? = null

    private var paddingX: Int = 0
    private var paddingY: Int = 0

    @SuppressLint("SetTextI18n")
    private fun generateRows(count: Int = 30) {

        val unt = (generatedRowsCount + count).coerceAtMost(itemList.size)

        for (i in generatedRowsCount until unt) {
            val row = TableRow(activity)

            // No. view
            val noView = TextView(activity)
            noView.text = (i + 1).toString()
            row.addView(noView)

            // Thumb
            val thumbView = ImageView(activity)
            val bitmap = ImageCacheSingleton.getCachedValue(itemList[i].thumbnail)
            if (bitmap != null) {
                thumbView.setImageBitmap(bitmap)
            } else {
                DownloadImageTask(thumbView).execute(itemList[i].thumbnail)
            }

            thumbView.setPadding(-paddingX * 2, 0, paddingX * 2, 0)
            row.addView(thumbView)

            // Title
            val titleView = TextView(activity)
            titleView.text = itemList[i].name

            titleView.setPadding(0, 0, paddingX * 2, 0)
            row.addView(titleView)

            // Release
            val releaseView = TextView(activity)
            releaseView.text = itemList[i].yearPublished.toString()
            row.addView(releaseView)

            if (ListType.isGameList()) {
                // Rank
                val rankView = TextView(activity)
                rankView.text = itemList[i].boardGameRank.toString()
                row.addView(rankView)

                // Row listener
                row.isClickable = true
                row.setOnClickListener {

                }
            }

            // Row
            row.setPadding(0, paddingY, 0, paddingY * 8)

            table?.addView(row)
        }

        generatedRowsCount += count + 1
    }

    private var currSort = ""
    private fun orderBy(column: String) {
        val childCount = table?.childCount
        if (childCount != null) {
            if (childCount > 1) {
                table?.removeViews(1, childCount - 1);
            }
        }

        if (column == currSort) {
            itemList.reverse()
        } else {
            when (column) {
                "ReleaseYear" -> itemList.sortBy { it.yearPublished }
                "Rank" -> itemList.sortBy { it.boardGameRank }
                else -> itemList.sortBy { it.name }
            }
        }

        currSort = column

        generatedRowsCount = 0
        generateRows()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        table = binding.tableLayout
        paddingX = resources.getDimensionPixelOffset(R.dimen.rowPaddingX)
        paddingY = resources.getDimensionPixelOffset(R.dimen.rowPaddingY)

        itemList = if (ListType.isGameList()) {
            binding.rankTextView.setOnClickListener {
                orderBy("Rank")
            }

            db.getGameList().map { CollectionItemInfo.from(it) }.toMutableList()
        } else {
            binding.rankTextView.visibility = View.GONE

            db.getExpansionList().map { CollectionItemInfo.from(it) }.toMutableList()
        }

        orderBy("Title")

        binding.titleTextView.setOnClickListener {
            orderBy("Title")
        }

        binding.releaseYearTextView.setOnClickListener {
            orderBy("ReleaseYear")
        }

        val scrollView = binding.scrollView

        val scrollBuffer = 200

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            if (table != null) {
                val bottomPos = scrollView.scrollY + scrollView.height

                if (bottomPos + scrollBuffer >= table!!.height) {
                    generateRows(30)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}