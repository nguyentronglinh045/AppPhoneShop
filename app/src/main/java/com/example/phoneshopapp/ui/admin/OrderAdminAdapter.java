package com.example.phoneshopapp.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.models.Order;
import com.example.phoneshopapp.models.OrderItem;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying orders in admin panel
 */
public class OrderAdminAdapter extends RecyclerView.Adapter<OrderAdminAdapter.OrderViewHolder> {
  private final List<Order> orders;
  private final OnChangeStatusListener onChangeStatusListener;

  public interface OnChangeStatusListener {
    void onChangeStatus(Order order);
  }

  public OrderAdminAdapter(List<Order> orders, OnChangeStatusListener onChangeStatusListener) {
    this.orders = orders;
    this.onChangeStatusListener = onChangeStatusListener;
  }

  @NonNull
  @Override
  public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
    return new OrderViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
    Order order = orders.get(position);

    // Display order ID
    holder.textOrderId.setText("Đơn hàng: " + order.getOrderId());

    // Display status
    holder.textOrderStatus.setText(order.getStatusDisplayName());

    // Display customer name
    if (order.getCustomerInfo() != null) {
      holder.textCustomerName.setText("Khách hàng: " + order.getCustomerInfo().getFullName());
    } else {
      holder.textCustomerName.setText("Khách hàng: Không có");
    }

    // Display order date
    if (order.getCreatedAt() != null) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
      holder.textOrderDate.setText("Ngày: " + dateFormat.format(order.getCreatedAt()));
    } else {
      holder.textOrderDate.setText("Ngày: Không có");
    }

    // Display total amount
    NumberFormat currencyFormat = NumberFormat
        .getCurrencyInstance(new Locale.Builder().setLanguage("vi").setRegion("VN").build());
    holder.textOrderTotal.setText("Tổng cộng: " + currencyFormat.format(order.getTotalAmount()));

    // Setup product items recycler view
    if (order.getItems() != null && !order.getItems().isEmpty()) {
      // Configure RecyclerView
      LinearLayoutManager layoutManager = new LinearLayoutManager(holder.recyclerOrderProducts.getContext());
      holder.recyclerOrderProducts.setLayoutManager(layoutManager);

      // Create and set adapter
      OrderItemMiniAdapter itemAdapter = new OrderItemMiniAdapter(order.getItems(), 3);
      holder.recyclerOrderProducts.setAdapter(itemAdapter);

      // Make RecyclerView visible
      holder.recyclerOrderProducts.setVisibility(View.VISIBLE);
    } else {
      holder.recyclerOrderProducts.setVisibility(View.GONE);
    }

    // Set click listener for change status button
    holder.btnChangeStatus.setOnClickListener(v -> onChangeStatusListener.onChangeStatus(order));
  }

  @Override
  public int getItemCount() {
    return orders.size();
  }

  static class OrderViewHolder extends RecyclerView.ViewHolder {
    TextView textOrderId, textOrderStatus, textCustomerName, textOrderDate, textOrderTotal;
    RecyclerView recyclerOrderProducts;
    Button btnChangeStatus;

    OrderViewHolder(View itemView) {
      super(itemView);
      textOrderId = itemView.findViewById(R.id.textOrderId);
      textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
      textCustomerName = itemView.findViewById(R.id.textCustomerName);
      textOrderDate = itemView.findViewById(R.id.textOrderDate);
      textOrderTotal = itemView.findViewById(R.id.textOrderTotal);
      recyclerOrderProducts = itemView.findViewById(R.id.recyclerOrderProducts);
      btnChangeStatus = itemView.findViewById(R.id.btnChangeStatus);
    }
  }
}
