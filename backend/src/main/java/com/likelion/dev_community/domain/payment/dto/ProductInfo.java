package com.likelion.dev_community.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductInfo {
    private final String id;

    private final String name;

    private final String code;

    private final Long amount;

    private final int quantity;
}
