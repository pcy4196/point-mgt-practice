package com.pcy.pmp.point;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.math.BigInteger;

@Getter
public class ExpiredPointSummary {
    String userId;      // userId
    BigInteger amount;  // 만료금액

    @QueryProjection
    public ExpiredPointSummary(String userId, BigInteger amount) {
        this.userId = userId;
        this.amount = amount;
    }
}
