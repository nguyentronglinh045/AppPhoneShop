package com.example.phoneshopapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.models.Order;
import com.example.phoneshopapp.models.OrderStatus;
import com.example.phoneshopapp.repositories.FirebaseOrderRepository;
import com.example.phoneshopapp.repositories.OrderRepository;
import com.example.phoneshopapp.repositories.callbacks.OrdersCallback;
import com.example.phoneshopapp.repositories.callbacks.UpdateCallback;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for managing orders in admin panel
 */
public class OrdersTabFragment extends Fragment {
  private RecyclerView recyclerOrders;
  private OrderAdminAdapter adapter;
  private List<Order> orderList = new ArrayList<>();
  private OrderRepository orderRepository;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_orders_tab, container, false);

    recyclerOrders = root.findViewById(R.id.recyclerOrders);

    // Initialize repository
    orderRepository = new FirebaseOrderRepository();

    // Setup RecyclerView
    adapter = new OrderAdminAdapter(orderList, this::onChangeOrderStatus);
    recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerOrders.setAdapter(adapter);

    // Load all orders
    loadAllOrders();

    return root;
  }

  private void loadAllOrders() {
    orderRepository.getAllOrders(new OrdersCallback() {
      @Override
      public void onSuccess(List<Order> orders) {
        orderList.clear();
        orderList.addAll(orders);
        adapter.notifyDataSetChanged();

        if (orders.isEmpty()) {
          Toast.makeText(getContext(), "No orders found", Toast.LENGTH_SHORT).show();
        }
      }

      @Override
      public void onError(String error) {
        Toast.makeText(getContext(), "Error loading orders: " + error, Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void onChangeOrderStatus(Order order) {
    // Show dialog to select new status
    OrderStatus[] statuses = OrderStatus.values();
    String[] statusNames = new String[statuses.length];
    for (int i = 0; i < statuses.length; i++) {
      statusNames[i] = statuses[i].getDisplayName();
    }

    new AlertDialog.Builder(requireContext())
        .setTitle("Change Order Status")
        .setItems(statusNames, (dialog, which) -> {
          OrderStatus newStatus = statuses[which];

          // Confirm status change
          new AlertDialog.Builder(requireContext())
              .setTitle("Confirm Status Change")
              .setMessage("Change order " + order.getOrderId() + " to " + newStatus.getDisplayName() + "?")
              .setPositiveButton("Confirm", (d, w) -> {
                updateOrderStatus(order.getOrderId(), newStatus);
              })
              .setNegativeButton("Cancel", null)
              .show();
        })
        .setNegativeButton("Cancel", null)
        .show();
  }

  private void updateOrderStatus(String orderId, OrderStatus newStatus) {
    orderRepository.updateOrderStatus(orderId, newStatus, new UpdateCallback() {
      @Override
      public void onSuccess() {
        Toast.makeText(getContext(), "Order status updated successfully", Toast.LENGTH_SHORT).show();
        // Reload orders to show updated status
        loadAllOrders();
      }

      @Override
      public void onError(String error) {
        Toast.makeText(getContext(), "Error updating status: " + error, Toast.LENGTH_SHORT).show();
      }
    });
  }
}
