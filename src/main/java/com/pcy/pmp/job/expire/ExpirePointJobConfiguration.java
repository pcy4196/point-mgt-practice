package com.pcy.pmp.job.expire;

import com.pcy.pmp.job.expire.validator.TodayJobParameterValidator;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class ExpirePointJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final TodayJobParameterValidator validator;

    @Bean
    public Job expirePointJob(
            Step expirePointStep
    ) {
        return jobBuilderFactory.get("expirePointJob")
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(expirePointStep)
                .build();
    }
}
