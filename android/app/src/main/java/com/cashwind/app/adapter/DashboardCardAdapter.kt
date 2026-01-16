package com.cashwind.app.adapter

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cashwind.app.R
import com.cashwind.app.model.DashboardCard
import java.util.Collections

class DashboardCardAdapter(
    private val cards: MutableList<DashboardCard>,
    private val onCardOrderChanged: (List<DashboardCard>) -> Unit,
    private val onMoveCard: ((String, String) -> Unit)? = null,
    private var isEditMode: Boolean = false
) : RecyclerView.Adapter<DashboardCardAdapter.CardViewHolder>() {

    inner class CardViewHolder(
        val cardLayout: LinearLayout,
        val emojiText: TextView,
        val titleText: TextView,
        val subtitleText: TextView,
        val moveButton: TextView
    ) : RecyclerView.ViewHolder(cardLayout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val context = parent.context
        val cardLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }

            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#F5F5F5"))
                cornerRadius = 32f
            }
            background = bg
        }

        val emojiText = TextView(context).apply {
            tag = "emoji"
            textSize = 48f
            gravity = Gravity.CENTER
        }

        val titleText = TextView(context).apply {
            tag = "title"
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.text_size_title))
            setTextColor(Color.parseColor("#1A1C19"))
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 8)
        }

        val subtitleText = TextView(context).apply {
            tag = "subtitle"
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.text_size_body))
            setTextColor(Color.parseColor("#424940"))
            gravity = Gravity.CENTER
        }

        val moveButton = TextView(context).apply {
            tag = "moveButton"
            text = "⇄ Move"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(24, 16, 24, 16)
            visibility = View.GONE
            
            val buttonBg = GradientDrawable().apply {
                setColor(Color.parseColor("#006E26"))
                cornerRadius = 16f
            }
            background = buttonBg
            
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 0)
            }
        }

        cardLayout.addView(emojiText)
        cardLayout.addView(titleText)
        cardLayout.addView(subtitleText)
        cardLayout.addView(moveButton)

        return CardViewHolder(cardLayout, emojiText, titleText, subtitleText, moveButton)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        
        holder.emojiText.text = card.emoji
        holder.titleText.text = card.title
        holder.subtitleText.text = card.subtitle
        
        holder.moveButton.visibility = if (isEditMode) View.VISIBLE else View.GONE
        
        holder.moveButton.setOnClickListener {
            val newLocation = if (card.location == "home") "more" else "home"
            onMoveCard?.invoke(card.id, newLocation)
        }

        holder.cardLayout.apply {
            isClickable = card.activityClass != null && !isEditMode
            isFocusable = card.activityClass != null && !isEditMode
            setOnClickListener {
                if (!isEditMode) {
                    card.activityClass?.let { activityClass ->
                        context.startActivity(Intent(context, activityClass))
                    }
                }
            }
        }
    }

    override fun getItemCount() = cards.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(cards, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(cards, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        onCardOrderChanged(cards)
    }

    fun updateCardSubtitle(cardId: String, newSubtitle: String) {
        val position = cards.indexOfFirst { it.id == cardId }
        if (position != -1) {
            cards[position] = cards[position].copy(subtitle = newSubtitle)
            notifyItemChanged(position)
        }
    }

    fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        notifyDataSetChanged()
    }

    fun removeCard(cardId: String) {
        val position = cards.indexOfFirst { it.id == cardId }
        if (position != -1) {
            cards.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
