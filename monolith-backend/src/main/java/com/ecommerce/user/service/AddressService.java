package com.ecommerce.user.service;

import com.ecommerce.user.dto.AddressDtos.*;
import com.ecommerce.user.entity.Address;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addresses;

    @Transactional(readOnly = true)
    public List<AddressResponse> list(Long userId) {
        return addresses.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public AddressResponse create(Long userId, AddressRequest req) {
        List<Address> existing = addresses.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        boolean makeDefault = existing.isEmpty() || Boolean.TRUE.equals(req.getIsDefault());
        if (makeDefault) {
            existing.forEach(a -> a.setIsDefault(false));
            addresses.saveAll(existing);
        }
        Address a = Address.builder()
                .userId(userId).name(req.getName()).phone(req.getPhone())
                .pincode(req.getPincode()).addressLine(req.getAddressLine())
                .city(req.getCity()).state(req.getState())
                .country(req.getCountry() == null || req.getCountry().isBlank() ? "India" : req.getCountry())
                .type(req.getType() == null || req.getType().isBlank() ? "HOME" : req.getType().toUpperCase())
                .isDefault(makeDefault)
                .build();
        return toResponse(addresses.save(a));
    }

    @Transactional
    public AddressResponse update(Long userId, Long id, AddressRequest req) {
        Address a = addresses.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));
        a.setName(req.getName());
        a.setPhone(req.getPhone());
        a.setPincode(req.getPincode());
        a.setAddressLine(req.getAddressLine());
        a.setCity(req.getCity());
        a.setState(req.getState());
        if (req.getCountry() != null && !req.getCountry().isBlank()) a.setCountry(req.getCountry());
        if (req.getType() != null && !req.getType().isBlank()) a.setType(req.getType().toUpperCase());
        if (Boolean.TRUE.equals(req.getIsDefault())) {
            addresses.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                    .forEach(other -> other.setIsDefault(false));
            a.setIsDefault(true);
        }
        return toResponse(addresses.save(a));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Address a = addresses.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));
        addresses.delete(a);
    }

    private AddressResponse toResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId()).name(a.getName()).phone(a.getPhone())
                .pincode(a.getPincode()).addressLine(a.getAddressLine())
                .city(a.getCity()).state(a.getState()).country(a.getCountry())
                .type(a.getType()).isDefault(a.getIsDefault())
                .build();
    }
}
