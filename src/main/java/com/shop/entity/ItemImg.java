package com.shop.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "item_img")
@Data
public class ItemImg extends BaseEntity{

    @Id
    @Column(name = "item_img_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String imgName; //이미지 파일명

    private String oriImgName; //원본 이미지 파일명

    private String imgUrl; //이미지 조회 경로

    private String repImgYn; //대표 이미지 여부

    @ManyToOne(fetch = FetchType.LAZY) //지연 로딩, 매핑된 상품 엔티티 정보가 필요한 경우 데이터 조회
    @JoinColumn(name = "item_id")
    private Item item;

    public void updateItemImg(String oriImgName, String imgName, String imgUrl) {
        this.oriImgName = oriImgName;
        this.imgName = imgName;
        this.imgUrl = imgUrl;
    }
}
