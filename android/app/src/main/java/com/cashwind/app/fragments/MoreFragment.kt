package com.cashwind.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.view.Gravity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import com.cashwind.app.R
import com.cashwind.app.adapter.DashboardCardAdapter
import com.cashwind.app.util.DashboardItemTouchHelper
import com.cashwind.app.util.DashboardCardManager

class MoreFragment : Fragment() {
    private lateinit var adapter: DashboardCardAdapter
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(20, 60, 20, 40)
        }

        val headerLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(requireContext()).apply {
            text = "More"
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_display))
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val editButton = Button(requireContext()).apply {
            text = "Edit"
            setOnClickListener {
                isEditMode = !isEditMode
                text = if (isEditMode) "Done" else "Edit"
                adapter.setEditMode(isEditMode)
            }
        }

        headerLayout.addView(title)
        headerLayout.addView(editButton)
        mainLayout.addView(headerLayout)

        val recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(0, 40, 0, 0)
            }
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        val cards = DashboardCardManager.getCards(requireContext(), "more")

        adapter = DashboardCardAdapter(cards,
            onCardOrderChanged = { updatedCards ->
                DashboardCardManager.saveCardOrder(requireContext(), updatedCards)
            },
            onMoveCard = { cardId, newLocation ->
                DashboardCardManager.moveCard(requireContext(), cardId, newLocation)
                adapter.removeCard(cardId)
                refreshFragment()
            },
            onCardClick = { card ->
                card.activityClass?.let { activityClass ->
                    val intent = android.content.Intent(requireContext(), activityClass)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }
        )
        recyclerView.adapter = adapter

        val touchHelper = ItemTouchHelper(DashboardItemTouchHelper(adapter))
        touchHelper.attachToRecyclerView(recyclerView)

        mainLayout.addView(recyclerView)

        return mainLayout
    }

    private fun refreshFragment() {
        parentFragmentManager.beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
    }
}
