package com.pcy.pmp.point;

import lombok.Getter;

import java.math.BigInteger;

@Getter
public class ExpiredPointSummary {
    String userId;      // userId
    BigInteger amount;  // 만료금액
}
