package com.shop.entity;


import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "cart")
@Data
@ToString
public class Cart {

    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne //회원 엔티티와 일대일로 매핑, 장바구니-회원은 일대일 단방향 매핑(장바구니만 회원 엔티티를 참조)
    @JoinColumn(name = "member_id") //매핑할 외래키 이름을 직접 설정
    private Member member;
}
