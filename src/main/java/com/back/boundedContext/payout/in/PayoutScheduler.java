package com.back.boundedContext.payout.in;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Profile("prod")
@Component
@RequiredArgsConstructor
public class PayoutScheduler {
	private final JobOperator jobOperator;
	private final Job payoutCollectItemsAndCompletePayoutsJob;

	@Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
	public void runAt01() throws Exception {
		runCollectItemsAndCompletePayoutsBatchJob();
	}

	@Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
	public void runAt04() throws Exception {
		runCollectItemsAndCompletePayoutsBatchJob();
	}

	@Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
	public void runAt22() throws Exception {
		runCollectItemsAndCompletePayoutsBatchJob();
	}

	private void runCollectItemsAndCompletePayoutsBatchJob() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("runDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
			.toJobParameters();

		jobOperator.start(payoutCollectItemsAndCompletePayoutsJob, jobParameters);
	}
}
