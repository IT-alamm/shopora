package com.ecommerce.user.controller;

import com.ecommerce.user.dto.AddressDtos.*;
import com.ecommerce.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AddressService addressService;

    private Long resolveUserId(String header) {
        if (header == null || header.isBlank() || "null".equalsIgnoreCase(header)) return 1L;
        try { return Long.parseLong(header); } catch (NumberFormatException e) { return 1L; }
    }

    @GetMapping("/health")
    public String health() { return "user-service OK"; }

    @GetMapping("/addresses")
    public List<AddressResponse> list(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return addressService.list(resolveUserId(userId));
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                  @Valid @RequestBody AddressRequest req) {
        return addressService.create(resolveUserId(userId), req);
    }

    @PutMapping("/addresses/{id}")
    public AddressResponse update(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                  @PathVariable Long id,
                                  @Valid @RequestBody AddressRequest req) {
        return addressService.update(resolveUserId(userId), id, req);
    }

    @DeleteMapping("/addresses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(value = "X-User-Id", required = false) String userId,
                       @PathVariable Long id) {
        addressService.delete(resolveUserId(userId), id);
    }
}
