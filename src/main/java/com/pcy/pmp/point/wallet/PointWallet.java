package com.pcy.pmp.point.wallet;

import com.pcy.pmp.point.IdEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigInteger;

@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PointWallet extends IdEntity {

    @Column(name = "user_id", unique = true, nullable = false)
    String userId;

    @Column(name = "amount", columnDefinition = "BIGINT")
    @Setter
    BigInteger amount;
}
