package com.pcy.pmp.job.expire;

import com.pcy.pmp.BatchTestSupport;
import com.pcy.pmp.point.Point;
import com.pcy.pmp.point.PointRepository;
import com.pcy.pmp.point.wallet.PointWallet;
import com.pcy.pmp.point.wallet.PointWalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class ExpirePointJobConfigurationTest extends BatchTestSupport {

    @Autowired
    Job expirePointJob;

    @Autowired
    PointWalletRepository pointWalletRepository;

    @Autowired
    PointRepository pointRepository;

    @Test
    void expirePointJob() throws Exception {
        // given
        LocalDate earnDate = LocalDate.of(2022, 8, 1);
        LocalDate expireDate = LocalDate.of(2022, 8, 3);

        PointWallet pointWallet = pointWalletRepository.save(
                new PointWallet(
                        "user1",
                        BigInteger.valueOf(5000)
                )
        );
        pointRepository.save(new Point(pointWallet, BigInteger.valueOf(1000), earnDate, expireDate));
        pointRepository.save(new Point(pointWallet, BigInteger.valueOf(1000), earnDate, expireDate));
        pointRepository.save(new Point(pointWallet, BigInteger.valueOf(1000), earnDate, expireDate));
        // when
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("today", "2022-08-04")
                .toJobParameters();

        JobExecution jobExecution = launchJob(expirePointJob, jobParameters);
        // then
        // 1.job 수행여부 확인
        then(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        // 2.포인트 만료여부 확인
        List<Point> pointList = pointRepository.findAll();
        then(pointList.stream().filter(Point::isExpired)).hasSize(3);
        // 3.포인트 지갑 완료
        PointWallet changePointWallet = pointWalletRepository.findById(pointWallet.getId()).orElseGet(null);
        then(changePointWallet).isNotNull();
        then(changePointWallet.getAmount()).isEqualByComparingTo(BigInteger.valueOf(2000));
    }

}