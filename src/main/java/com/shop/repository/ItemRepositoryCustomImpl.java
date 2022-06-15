package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.Item;
import com.shop.entity.QItem;

import com.shop.entity.QItemImg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{ //ItemRepositoryCustom 상속받는다.

    private JPAQueryFactory queryFactory; //동적으로 쿼리 생성하기 위해 JPAQueryFactory 클래스 사용

    public ItemRepositoryCustomImpl(EntityManager em) { //JPAQueryFactory 의 생성자로 EntityManager 객체를 넣어준다.
        this.queryFactory = new JPAQueryFactory(em);
    }

    //상품 판매 조건이 전체(null)일 경우는 null 을 리턴. 결과값이 null 이면 where 절에서 해당 조건은 무시된다.
    //상품 판매 상태 조건이 null 이 아니라 판매중 or 품절 상태라면 해당 조건의 상품만 조회한다.
    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus) {

        return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
    }

    //SearchDateType 의 값에 따라서 dateTime 의 값을 이전 시간의 값으로 세팅 후 해당 시간 이후로 등록된 상품만 조회.
    //ex) SearchDateType 값이 "1m"인 경우 dateTime 의 시간을 한 달 전으로 세팅 후 최근 한 달 동안 등록된 상품만 조회하도록 조건값을 반환한다.
    private BooleanExpression regDtsAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();

        if(StringUtils.equals("all", searchDateType) || searchDateType == null) {
            return null;
        } else if (StringUtils.equals("1d", searchDateType)) {
            dateTime = dateTime.minusDays(1);
        } else if (StringUtils.equals("1w", searchDateType)) {
            dateTime = dateTime.minusWeeks(1);
        } else if (StringUtils.equals("1m", searchDateType)) {
            dateTime = dateTime.minusMonths(1);
        } else if (StringUtils.equals("6m", searchDateType)) {
            dateTime = dateTime.minusMonths(1);
        }

        return QItem.item.regTime.after(dateTime);
    }

    //SearchBy의 값에 따라 '상품명'에 검색어를 포함하고 있는 상품 또는 '상품 생성자의 아이디'에 검색어를 포함하고 있는 상품을 조회하도록 조건값을 반환한다.
    private BooleanExpression searchByLike(String searchBy, String searchQuery) {

        if(StringUtils.equals("itemNm", searchBy)) {
            return QItem.item.itemNm.like("%" + searchQuery + "%");
        } else if(StringUtils.equals("createdBy", searchBy)) {
            return QItem.item.createdBy.like("%" + searchQuery + "%");
        }

        return null;
    }

    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        //QueryFactory 를 이용해 쿼리 생성.
        QueryResults<Item> results = queryFactory
                .selectFrom(QItem.item) //상품 데이터를 조회하기 위해 QItem 의 item 을 지정
                .where(regDtsAfter(itemSearchDto.getSearchDateType()), //Where조건절. BooleanExpression 반환하는 조건문을 넣는다. ','단위로 넣어줄 경우 and 조건으로 인식한다.
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(),
                                itemSearchDto.getSearchQuery()))
                .orderBy(QItem.item.id.desc())
                .offset(pageable.getOffset()) //데이터를 가지고 올 시작 인덱스를 지정
                .limit(pageable.getPageSize()) //한번에 가지고 올 최대 개수를 지정
                .fetchResults(); //조회한 리스트 및 전체 개수를 포함하는 QueryResult를 반환. 상품 데이터 리스트 조회 및 상품 데이터 전체 개수를 조회하는 2번의 쿼리문이 실행된다.

        List<Item> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total); //조회한 데이터를 Page 클래스의 구현체인 PageImpl 객체로 반환한다.
    }

    //검색어가 null 이 아니면 상품명에 해당 검색어가 포함되는 상품을 조회하는 조건을 반환
    private BooleanExpression itemNmLike(String searchQuery) {
        return StringUtils.isEmpty(searchQuery) ? null : QItem.item.itemNm.like("%" + searchQuery + "%");
    }

    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        QueryResults<MainItemDto> results = queryFactory
                .select(
                        new QMainItemDto( //생성자에 반환할 값을 넣어준다. @QueryProjection 을 사용해 dto 로 바로 조회
                                item.id,
                                item.itemNm,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price
                        )
                )
                .from(itemImg)
                .join(itemImg.item, item) //itemImg 와 item 을 내부 조인
                .where(itemImg.repImgYn.eq("Y")) //상품 이미지의 경우 대표 상품 이미지만 불러온다.
                .where(itemNmLike(itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MainItemDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);
    }
}
