package com.cashwind.app.fragments

import android.content.Context
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
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class HomeFragment : Fragment() {
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

        val titleLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val title = TextView(requireContext()).apply {
            text = "Cashwind"
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_display))
            setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
        }

        val versionText = TextView(requireContext()).apply {
            try {
                val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
                text = "v${packageInfo.versionName}"
            } catch (e: Exception) {
                text = "v1.3.0"
            }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }

        titleLayout.addView(title)
        titleLayout.addView(versionText)

        val editButton = Button(requireContext()).apply {
            text = "Edit"
            setOnClickListener {
                isEditMode = !isEditMode
                text = if (isEditMode) "Done" else "Edit"
                adapter.setEditMode(isEditMode)
            }
        }

        headerLayout.addView(titleLayout)
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

        val cards = DashboardCardManager.getCards(requireContext(), "home")

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
                try {
                    card.activityClass?.let { activityClass ->
                        val activity = activity ?: return@let
                        val intent = android.content.Intent(activity, activityClass)
                        activity.startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(context, "Error opening: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        )
        recyclerView.adapter = adapter

        val touchHelper = ItemTouchHelper(DashboardItemTouchHelper(adapter))
        touchHelper.attachToRecyclerView(recyclerView)

        mainLayout.addView(recyclerView)

        loadDashboardData()

        return mainLayout
    }

    private fun refreshFragment() {
        parentFragmentManager.beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
    }

    private fun loadDashboardData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val database = CashwindDatabase.getInstance(requireContext())
                val allBills = withContext(Dispatchers.IO) {
                    database.billDao().getAllBillsDirect()
                }
                
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                
                val currentMonthBills = allBills.filter { bill ->
                    bill.dueDate.startsWith("$year-${String.format("%02d", month)}")
                }
                
                val total = currentMonthBills.sumOf { it.amount }
                adapter.updateCardSubtitle("bills", "Due this month: $${String.format("%.2f", total)}")
                
                val today = DateUtils.formatIsoDate(Date())
                val pastDueBills = allBills.filter { bill ->
                    !bill.isPaid && bill.dueDate < today
                }
                
                adapter.updateCardSubtitle("pastdue", "${pastDueBills.size} bills")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
