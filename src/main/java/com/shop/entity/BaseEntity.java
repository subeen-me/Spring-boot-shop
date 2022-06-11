package com.shop.entity;

import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

@EntityListeners(value = {AuditingEntityListener.class})
@MappedSuperclass
@Getter
public abstract class BaseEntity extends BaseTimeEntity{

    /**
     * BaseTimeEntity 를 상속받는 BaseEntity
     * 등록일, 수정일, 등록자, 수정자를 모두 갖는 엔티티는 이 엔티티를 상속받으면 된다.
     */

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String modifiedBy;
}
