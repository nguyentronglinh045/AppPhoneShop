package com.example.phoneshopapp.repositories;

import com.example.phoneshopapp.models.Address;
import com.example.phoneshopapp.repositories.callbacks.AddressesCallback;
import com.example.phoneshopapp.repositories.callbacks.AddressCallback;
import com.example.phoneshopapp.repositories.callbacks.UpdateCallback;

public interface AddressRepository {
    
    /**
     * Get all addresses for a specific user
     * @param userId User ID to get addresses for
     * @param callback Callback for success/error handling
     */
    void getUserAddresses(String userId, AddressesCallback callback);
    
    /**
     * Add a new address
     * @param address Address object to add
     * @param callback Callback for success/error handling
     */
    void addAddress(Address address, UpdateCallback callback);
    
    /**
     * Update an existing address
     * @param address Address object to update
     * @param callback Callback for success/error handling
     */
    void updateAddress(Address address, UpdateCallback callback);
    
    /**
     * Delete an address
     * @param addressId Address ID to delete
     * @param callback Callback for success/error handling
     */
    void deleteAddress(String addressId, UpdateCallback callback);
    
    /**
     * Set an address as default for a user
     * @param userId User ID
     * @param addressId Address ID to set as default
     * @param callback Callback for success/error handling
     */
    void setDefaultAddress(String userId, String addressId, UpdateCallback callback);
    
    /**
     * Get the default address for a user
     * @param userId User ID
     * @param callback Callback for success/error handling
     */
    void getDefaultAddress(String userId, AddressCallback callback);
}