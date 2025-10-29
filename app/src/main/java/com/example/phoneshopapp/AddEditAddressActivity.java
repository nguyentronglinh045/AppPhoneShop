package com.example.phoneshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.phoneshopapp.managers.AddressManager;
import com.example.phoneshopapp.repositories.callbacks.UpdateCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditAddressActivity extends AppCompatActivity {

    // Request codes
    public static final int REQUEST_ADD_ADDRESS = 1001;
    public static final int REQUEST_EDIT_ADDRESS = 1002;
    
    // Extra keys
    public static final String EXTRA_ADDRESS_ID = "address_id";
    public static final String EXTRA_ADDRESS_NAME = "address_name";
    public static final String EXTRA_RECIPIENT_NAME = "recipient_name";
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_ADDRESS_DETAILS = "address_details";
    public static final String EXTRA_WARD = "ward";
    public static final String EXTRA_DISTRICT = "district";
    public static final String EXTRA_PROVINCE = "province";
    public static final String EXTRA_IS_DEFAULT = "is_default";

    // UI Components
    private MaterialToolbar toolbar;
    private TextInputEditText editTextAddressName, editTextRecipientName, editTextPhone, editTextAddressDetails;
    private Spinner spinnerProvince, spinnerDistrict, spinnerWard;
    private SwitchMaterial switchDefault;
    private MaterialButton btnSave, btnCancel;

    // Data
    private AddressManager addressManager;
    private String addressId; // null for new address
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_address);

        initViews();
        setupToolbar();
        setupSpinners();
        setupClickListeners();
        
        // Initialize manager
        addressManager = AddressManager.getInstance(this);
        
        // Check if editing existing address
        checkEditMode();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        editTextAddressName = findViewById(R.id.editTextAddressName);
        editTextRecipientName = findViewById(R.id.editTextRecipientName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddressDetails = findViewById(R.id.editTextAddressDetails);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerWard = findViewById(R.id.spinnerWard);
        switchDefault = findViewById(R.id.switchDefault);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSpinners() {
        // Vietnam provinces/cities
        String[] provinces = {
            "Chọn Tỉnh/Thành phố",
            "TP. Hồ Chí Minh",
            "Hà Nội", 
            "Đà Nẵng",
            "Hải Phòng",
            "Cần Thơ",
            "An Giang",
            "Bà Rịa - Vũng Tàu",
            "Bắc Giang",
            "Bắc Kạn",
            "Bạc Liêu"
            // Add more provinces as needed
        };
        
        // Sample districts (in real app, this would be dynamic based on province selection)
        String[] districts = {
            "Chọn Quận/Huyện",
            "Quận 1",
            "Quận 2", 
            "Quận 3",
            "Quận 4",
            "Quận 5",
            "Quận 6",
            "Quận 7",
            "Quận 8",
            "Quận 9",
            "Quận 10"
            // Add more districts
        };
        
        // Sample wards (in real app, this would be dynamic based on district selection)
        String[] wards = {
            "Chọn Phường/Xã",
            "Phường Bến Nghé",
            "Phường Bến Thành",
            "Phường Cầu Kho",
            "Phường Cầu Ông Lãnh",
            "Phường Cô Giang",
            "Phường Đa Kao",
            "Phường Nguyễn Cư Trinh",
            "Phường Nguyễn Thái Bình",
            "Phường Phạm Ngũ Lão",
            "Phường Tân Định"
            // Add more wards
        };

        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinces);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);

        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districts);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);

        ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wards);
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(wardAdapter);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveAddress());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        addressId = intent.getStringExtra(EXTRA_ADDRESS_ID);
        
        if (addressId != null && !addressId.isEmpty()) {
            // Edit mode
            isEditMode = true;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Sửa Địa Chỉ");
            }
            
            // Populate fields with existing data
            populateFields(intent);
        } else {
            // Add mode
            isEditMode = false;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Thêm Địa Chỉ");
            }
        }
    }

    private void populateFields(Intent intent) {
        editTextAddressName.setText(intent.getStringExtra(EXTRA_ADDRESS_NAME));
        editTextRecipientName.setText(intent.getStringExtra(EXTRA_RECIPIENT_NAME));
        editTextPhone.setText(intent.getStringExtra(EXTRA_PHONE));
        editTextAddressDetails.setText(intent.getStringExtra(EXTRA_ADDRESS_DETAILS));
        
        // Set spinner selections (simplified - in real app would need proper matching)
        String province = intent.getStringExtra(EXTRA_PROVINCE);
        String district = intent.getStringExtra(EXTRA_DISTRICT);
        String ward = intent.getStringExtra(EXTRA_WARD);
        
        setSpinnerSelection(spinnerProvince, province);
        setSpinnerSelection(spinnerDistrict, district);
        setSpinnerSelection(spinnerWard, ward);
        
        switchDefault.setChecked(intent.getBooleanExtra(EXTRA_IS_DEFAULT, false));
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value != null && spinner.getAdapter() != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveAddress() {
        if (!validateInput()) {
            return;
        }

        // Show loading
        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        String addressName = editTextAddressName.getText().toString().trim();
        String recipientName = editTextRecipientName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String addressDetails = editTextAddressDetails.getText().toString().trim();
        String province = spinnerProvince.getSelectedItem().toString();
        String district = spinnerDistrict.getSelectedItem().toString();
        String ward = spinnerWard.getSelectedItem().toString();
        boolean isDefault = switchDefault.isChecked();

        if (isEditMode) {
            // Update existing address
            updateAddress(addressName, recipientName, phone, addressDetails, ward, district, province, isDefault);
        } else {
            // Add new address
            addNewAddress(addressName, recipientName, phone, addressDetails, ward, district, province, isDefault);
        }
    }

    private boolean validateInput() {
        // Validate address name
        if (editTextAddressName.getText() == null || editTextAddressName.getText().toString().trim().isEmpty()) {
            editTextAddressName.setError("Vui lòng nhập tên địa chỉ");
            editTextAddressName.requestFocus();
            return false;
        }

        // Validate recipient name
        if (editTextRecipientName.getText() == null || editTextRecipientName.getText().toString().trim().isEmpty()) {
            editTextRecipientName.setError("Vui lòng nhập tên người nhận");
            editTextRecipientName.requestFocus();
            return false;
        }

        // Validate phone
        if (editTextPhone.getText() == null || editTextPhone.getText().toString().trim().isEmpty()) {
            editTextPhone.setError("Vui lòng nhập số điện thoại");
            editTextPhone.requestFocus();
            return false;
        }

        // Validate address details
        if (editTextAddressDetails.getText() == null || editTextAddressDetails.getText().toString().trim().isEmpty()) {
            editTextAddressDetails.setError("Vui lòng nhập địa chỉ chi tiết");
            editTextAddressDetails.requestFocus();
            return false;
        }

        // Validate spinners
        if (spinnerProvince.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Vui lòng chọn Tỉnh/Thành phố", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spinnerDistrict.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Vui lòng chọn Quận/Huyện", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spinnerWard.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Vui lòng chọn Phường/Xã", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void addNewAddress(String addressName, String recipientName, String phone,
                              String addressDetails, String ward, String district, String province,
                              boolean isDefault) {
        
        addressManager.addAddress(addressName, recipientName, phone, addressDetails, ward, district, province, isDefault,
                new UpdateCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            setResult(RESULT_OK);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            btnSave.setEnabled(true);
                            btnSave.setText("Lưu");
                            Toast.makeText(AddEditAddressActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void updateAddress(String addressName, String recipientName, String phone,
                              String addressDetails, String ward, String district, String province,
                              boolean isDefault) {
        
        // Create address object for update
        com.example.phoneshopapp.models.Address address = new com.example.phoneshopapp.models.Address();
        address.setAddressId(addressId);
        address.setAddressName(addressName);
        address.setRecipientName(recipientName);
        address.setPhone(phone);
        address.setAddressDetails(addressDetails);
        address.setWard(ward);
        address.setDistrict(district);
        address.setProvince(province);
        address.setDefault(isDefault);

        addressManager.updateAddress(address, new UpdateCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu");
                    Toast.makeText(AddEditAddressActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}