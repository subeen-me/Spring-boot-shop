package com.shop.repository;

import com.shop.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    //회원가입 시 중복회원이 있는지 검사하기 위한 쿼리 메소드
    Member findByEmail(String email);
}
