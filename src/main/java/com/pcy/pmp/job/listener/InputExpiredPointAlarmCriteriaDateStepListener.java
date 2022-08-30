package com.pcy.pmp.job.listener;


import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InputExpiredPointAlarmCriteriaDateStepListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        // today jobParameter를 가져온다.
        // today - 1 -> alarmCriteriaDate라는 이름으로 StepExecutionContext에 주입
        JobParameter todayParameter = stepExecution.getJobParameters().getParameters().get("today");
        if (todayParameter == null) {
            return;
        }
        LocalDate today = LocalDate.parse((String) todayParameter.getValue());
        ExecutionContext context = stepExecution.getExecutionContext();
        context.put("alarmCriteriaDate", today.minusDays(1).format(DateTimeFormatter.ISO_DATE));
        stepExecution.setExecutionContext(context);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
