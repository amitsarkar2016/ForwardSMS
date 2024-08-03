package com.otpforward.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.otpforward.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


class SwipeToDeleteCallback<T>(
    context: Context,
    private val onDelete: (item: T) -> Unit,
    private val adapter: SwipeAdapter<T>
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteColor = ContextCompat.getColor(context, R.color.red_white)
    private val deleteIcon = R.drawable.ic_delete

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val swipedItem = adapter.getCurrentList()[position]
        (adapter as RecyclerView.Adapter<*>).notifyItemChanged(position)
        onDelete(swipedItem)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        RecyclerViewSwipeDecorator.Builder(
            c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
        ).addSwipeLeftBackgroundColor(deleteColor)
            .addSwipeLeftActionIcon(deleteIcon)
            .addSwipeRightLabel("Delete")
            .setSwipeLeftLabelColor(Color.WHITE)
            .setIconHorizontalMargin(TypedValue.COMPLEX_UNIT_DIP, 40)
            .addCornerRadius(TypedValue.COMPLEX_UNIT_DIP, 16)
            .create()
            .decorate()

        super.onChildDraw(
            c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
        )
    }
}


interface SwipeAdapter<T> {
    fun getCurrentList(): List<T>
}
