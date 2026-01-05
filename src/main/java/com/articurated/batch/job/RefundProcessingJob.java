package com.articurated.batch.job;

import com.articurated.model.Return;
import com.articurated.repository.ReturnRepository;
import com.articurated.service.PaymentGatewayService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RefundProcessingJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ReturnRepository returnRepository;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Bean
    public Job processRefundJob() {
        return jobBuilderFactory.get("processRefundJob")
                .incrementer(new RunIdIncrementer())
                .flow(processRefundStep())
                .end()
                .build();
    }

    @Bean
    public Step processRefundStep() {
        return stepBuilderFactory.get("processRefundStep")
                .<Return, Return>chunk(10)
                .reader(returnReader())
                .processor(refundProcessor())
                .writer(refundWriter())
                .build();
    }

    @Bean
    public ItemReader<Return> returnReader() {
        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("id", Sort.Direction.ASC);
        
        return new RepositoryItemReaderBuilder<Return>()
                .name("returnReader")
                .repository(returnRepository)
                .methodName("findByStatus")
                .arguments(com.articurated.model.enums.ReturnStatus.COMPLETED)
                .sorts(sorts)
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<Return, Return> refundProcessor() {
        return returnRequest -> {
            // Check if refund already processed (you can add a flag in Return entity)
            // For now, we'll process all completed returns
            return returnRequest;
        };
    }

    @Bean
    public ItemWriter<Return> refundWriter() {
        return returns -> {
            for (Return returnRequest : returns) {
                try {
                    paymentGatewayService.processRefund(returnRequest);
                } catch (Exception e) {
                    // Log error but continue processing other returns
                    System.err.println("Error processing refund for return " + returnRequest.getReturnNumber() + ": " + e.getMessage());
                }
            }
        };
    }
}



