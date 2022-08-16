package com.pcy.pmp.point.reservation;

import com.pcy.pmp.point.IdEntity;
import com.pcy.pmp.point.wallet.PointWallet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDate;

@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PointReservation extends IdEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_wallet_id", nullable = false)
    PointWallet pointWallet;

    @Column(name = "amount", nullable = false, columnDefinition = "BIGINT")
    BigInteger amount;                      // 적립금액

    @Column(name = "earned_date", nullable = false)
    LocalDate earnedDate;                   // 적립일자

    @Column(name = "available_days", nullable = false)
    int availableDays;                      // 유효일

    @Column(name = "is_executed", columnDefinition = "TINYINT", length = 1, nullable = false)
    boolean executed;                       // 실행여부

    // 생성자
    public PointReservation(
            PointWallet pointWallet,
            BigInteger amount,
            LocalDate earnedDate,
            int availableDays
    ) {
        this.pointWallet = pointWallet;
        this.amount = amount;
        this.earnedDate = earnedDate;
        this.availableDays = availableDays;
        this.executed = false;
    }

    public void executed() {
        this.executed = true;
    }

    // 만료일자 구하는 메서드(Method)
    public LocalDate getExpiredDate() {
        return this.earnedDate.plusDays(this.availableDays);
    }
}
