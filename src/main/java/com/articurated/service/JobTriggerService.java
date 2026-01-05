package com.articurated.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JobTriggerService {

    private final JobLauncher jobLauncher;
    private final Job generateInvoiceJob;
    private final Job processRefundJob;

    public JobTriggerService(JobLauncher jobLauncher, 
                            @Qualifier("generateInvoiceJob") Job generateInvoiceJob,
                            @Qualifier("processRefundJob") Job processRefundJob) {
        this.jobLauncher = jobLauncher;
        this.generateInvoiceJob = generateInvoiceJob;
        this.processRefundJob = processRefundJob;
    }

    public void triggerInvoiceGeneration(Long orderId) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("orderId", orderId)
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(generateInvoiceJob, jobParameters);
        } catch (Exception e) {
            System.err.println("Error triggering invoice generation job: " + e.getMessage());
        }
    }

    public void triggerRefundProcessing(Long returnId) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("returnId", returnId)
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(processRefundJob, jobParameters);
        } catch (Exception e) {
            System.err.println("Error triggering refund processing job: " + e.getMessage());
        }
    }
}

