package com.example.phoneshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.phoneshopapp.adapters.AddressesAdapter;
import com.example.phoneshopapp.managers.AddressManager;
import com.example.phoneshopapp.models.Address;
import com.example.phoneshopapp.repositories.callbacks.AddressesCallback;
import com.example.phoneshopapp.repositories.callbacks.UpdateCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class ManageAddressesActivity extends AppCompatActivity implements AddressesAdapter.OnAddressActionListener {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewAddresses;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialTextView textEmptyState;
    private FloatingActionButton fabAddAddress;
    
    private AddressesAdapter addressesAdapter;
    private AddressManager addressManager;
    private List<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_addresses);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        
        // Initialize manager
        addressManager = AddressManager.getInstance(this);
        
        // Load addresses
        loadAddresses();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewAddresses = findViewById(R.id.recyclerViewAddresses);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        textEmptyState = findViewById(R.id.textEmptyState);
        fabAddAddress = findViewById(R.id.fabAddAddress);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Địa Chỉ Giao Hàng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        addresses = new ArrayList<>();
        addressesAdapter = new AddressesAdapter(this, addresses, this);
        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAddresses.setAdapter(addressesAdapter);
    }

    private void setupClickListeners() {
        fabAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditAddressActivity.class);
            startActivityForResult(intent, AddEditAddressActivity.REQUEST_ADD_ADDRESS);
        });

        swipeRefreshLayout.setOnRefreshListener(this::loadAddresses);
    }

    private void loadAddresses() {
        swipeRefreshLayout.setRefreshing(true);
        
        addressManager.getUserAddresses(new AddressesCallback() {
            @Override
            public void onSuccess(List<Address> addressList) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    addresses.clear();
                    addresses.addAll(addressList);
                    addressesAdapter.notifyDataSetChanged();
                    
                    updateEmptyState();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(ManageAddressesActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    updateEmptyState();
                });
            }
        });
    }

    private void updateEmptyState() {
        if (addresses.isEmpty()) {
            recyclerViewAddresses.setVisibility(View.GONE);
            textEmptyState.setVisibility(View.VISIBLE);
            textEmptyState.setText("Chưa có địa chỉ giao hàng.\nThêm địa chỉ mới để tiếp tục.");
        } else {
            recyclerViewAddresses.setVisibility(View.VISIBLE);
            textEmptyState.setVisibility(View.GONE);
        }
    }

    // AddressesAdapter.OnAddressActionListener implementation
    @Override
    public void onEditAddress(Address address) {
        Intent intent = new Intent(this, AddEditAddressActivity.class);
        intent.putExtra(AddEditAddressActivity.EXTRA_ADDRESS_ID, address.getAddressId());
        intent.putExtra(AddEditAddressActivity.EXTRA_ADDRESS_NAME, address.getAddressName());
        intent.putExtra(AddEditAddressActivity.EXTRA_RECIPIENT_NAME, address.getRecipientName());
        intent.putExtra(AddEditAddressActivity.EXTRA_PHONE, address.getPhone());
        intent.putExtra(AddEditAddressActivity.EXTRA_ADDRESS_DETAILS, address.getAddressDetails());
        intent.putExtra(AddEditAddressActivity.EXTRA_WARD, address.getWard());
        intent.putExtra(AddEditAddressActivity.EXTRA_DISTRICT, address.getDistrict());
        intent.putExtra(AddEditAddressActivity.EXTRA_PROVINCE, address.getProvince());
        intent.putExtra(AddEditAddressActivity.EXTRA_IS_DEFAULT, address.isDefault());
        startActivityForResult(intent, AddEditAddressActivity.REQUEST_EDIT_ADDRESS);
    }

    @Override
    public void onDeleteAddress(Address address) {
        // Confirm deletion
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa địa chỉ")
                .setMessage("Bạn có chắc chắn muốn xóa địa chỉ \"" + address.getAddressName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteAddress(address);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onSetDefaultAddress(Address address) {
        addressManager.setDefaultAddress(address.getAddressId(), new UpdateCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(ManageAddressesActivity.this, 
                            "Đã đặt làm địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                    loadAddresses(); // Refresh to update default status
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(ManageAddressesActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void deleteAddress(Address address) {
        addressManager.deleteAddress(address.getAddressId(), new UpdateCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(ManageAddressesActivity.this, 
                            "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show();
                    loadAddresses(); // Refresh list
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(ManageAddressesActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == AddEditAddressActivity.REQUEST_ADD_ADDRESS) {
                Toast.makeText(this, "Đã thêm địa chỉ mới", Toast.LENGTH_SHORT).show();
                loadAddresses();
            } else if (requestCode == AddEditAddressActivity.REQUEST_EDIT_ADDRESS) {
                Toast.makeText(this, "Đã cập nhật địa chỉ", Toast.LENGTH_SHORT).show();
                loadAddresses();
            }
        }
    }
}