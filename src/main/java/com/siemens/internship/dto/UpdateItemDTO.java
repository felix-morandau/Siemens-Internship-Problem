package com.siemens.internship.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateItemDTO {
    private String name;

    private String description;

    private String status;

    @Pattern(
            regexp = "^[a-zA-Z0-9.#$%&'*+=?^_`{|}~-]+@[a-zA-Z0-9-]+\\.([a-zA-Z0-9-]{2,})+$",
            message = "Invalid email format"
    )
    private String email;
}
