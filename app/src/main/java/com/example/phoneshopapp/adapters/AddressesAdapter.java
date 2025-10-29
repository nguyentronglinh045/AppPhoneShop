package com.example.phoneshopapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.models.Address;
import com.google.android.material.chip.Chip;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class AddressesAdapter extends RecyclerView.Adapter<AddressesAdapter.AddressViewHolder> {

    public interface OnAddressActionListener {
        void onEditAddress(Address address);
        void onDeleteAddress(Address address);
        void onSetDefaultAddress(Address address);
    }

    private Context context;
    private List<Address> addresses;
    private OnAddressActionListener listener;

    public AddressesAdapter(Context context, List<Address> addresses, OnAddressActionListener listener) {
        this.context = context;
        this.addresses = addresses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addresses.get(position);
        holder.bind(address);
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public class AddressViewHolder extends RecyclerView.ViewHolder {
        private CardView cardAddress;
        private MaterialTextView textAddressName, textRecipientInfo, textAddressDetails;
        private Chip chipDefault;
        private ImageButton btnEdit, btnDelete, btnSetDefault;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            cardAddress = itemView.findViewById(R.id.cardAddress);
            textAddressName = itemView.findViewById(R.id.textAddressName);
            textRecipientInfo = itemView.findViewById(R.id.textRecipientInfo);
            textAddressDetails = itemView.findViewById(R.id.textAddressDetails);
            chipDefault = itemView.findViewById(R.id.chipDefault);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnSetDefault = itemView.findViewById(R.id.btnSetDefault);
        }

        public void bind(Address address) {
            textAddressName.setText(address.getAddressName());
            textRecipientInfo.setText(address.getRecipientName() + " • " + address.getPhone());
            textAddressDetails.setText(address.getFullAddress());

            // Show/hide default chip
            if (address.isDefault()) {
                chipDefault.setVisibility(View.VISIBLE);
                btnSetDefault.setVisibility(View.GONE);
            } else {
                chipDefault.setVisibility(View.GONE);
                btnSetDefault.setVisibility(View.VISIBLE);
            }

            // Click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditAddress(address);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteAddress(address);
                }
            });

            btnSetDefault.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSetDefaultAddress(address);
                }
            });

            // Card click to edit
            cardAddress.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditAddress(address);
                }
            });
        }
    }
}