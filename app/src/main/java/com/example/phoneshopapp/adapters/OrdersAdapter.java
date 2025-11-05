package com.example.phoneshopapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.models.Order;
import com.example.phoneshopapp.models.OrderStatus;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onReviewClick(Order order);  // Thêm method cho review
    }

    public OrdersAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders.clear();
        if (newOrders != null) {
            this.orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardOrder;
        private MaterialTextView textOrderId;
        private MaterialTextView textOrderDate;
        private MaterialTextView textOrderStatus;
        private MaterialTextView textTotalAmount;
        private MaterialTextView textItemCount;
        private MaterialButton btnReview;  // Thêm button review

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardOrder = itemView.findViewById(R.id.cardOrder);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textOrderDate = itemView.findViewById(R.id.textOrderDate);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
            textTotalAmount = itemView.findViewById(R.id.textTotalAmount);
            textItemCount = itemView.findViewById(R.id.textItemCount);
            btnReview = itemView.findViewById(R.id.btnReview);  // Init button
        }

        public void bind(Order order, OnOrderClickListener listener) {
            textOrderId.setText(order.getFormattedOrderId());
            textTotalAmount.setText(String.format("₫%.0f", order.getTotalAmount()));

            // Format date
            if (order.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                textOrderDate.setText(sdf.format(order.getCreatedAt()));
            }

            // Set status with appropriate color
            textOrderStatus.setText(order.getStatusDisplayName());
            setStatusColor(textOrderStatus, order.getOrderStatus());

            // Use real item count from order
            int itemCount = order.getTotalItemCount();
            textItemCount.setText(String.format("%d sản phẩm", itemCount));

            // Set click listener
            cardOrder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });

            // Handle Review button visibility and click
            handleReviewButton(order, listener);
        }

        /**
         * Handle Review button visibility and click logic
         * QUAN TRỌNG: 
         * - Chỉ hiển thị khi OrderStatus = DELIVERED
         * - Ẩn nút nếu order.hasReview = true (đã đánh giá rồi)
         */
        private void handleReviewButton(Order order, OnOrderClickListener listener) {
            // Kiểm tra điều kiện hiển thị nút Review
            if (order.getOrderStatus() == OrderStatus.DELIVERED) {
                // Đơn hàng đã giao - check hasReview
                if (order.isHasReview()) {
                    // Đã review rồi - ẩn nút
                    btnReview.setVisibility(View.GONE);
                } else {
                    // Chưa review - hiển thị nút
                    btnReview.setVisibility(View.VISIBLE);
                    btnReview.setEnabled(true);
                    btnReview.setText("Đánh giá sản phẩm");
                    
                    // Set click listener
                    btnReview.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onReviewClick(order);
                        }
                    });
                }
            } else {
                // Đơn hàng chưa giao - ẩn nút
                btnReview.setVisibility(View.GONE);
            }
        }

        private void setStatusColor(MaterialTextView textView, OrderStatus status) {
            int color;
            int backgroundColor;

            switch (status) {
                case PENDING:
                    color = ContextCompat.getColor(itemView.getContext(), R.color.status_pending);
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.status_pending_bg);
                    break;
                case CONFIRMED:
                    color = ContextCompat.getColor(itemView.getContext(), R.color.status_confirmed);
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.status_confirmed_bg);
                    break;
                case SHIPPING:
                    color = ContextCompat.getColor(itemView.getContext(), R.color.status_shipping);
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.status_shipping_bg);
                    break;
                case DELIVERED:
                    color = ContextCompat.getColor(itemView.getContext(), R.color.status_delivered);
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.status_delivered_bg);
                    break;
                case CANCELLED:
                    color = ContextCompat.getColor(itemView.getContext(), R.color.status_cancelled);
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.status_cancelled_bg);
                    break;
                default:
                    color = ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray);
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), android.R.color.transparent);
                    break;
            }

            textView.setTextColor(color);
            textView.setBackgroundColor(backgroundColor);
        }
    }
}