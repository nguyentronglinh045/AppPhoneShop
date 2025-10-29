package com.example.phoneshopapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.phoneshopapp.R;
import com.example.phoneshopapp.managers.AddressManager;
import com.example.phoneshopapp.repositories.callbacks.UpdateCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

/**
 * Dialog helper for adding new address
 */
public class AddAddressDialog {
    
    public interface OnAddressAddedListener {
        void onAddressAdded();
        void onAddressCancelled();
    }
    
    private final Context context;
    private final OnAddressAddedListener listener;
    private Dialog dialog;
    
    // Views
    private TextInputEditText editTextAddressName;
    private TextInputEditText editTextRecipientName;
    private TextInputEditText editTextPhone;
    private TextInputEditText editTextAddressDetails;
    private Spinner spinnerProvince;
    private Spinner spinnerDistrict;
    private Spinner spinnerWard;
    private SwitchMaterial switchDefaultAddress;
    private MaterialButton btnCancel;
    private MaterialButton btnSave;
    
    public AddAddressDialog(Context context, OnAddressAddedListener listener) {
        this.context = context;
        this.listener = listener;
    }
    
    public void show() {
        // Inflate dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_address, null);
        
        // Initialize views
        initViews(dialogView);
        setupSpinners();
        setupClickListeners();
        
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setCancelable(true);
        
        dialog = builder.create();
        dialog.show();
        
        // Make dialog wider
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                -1
            );
        }
    }
    
    private void initViews(View dialogView) {
        editTextAddressName = dialogView.findViewById(R.id.editTextAddressName);
        editTextRecipientName = dialogView.findViewById(R.id.editTextRecipientName);
        editTextPhone = dialogView.findViewById(R.id.editTextPhone);
        editTextAddressDetails = dialogView.findViewById(R.id.editTextAddressDetails);
        spinnerProvince = dialogView.findViewById(R.id.spinnerProvince);
        spinnerDistrict = dialogView.findViewById(R.id.spinnerDistrict);
        spinnerWard = dialogView.findViewById(R.id.spinnerWard);
        switchDefaultAddress = dialogView.findViewById(R.id.switchDefaultAddress);
        btnCancel = dialogView.findViewById(R.id.btnCancel);
        btnSave = dialogView.findViewById(R.id.btnSave);
    }
    
    private void setupSpinners() {
        // Mock data for demo - in real app this would come from API
        List<String> provinces = Arrays.asList(
            "Chọn tỉnh/thành phố",
            "Hà Nội",
            "TP. Hồ Chí Minh", 
            "Đà Nẵng",
            "Cần Thơ",
            "Hải Phòng"
        );
        
        List<String> districts = Arrays.asList(
            "Chọn quận/huyện",
            "Quận 1",
            "Quận 2", 
            "Quận 3",
            "Quận Tân Bình",
            "Quận Bình Thạnh"
        );
        
        List<String> wards = Arrays.asList(
            "Chọn phường/xã",
            "Phường Bến Nghé",
            "Phường Đa Kao",
            "Phường Cô Giang", 
            "Phường Nguyễn Cư Trinh",
            "Phường Tân Định"
        );
        
        // Setup adapters
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(context,
            android.R.layout.simple_spinner_item, provinces);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);
        
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(context,
            android.R.layout.simple_spinner_item, districts);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);
        
        ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(context,
            android.R.layout.simple_spinner_item, wards);
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(wardAdapter);
    }
    
    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onAddressCancelled();
            }
        });
        
        btnSave.setOnClickListener(v -> saveAddress());
    }
    
    private void saveAddress() {
        // Validate input
        String addressName = editTextAddressName.getText().toString().trim();
        String recipientName = editTextRecipientName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String addressDetails = editTextAddressDetails.getText().toString().trim();
        
        if (addressName.isEmpty()) {
            editTextAddressName.setError("Vui lòng nhập tên địa chỉ");
            editTextAddressName.requestFocus();
            return;
        }
        
        if (recipientName.isEmpty()) {
            editTextRecipientName.setError("Vui lòng nhập tên người nhận");
            editTextRecipientName.requestFocus();
            return;
        }
        
        if (phone.isEmpty()) {
            editTextPhone.setError("Vui lòng nhập số điện thoại");
            editTextPhone.requestFocus();
            return;
        }
        
        if (addressDetails.isEmpty()) {
            editTextAddressDetails.setError("Vui lòng nhập địa chỉ chi tiết");
            editTextAddressDetails.requestFocus();
            return;
        }
        
        // Get spinner selections
        String province = spinnerProvince.getSelectedItem().toString();
        String district = spinnerDistrict.getSelectedItem().toString();
        String ward = spinnerWard.getSelectedItem().toString();
        
        if (province.equals("Chọn tỉnh/thành phố")) {
            Toast.makeText(context, "Vui lòng chọn tỉnh/thành phố", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (district.equals("Chọn quận/huyện")) {
            Toast.makeText(context, "Vui lòng chọn quận/huyện", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (ward.equals("Chọn phường/xã")) {
            Toast.makeText(context, "Vui lòng chọn phường/xã", Toast.LENGTH_SHORT).show();
            return;
        }
        
        boolean isDefault = switchDefaultAddress.isChecked();
        
        // Save address using AddressManager
        AddressManager addressManager = AddressManager.getInstance(context);
        addressManager.addAddress(
            addressName,
            recipientName,
            phone,
            addressDetails,
            ward,
            district,
            province,
            isDefault,
            new UpdateCallback() {
                @Override
                public void onSuccess() {
                    dialog.dismiss();
                    Toast.makeText(context, "Đã thêm địa chỉ thành công", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onAddressAdded();
                    }
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(context, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}