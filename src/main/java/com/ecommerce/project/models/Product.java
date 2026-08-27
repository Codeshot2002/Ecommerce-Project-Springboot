package com.ecommerce.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    @NotBlank(message = "Name is mandatory")
    @Getter
    @Setter
    private String name;

    @Column(length = 1_000)
    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    @NotNull(message = "Quantity is mandatory")
    private Long quantity = 1L;

    @Getter
    @Setter
    @NotNull(message = "Category is mandatory")
    private Long categoryId;

    @Version
    @Getter @Setter
    private Long version;
}
