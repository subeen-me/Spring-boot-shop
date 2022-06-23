package com.shop.controller;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping(value = "/cart")
    public @ResponseBody
    ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto, BindingResult bindingResult, Principal principal) {

        //장바구니에 담을 상품 정보를 받는 cartItemDto 객체에 데이터 바인딩 시 에러가 있는지 검사
        if(bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for(FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }
        String email = principal.getName(); //현재 로그인한 회원의 이메일 정보를 변수에 저장
        Long cartItemId;

        //화면으로부터 넘어온 장바구니에 담을 상품 정보와 현재 로그인한 회원의 이메일 정보를 이용하여 장바구니에 상품을 담는 로직을 호출
        try {
            cartItemId = cartService.addCart(cartItemDto, email);
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        //결과값으로 생성된 장바구니 상품 아이디와 요청이 성공하였다는 HTTP 응담 상태 코드를 반환
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @GetMapping(value = "/cart")
    public String orderHist(Principal principal, Model model) {
        //현재 로그인한 사용자의 이메일 정보를 이용해 장바구니에 담긴 상품 정보를 조회
        List<CartDetailDto> cartDetailList = cartService.getCartList(principal.getName());
        model.addAttribute("cartItems", cartDetailList); //장바구니 상품 정보 뷰에 전달
        return "cart/cartList";
    }

    @PatchMapping(value = "/cartItem/{cartItemId}") //장바구니 수량만 업데이트하는 메소드
    public @ResponseBody ResponseEntity updateCartItem (@PathVariable("cartItemId") Long cartItemId, int count, Principal principal) {

        if(count <= 0) { //0개 이하로 업데이트 요청 시 에러 메세지
            return new ResponseEntity<String>
                    ("최소 1개 이상 담아주세요.", HttpStatus.BAD_REQUEST);
        } else if(!cartService.validateCartItem(cartItemId, principal.getName())) { //수정 권한 체크
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.updateCartItemCount(cartItemId, count); //장바구니 상품 개수 업데이트
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @DeleteMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity deleteCartItem(@PathVariable("cartItemId") Long cartItemId, Principal principal) {

        if(!cartService.validateCartItem(cartItemId, principal.getName())) { //수정 권한 체크
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.deleteCartItem(cartItemId);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @PostMapping(value = "/cart/orders")
    public @ResponseBody ResponseEntity orderCartItem(@RequestBody CartOrderDto cartOrderDto, Principal principal) {

        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        if(cartOrderDtoList == null || cartOrderDtoList.size() == 0) { //주문할 상품을 선택하지 않았는지 체크
            return new ResponseEntity<String>("주문할 상품을 선택해주세요.", HttpStatus.FORBIDDEN);
        }

        for (CartOrderDto cartOrder : cartOrderDtoList) { //주문 권한을 체크
            if(!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())) {
                return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
        }

        //주문 로직 호출 결과 생성된 주문 번호를 반환받는다
        Long orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName());
        return new ResponseEntity<Long>(orderId, HttpStatus.OK); //생성된 주문 번호와 요청 성공 응답코드 반환
    }
}
