package com.pcy.pmp.point;

import com.pcy.pmp.point.wallet.PointWallet;
import lombok.*;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDate;

@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Point extends IdEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_wallet_id", nullable = false)
    PointWallet pointWallet;

    @Column(name = "amount", nullable = false, columnDefinition = "BIGINT")
    BigInteger amount;

    @Column(name = "earned_date", nullable = false)
    LocalDate earnedDate;       // 적립일자

    @Column(name = "expire_date", nullable = false)
    LocalDate expireDate;       // 만료일자

    @Column(name = "is_used", nullable = false, columnDefinition = "TINYINT", length = 1)
    boolean used;

    @Column(name = "is_expired", nullable = false, columnDefinition = "TINYINT", length = 1)
    @Setter
    boolean expired;

    public Point(
            PointWallet pointWallet,
            BigInteger amount,
            LocalDate earnedDate,
            LocalDate expireDate
    ) {
       this.pointWallet = pointWallet;
       this.amount = amount;
       this.earnedDate = earnedDate;
       this.expireDate = expireDate;
       this.used = false;
       this.expired = false;
    }

    public void expire() {
        if(!this.used) {
            this.expired = true;
        }
    }
}
