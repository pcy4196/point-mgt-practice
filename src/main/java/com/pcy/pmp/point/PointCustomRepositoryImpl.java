package com.pcy.pmp.point;

import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

public class PointCustomRepositoryImpl extends QuerydslRepositorySupport implements PointCustomRepository {

    public PointCustomRepositoryImpl() {
        super(Point.class);
    }

    /**
     * -- REF 쿼리 --
     * select
     * w.user_id,
     * sum(p.amount)
     * from point p
     * inner join point_wallet w
     * on p.point_wallet_id = w.id
     * where p.is_expired = 1
     * and p.is_used = 0
     * and p.expire_date = ‘2021-01-01’ group by p.point_wallet_id;
     */
    @Override
    public Page<ExpiredPointSummary> sumByExpiredDate(LocalDate alarmCriteriaDate, Pageable pageable) {
        JPQLQuery<ExpiredPointSummary> query = from(QPoint.point)
                .select(
                        new QExpiredPointSummary(
                                QPoint.point.pointWallet.userId,
                                QPoint.point.amount.sum().coalesce(BigInteger.ZERO)
                        )
                )
                .where(QPoint.point.expired.eq(true))
                .where(QPoint.point.used.eq(false))
                .where(QPoint.point.expireDate.eq(alarmCriteriaDate))
                .groupBy(QPoint.point.pointWallet);
        List<ExpiredPointSummary> expiredPointLists = getQuerydsl().applyPagination(pageable, query).fetch();
        long elemCnt = query.fetchCount();
        return new PageImpl<>(
                    expiredPointLists,
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
                    elemCnt
        );
    }

    // 만료예정 포인트 조회하는 QueryDSL
    @Override
    public Page<ExpiredPointSummary> sumBeforeCriteriaDate(LocalDate alarmCriteriaDate, Pageable pageable) {
        JPQLQuery<ExpiredPointSummary> query = from(QPoint.point)
                .select(
                        new QExpiredPointSummary(
                                QPoint.point.pointWallet.userId,
                                QPoint.point.amount.sum().coalesce(BigInteger.ZERO)
                        )
                )
                .where(QPoint.point.expired.eq(false))
                .where(QPoint.point.used.eq(false))
                .where(QPoint.point.expireDate.lt(alarmCriteriaDate))
                .groupBy(QPoint.point.pointWallet);
        List<ExpiredPointSummary> expiredSoonPointLists = getQuerydsl().applyPagination(pageable, query).fetch();
        long elemCnt = query.fetchCount();
        return new PageImpl<>(
                expiredSoonPointLists,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
                elemCnt
        );
    }

    ;
}

