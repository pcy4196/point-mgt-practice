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

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.BDDAssertions.then;

class MessageExpireSoonPointJobConfigurationTest extends BatchTestSupport {
    @Autowired
    Job messageExpireSoonPointJob;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    PointWalletRepository pointWalletRepository;

    @Autowired
    PointRepository pointRepository;

    @Test
    void messageToExpirePointJob() throws Exception {
        // given
        LocalDate earnDate = LocalDate.of(2022, 1, 1);
        LocalDate expireDate = LocalDate.of(2022, 8, 10);
        LocalDate notExpireDate = LocalDate.of(2022, 8, 20);
        PointWallet pointWallet1 = pointWalletRepository.save(
                new PointWallet("user1", BigInteger.valueOf(3000))
        );
        PointWallet pointWallet2 = pointWalletRepository.save(
                new PointWallet("user2", BigInteger.valueOf(2000))
        );
        // 1. 만료예정 포인트
        pointRepository.save(new Point(pointWallet2, BigInteger.valueOf(1000), earnDate, notExpireDate));
        pointRepository.save(new Point(pointWallet2, BigInteger.valueOf(1000), earnDate, notExpireDate));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, notExpireDate));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, notExpireDate));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, notExpireDate));
        // 2. 만료 포인트
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        pointRepository.save(new Point(pointWallet1, BigInteger.valueOf(1000), earnDate, expireDate, false, true));
        // when
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("today", "2022-08-15")
                .toJobParameters();
        JobExecution execution = launchJob(messageExpireSoonPointJob, jobParameters);
        // then
        then(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        List<Message> messages = messageRepository.findAll();
        then(messages).hasSize(2);
        Message message1 = messages.stream().filter(item -> item.getUserId().equals("user1")).findFirst().orElseGet(null);
        then(message1).isNotNull();
        then(message1.getTitle()).isEqualTo("3000 포인트 만료 예정");
        then(message1.getContent()).isEqualTo("2022-08-22 까지 3000 포인트가 만료 예정입니다.");
        Message message2 = messages.stream().filter(item -> item.getUserId().equals("user2")).findFirst().orElseGet(null);
        then(message2).isNotNull();
        then(message2.getTitle()).isEqualTo("2000 포인트 만료 예정");
        then(message2.getContent()).isEqualTo("2022-08-22 까지 2000 포인트가 만료 예정입니다.");
    }
}