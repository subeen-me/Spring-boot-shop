package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 장바구니 페이지에서 주문할 상품 데이터를 전달할 dto
 */
@Getter @Setter
public class CartOrderDto {

    private Long cartItemId;

    private List<CartOrderDto> cartOrderDtoList;
}
