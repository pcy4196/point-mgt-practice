package com.pcy.pmp.job.expire.message;

import com.pcy.pmp.job.expire.validator.TodayJobParameterValidator;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 사용하지 않은 포인트 중에 만료 예정(7일이내)인 포인트들을 메시지로 알리도록 message 테이블에 저장하는 Job
 */
@Configuration
@AllArgsConstructor
public class MessageExpireSoonPointJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final TodayJobParameterValidator validator;

    @Bean
    public Job messageExpireSoonPointJob (
            Step messageExpiredSoonPointStep
    ) {
        return jobBuilderFactory
                .get("messageExpireSoonPointJob")
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(messageExpiredSoonPointStep)
                .build();
    }

}
