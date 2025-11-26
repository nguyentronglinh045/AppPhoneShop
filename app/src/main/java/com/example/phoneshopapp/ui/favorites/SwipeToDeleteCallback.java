package com.example.phoneshopapp.ui.favorites;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phoneshopapp.R;

/**
 * Swipe-to-delete callback for favorites RecyclerView
 * Allows users to swipe left to remove items from favorites
 */
public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private FavoritesAdapter adapter;
    private Drawable deleteIcon;
    private final ColorDrawable background;

    public SwipeToDeleteCallback(FavoritesAdapter adapter) {
        super(0, ItemTouchHelper.LEFT);
        this.adapter = adapter;
        this.background = new ColorDrawable(Color.parseColor("#F44336")); // error_color
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, 
                         @NonNull RecyclerView.ViewHolder viewHolder, 
                         @NonNull RecyclerView.ViewHolder target) {
        return false; // We don't support move
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        adapter.removeItem(position);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                           @NonNull RecyclerView.ViewHolder viewHolder,
                           float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        // Lazy load delete icon
        if (deleteIcon == null) {
            deleteIcon = ContextCompat.getDrawable(itemView.getContext(), 
                android.R.drawable.ic_menu_delete);
            if (deleteIcon != null) {
                deleteIcon.setTint(Color.WHITE);
            }
        }

        int iconMargin = (itemView.getHeight() - (deleteIcon != null ? deleteIcon.getIntrinsicHeight() : 0)) / 2;
        int iconTop = itemView.getTop() + iconMargin;
        int iconBottom = iconTop + (deleteIcon != null ? deleteIcon.getIntrinsicHeight() : 0);

        if (dX < 0) { // Swiping to the left
            int iconLeft = itemView.getRight() - iconMargin - (deleteIcon != null ? deleteIcon.getIntrinsicWidth() : 0);
            int iconRight = itemView.getRight() - iconMargin;
            
            if (deleteIcon != null) {
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            }

            background.setBounds(
                itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom()
            );
        } else { // View is unSwiped
            background.setBounds(0, 0, 0, 0);
            if (deleteIcon != null) {
                deleteIcon.setBounds(0, 0, 0, 0);
            }
        }

        background.draw(c);
        if (deleteIcon != null) {
            deleteIcon.draw(c);
        }
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.7f; // Require 70% swipe to trigger delete
    }
}
