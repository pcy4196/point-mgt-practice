package com.pcy.pmp.job.expire;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class ExpirePointJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;

    @Bean
    public Job expirePointJob(
            Step expirePointStep
    ) {
        return jobBuilderFactory.get("expirePointJob")
                .start(expirePointStep)
                .build();
    }
}
