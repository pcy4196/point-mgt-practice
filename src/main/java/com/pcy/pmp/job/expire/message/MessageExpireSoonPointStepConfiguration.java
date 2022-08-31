package com.pcy.pmp.job.expire.message;

import com.pcy.pmp.job.listener.InputExpiredSoonPointAlarmCriteriaDateStepListener;
import com.pcy.pmp.message.Message;
import com.pcy.pmp.point.ExpiredPointSummary;
import com.pcy.pmp.point.PointRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.Map;

@Configuration
@AllArgsConstructor
public class MessageExpireSoonPointStepConfiguration {

    private final StepBuilderFactory stepBuilderFactory;
    private final InputExpiredSoonPointAlarmCriteriaDateStepListener listener;

    @Bean
    @JobScope
    public Step messageExpiredSoonPointStep(
            PlatformTransactionManager platformTransactionManager,      // TransactionManager 가져오기
            RepositoryItemReader<ExpiredPointSummary> messageExpiredSoonPointItemReader,
            ItemProcessor<ExpiredPointSummary, Message> messageExpiredSoonPointItemProcessor,
            JpaItemWriter<Message> messageExpiredSoonPointItemSoonWriter
    ) {
        return stepBuilderFactory
                .get("messageExpiredPointStep")
                .allowStartIfComplete(true)
                .transactionManager(platformTransactionManager)
                .listener(listener)
                .<ExpiredPointSummary, Message>chunk(1000)
                .reader(messageExpiredSoonPointItemReader)
                .processor(messageExpiredSoonPointItemProcessor)
                .writer(messageExpiredSoonPointItemSoonWriter)
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<ExpiredPointSummary> messageExpiredSoonPointItemReader(
        PointRepository pointRepository,
        @Value("#{T(java.time.LocalDate).parse(stepExecutionContext[alarmCriteriaDate])}")
                LocalDate alarmCriteriaDate
    ) {
        return new RepositoryItemReaderBuilder<ExpiredPointSummary>()
                .name("messageExpiredSoonPointItemReader")
                .repository(pointRepository)
                .methodName("sumBeforeCriteriaDate")
                .pageSize(1000)
                .arguments(alarmCriteriaDate)
                .sorts(Map.of("pointWallet", Sort.Direction.ASC))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<ExpiredPointSummary, Message> messageExpiredSoonPointItemProcessor(
            @Value("#{T(java.time.LocalDate).parse(stepExecutionContext[alarmCriteriaDate])}")
                LocalDate alarmCriteriaDate
    ) {
        return summary -> Message.ExpireSoonPointMessageInstance(
                summary.getUserId(), alarmCriteriaDate, summary.getAmount()
        );
    }

    @Bean
    @StepScope
    public JpaItemWriter<Message> messageExpiredSoonPointItemSoonWriter(
            EntityManagerFactory entityManagerFactory
    ) {
        JpaItemWriter<Message> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
