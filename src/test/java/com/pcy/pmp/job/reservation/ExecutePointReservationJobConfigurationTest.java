package com.pcy.pmp.job.reservation;

import com.pcy.pmp.BatchTestSupport;
import com.pcy.pmp.point.Point;
import com.pcy.pmp.point.PointRepository;
import com.pcy.pmp.point.reservation.PointReservation;
import com.pcy.pmp.point.reservation.PointReservationRepository;
import com.pcy.pmp.point.wallet.PointWallet;
import com.pcy.pmp.point.wallet.PointWalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class ExecutePointReservationJobConfigurationTest extends BatchTestSupport {

    @Autowired
    PointWalletRepository pointWalletRepository;

    @Autowired
    PointReservationRepository pointReservationRepository;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    Job executePointReservationJob;

    @Test
    void executePointReservationJob() throws Exception {
        // given
        // point reservation 작성
        LocalDate earnDate = LocalDate.of(2022, 8, 5);

        PointWallet pointWallet = pointWalletRepository.save(
                new PointWallet(
                        "user1",
                        BigInteger.valueOf(3000)
                )
        );

        pointReservationRepository.save(
                new PointReservation(
                        pointWallet,
                        BigInteger.valueOf(1000),
                        earnDate,
                        10
                )
        );

        // when
        // executePointREservationJob을 수행
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("today", "2022-08-05")
                .toJobParameters();

        JobExecution jobExecution = launchJob(executePointReservationJob, jobParameters);

        // then
        // 1. point reservation 수행 완료
        then(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        // 2. point 적립 발생여부
        List<PointReservation> reservations = pointReservationRepository.findAll();
        then(reservations).hasSize(1);              // data 사이즈 체크
        then(reservations.get(0).isExecuted()).isTrue();       // 예약 실행여부 체크

        List<Point> points = pointRepository.findAll();
        then(points).hasSize(1);
        then(points.get(0).getAmount()).isEqualByComparingTo(BigInteger.valueOf(1000));       // 포인트 발생 금액
        then(points.get(0).getEarnedDate()).isEqualTo(earnDate);                              // 포인트 발생일자
        then(points.get(0).getExpireDate()).isEqualTo(earnDate.plusDays(10));      // 포인트 만료일자 확인

        // 3. point wallet의 잔액이 증가
        List<PointWallet> pointWallets = pointWalletRepository.findAll();
        then(pointWallets).hasSize(1);              // data 사이즈 체크
        then(pointWallets.get(0).getAmount()).isEqualByComparingTo(BigInteger.valueOf(4000));  // 포인트 발생 금액
    }
}