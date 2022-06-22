package com.shop.entity;


import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "cart")
@Data
@ToString
public class Cart extends BaseEntity {

    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY) //회원 엔티티와 일대일로 매핑, 장바구니-회원은 일대일 단방향 매핑(장바구니만 회원 엔티티를 참조)
    @JoinColumn(name = "member_id") //매핑할 외래키 이름을 직접 설정
    private Member member;

    //회원 1명당 1개의 장바구니. 처음 장바구니에 상품을 담을 때에는 해당 회원의 장바구니를 생성
    //회원 엔티티를 파라미터로 받아서 장바구니 엔티티를 생성하는 로직
    public static Cart createCart(Member member) {
        Cart cart = new Cart();
        cart.setMember(member);
        return cart;
    }
}
