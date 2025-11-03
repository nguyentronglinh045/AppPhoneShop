package com.example.phoneshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class ManageAddressesActivity extends AppCompatActivity implements AddressesAdapter.OnAddressActionListener {

    private androidx.appcompat.widget.Toolbar toolbar;
    private RecyclerView recyclerViewAddresses;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutEmpty;
    private TextView textEmptyMessage;
    private MaterialButton btnAddFirstAddress;
    private FloatingActionButton fabAddAddress;
    
    private AddressesAdapter addressesAdapter;
    private AddressManager addressManager;
    private List<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.d("ManageAddresses", "onCreate started");
            setContentView(R.layout.activity_manage_addresses);

            initViews();
            setupToolbar();
            setupRecyclerView();
            setupClickListeners();
            
            // Initialize manager
            Log.d("ManageAddresses", "Initializing AddressManager");
            addressManager = AddressManager.getInstance(this);
            
            if (addressManager == null) {
                Log.e("ManageAddresses", "Failed to initialize AddressManager");
                Toast.makeText(this, "Lỗi khởi tạo hệ thống", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            Log.d("ManageAddresses", "AddressManager initialized successfully");
            
            // Load addresses
            loadAddresses();
            
        } catch (Exception e) {
            Log.e("ManageAddresses", "Exception in onCreate", e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        Log.d("ManageAddresses", "initViews started");
        
        toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            Log.e("ManageAddresses", "toolbar is null");
            throw new RuntimeException("toolbar not found in layout");
        }
        
        recyclerViewAddresses = findViewById(R.id.recyclerViewAddresses);
        if (recyclerViewAddresses == null) {
            Log.e("ManageAddresses", "recyclerViewAddresses is null");
            throw new RuntimeException("recyclerViewAddresses not found in layout");
        }
        
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout == null) {
            Log.e("ManageAddresses", "swipeRefreshLayout is null");
            throw new RuntimeException("swipeRefreshLayout not found in layout");
        }
        
        layoutEmpty = findViewById(R.id.layoutEmpty);
        if (layoutEmpty == null) {
            Log.e("ManageAddresses", "layoutEmpty is null");
            throw new RuntimeException("layoutEmpty not found in layout");
        }
        
        // Find TextView inside layoutEmpty for displaying empty message
        textEmptyMessage = layoutEmpty.findViewById(R.id.textEmptyMessage);
        if (textEmptyMessage == null) {
            Log.e("ManageAddresses", "textEmptyMessage is null");
        }
        
        btnAddFirstAddress = layoutEmpty.findViewById(R.id.btnAddFirstAddress);
        if (btnAddFirstAddress == null) {
            Log.e("ManageAddresses", "btnAddFirstAddress is null");
        }
        
        fabAddAddress = findViewById(R.id.fabAddAddress);
        if (fabAddAddress == null) {
            Log.e("ManageAddresses", "fabAddAddress is null");
            throw new RuntimeException("fabAddAddress not found in layout");
        }
        
        Log.d("ManageAddresses", "initViews completed successfully");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Địa Chỉ Giao Hàng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
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

        // Add click listener for "Add First Address" button in empty state
        if (btnAddFirstAddress != null) {
            btnAddFirstAddress.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddEditAddressActivity.class);
                startActivityForResult(intent, AddEditAddressActivity.REQUEST_ADD_ADDRESS);
            });
        }

        swipeRefreshLayout.setOnRefreshListener(this::loadAddresses);
    }

    private void loadAddresses() {
        swipeRefreshLayout.setRefreshing(true);
        
        try {
            // Check if addressManager is properly initialized
            if (addressManager == null) {
                Log.e("ManageAddresses", "AddressManager is null!");
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "Lỗi khởi tạo AddressManager", Toast.LENGTH_LONG).show();
                updateEmptyState();
                return;
            }
            
            addressManager.getUserAddresses(new AddressesCallback() {
                @Override
                public void onSuccess(List<Address> addressList) {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        addresses.clear();
                        if (addressList != null) {
                            addresses.addAll(addressList);
                        }
                        addressesAdapter.notifyDataSetChanged();
                        
                        updateEmptyState();
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Log.e("ManageAddresses", "Error loading addresses: " + errorMessage);
                        Toast.makeText(ManageAddressesActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        updateEmptyState();
                    });
                }
            });
        } catch (Exception e) {
            Log.e("ManageAddresses", "Exception in loadAddresses", e);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "Lỗi không mong muốn: " + e.getMessage(), Toast.LENGTH_LONG).show();
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (addresses.isEmpty()) {
            recyclerViewAddresses.setVisibility(View.GONE);
            if (layoutEmpty != null) {
                layoutEmpty.setVisibility(View.VISIBLE);
                if (textEmptyMessage != null) {
                    textEmptyMessage.setText("Chưa có địa chỉ giao hàng.\nThêm địa chỉ mới để tiếp tục.");
                }
            }
        } else {
            recyclerViewAddresses.setVisibility(View.VISIBLE);
            if (layoutEmpty != null) {
                layoutEmpty.setVisibility(View.GONE);
            }
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