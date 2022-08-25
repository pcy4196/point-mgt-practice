package com.pcy.pmp.job.reservation;

import com.pcy.pmp.point.Point;
import com.pcy.pmp.point.PointRepository;
import com.pcy.pmp.point.reservation.PointReservation;
import com.pcy.pmp.point.reservation.PointReservationRepository;
import com.pcy.pmp.point.wallet.PointWallet;
import com.pcy.pmp.point.wallet.PointWalletRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.Map;

@Configuration
@AllArgsConstructor
public class executePointReservationStepConfiguration {

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @JobScope
    public Step executePointReservationStep(
            PlatformTransactionManager platformTransactionManager,      // TransactionManager 가져오기
            JpaPagingItemReader<PointReservation> executePointReservationItemReader,
            ItemProcessor<PointReservation, Pair<PointReservation, Point>> executePointReservationItemProcessor,
            ItemWriter<Pair<PointReservation, Point>> executePointReservationItemWriter
    ) {
        return stepBuilderFactory.get("executePointReservationStep")
                .transactionManager(platformTransactionManager)
                .<PointReservation, Pair<PointReservation, Point>>chunk(1000)
                .reader(executePointReservationItemReader)
                .processor(executePointReservationItemProcessor)
                .writer(executePointReservationItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<PointReservation> executePointReservationItemReader(
            EntityManagerFactory entityManagerFactory,
            // JobParameter를 가져와서 LocalDate로 converting함
            @Value("#{T(java.time.LocalDate).parse(jobParameters[today])}") LocalDate today
    ) {
        return new JpaPagingItemReaderBuilder<PointReservation>()
                .name("executePointReservationItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select pr from PointReservation pr where pr.earnedDate = :today and pr.executed = false")
                .parameterValues(Map.of("today", today))
                .pageSize(1000)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<PointReservation, Pair<PointReservation, Point>> executePointReservationItemProcessor() {
        return reservation -> {
            reservation.setExecuted(true);
            Point earnedPoint = new Point(
                    reservation.getPointWallet(),
                    reservation.getAmount(),
                    reservation.getEarnedDate(),
                    reservation.getExpiredDate()
            );
            PointWallet pointWallet = earnedPoint.getPointWallet();
            pointWallet.setAmount(pointWallet.getAmount().add(earnedPoint.getAmount()));
            return Pair.of(reservation, earnedPoint);
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Pair<PointReservation, Point>> executePointReservationItemWriter(
            PointReservationRepository pointReservationRepository,
            PointRepository pointRepository,
            PointWalletRepository pointWalletRepository
    ) {
        return reservationPointsPairs -> {
            for (Pair<PointReservation, Point> pair : reservationPointsPairs) {
                PointReservation reservation = pair.getFirst();
                pointReservationRepository.save(reservation);
                Point point = pair.getSecond();
                pointRepository.save(point);
                pointWalletRepository.save(point.getPointWallet());
            }
        };
    }
}
