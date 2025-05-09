package com.siemens.internship.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateItemDTO {
    @NotNull(message = "Item name required")
    private String name;

    private String description;

    @NotNull(message = "Item status required")
    private String status;

    @NotNull(message = "Email required")
    @Pattern(
            regexp = "^[a-zA-Z0-9.#$%&'*+-=?^_`{|}~]+@[a-zA-Z0-9-]+\\.([a-zA-Z0-9-]{2,})+$",
            message = "Invalid email format"
    )
    private String email;
}
