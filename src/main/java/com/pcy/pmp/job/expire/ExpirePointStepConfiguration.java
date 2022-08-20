package com.pcy.pmp.job.expire;

import com.pcy.pmp.point.Point;
import com.pcy.pmp.point.PointRepository;
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
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.Map;

@Configuration
@AllArgsConstructor
public class ExpirePointStepConfiguration {

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @JobScope
    public Step expirePointStep(
            PlatformTransactionManager platformTransactionManager,      // TransactionManager 가져오기
            JpaPagingItemReader<Point> expirePointItemReader,           // ItemReader 가져오기
            ItemProcessor<Point, Point> expirePointItemProcessor,       // ItemProcessor 가져오기
            ItemWriter<Point> expirePointItemWriter                     // ItemWriter 가져오기
    ) {
        return stepBuilderFactory
                .get("expirePointStep")                 // Step 이름
                .allowStartIfComplete(true)             // Step 중복 실행 가능
                .transactionManager(platformTransactionManager)
                .<Point, Point> chunk(1000) // chunk 사이즈
                .reader(expirePointItemReader)
                .processor(expirePointItemProcessor)
                .writer(expirePointItemWriter)
                .build();
    }

    @Bean
    @StepScope  // Step이 아래의 ItemReader를 Lazy하게 생성함
    public JpaPagingItemReader<Point> expirePointItemReader(
            EntityManagerFactory entityManagerFactory,
            // JobParameter를 가져와서 LocalDate로 converting함
            @Value("#{T(java.time.LocalDate).parse(jobParameters[today])}") LocalDate today
    ) {
        return new JpaPagingItemReaderBuilder<Point>()  // JpaPagingItemReader 생성
                .name("expirePointItemReader")
                .entityManagerFactory(entityManagerFactory)
                // JPQL 사용
                .queryString("select p from Point p where p.expireDate < :today and used = false and expired = false")
                .parameterValues(Map.of("today", today))    // :today에 파라미터값을 넣어줌
                .pageSize(1000)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Point, Point> expirePointItemProcessor() {
        return point -> {               // Point를 받아와서 수정하고 Point를 반환함
            point.setExpired(true);     // @Setter 추가 필요
            PointWallet wallet = point.getPointWallet();
            wallet.setAmount(wallet.getAmount().subtract(point.getAmount()));   // Point 안에 있는 PointWallet의 잔액을 차감함
            return point;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Point> expirePointItemWriter(
            PointRepository pointRepository,
            PointWalletRepository pointWalletRepository
    ) {
        return points -> {
            for (Point point : points) {
                if (point.isExpired()) {
                    pointRepository.save(point);    // Processor에서 수정한 Point와 PointWallet을 UPDATE 처리
                    pointWalletRepository.save(point.getPointWallet());
                }
            }
        };
    }
}
