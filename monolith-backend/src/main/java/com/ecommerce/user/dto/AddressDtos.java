package com.ecommerce.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class AddressDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AddressRequest {
        @NotBlank private String name;
        @NotBlank @Pattern(regexp = "\\d{10}", message = "phone must be 10 digits") private String phone;
        @NotBlank @Pattern(regexp = "\\d{6}", message = "pincode must be 6 digits") private String pincode;
        @NotBlank private String addressLine;
        @NotBlank private String city;
        @NotBlank private String state;
        private String country;
        private String type; // HOME, WORK
        private Boolean isDefault;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AddressResponse {
        private Long id;
        private String name;
        private String phone;
        private String pincode;
        private String addressLine;
        private String city;
        private String state;
        private String country;
        private String type;
        private Boolean isDefault;
    }
}
