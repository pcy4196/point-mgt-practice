package com.pcy.pmp.job.expire.message;

import com.pcy.pmp.BatchTestSupport;
import com.pcy.pmp.message.Message;
import com.pcy.pmp.message.MessageRepository;
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

class MessageExpiredPointJobConfigurationTest extends BatchTestSupport {

    @Autowired
    Job messageExpiredPointJob;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    PointWalletRepository pointWalletRepository;

    @Autowired
    PointRepository pointRepository;

    @Test
    void messageExpiredPointJob() throws Exception {
        // given
        // 1. 포인트 지갑 생성
        // 2. 오늘(2022-08-30) 만료시킨 포인트 적립 내역을 생성(expireData = 어제)
        LocalDate earnDate = LocalDate.of(2022, 1, 1);
        LocalDate expireDate = LocalDate.of(2022, 8, 29);
        LocalDate notExpireDate = LocalDate.of(2025, 12, 31);
        PointWallet pointWallet1 = pointWalletRepository.save(
                new PointWallet("user1", BigInteger.valueOf(3000))
        );
        PointWallet pointWallet2 = pointWalletRepository.save(
                new PointWallet("user2", BigInteger.ZERO)
        );
        // 1) 만료된 포인트
        pointRepository.save(new Point(pointWallet2, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        pointRepository.save(new Point(pointWallet2, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        // 2) 만료되지 않은 포인트
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, notExpireDate));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, notExpireDate));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, notExpireDate));

        // when
        // messageExpiredPointJob 수행
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("today", "2022-08-30")
                .toJobParameters();
        JobExecution jobExecution = launchJob(messageExpiredPointJob, jobParameters);


        // then
        // 1. 아래와 같은 메서지 생성여부 확인
        // 3000 포인트 만료
        // 2021-08-30 기준 3000 포인트가 만료되었습니다.
        // user1 : 3000 포인트 만료 메시지
        // user2 : 2000 포인트 만료 메시지
        then(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        List<Message> messages = messageRepository.findAll();
        then(messages).hasSize(2);          // 메시지 2건
        Message message1 = messages.stream().filter(i -> "user1".equals(i.getUserId())).findFirst().orElseGet(null);
        then(message1).isNotNull();
        then(message1.getTitle()).isEqualTo("3000 포인트 만료");
        then(message1.getContent()).isEqualTo("2022-08-30 기준 3000 포인트가 만료되었습니다.");
        Message message2 = messages.stream().filter(i -> "user2".equals(i.getUserId())).findFirst().orElseGet(null);
        then(message2).isNotNull();
        then(message2.getTitle()).isEqualTo("2000 포인트 만료");
        then(message2.getContent()).isEqualTo("2022-08-30 기준 2000 포인트가 만료되었습니다.");
    }
}