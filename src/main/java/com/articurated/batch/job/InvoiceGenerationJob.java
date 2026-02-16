package com.articurated.batch.job;

import com.articurated.model.Order;
import com.articurated.repository.OrderRepository;
import com.articurated.service.PdfInvoiceService;
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
public class InvoiceGenerationJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PdfInvoiceService pdfInvoiceService;

    @Bean
    public Job generateInvoiceJob() {
        return jobBuilderFactory.get("generateInvoiceJob")
                .incrementer(new RunIdIncrementer())
                .flow(generateInvoiceStep())
                .end()
                .build();
    }

    @Bean
    public Step generateInvoiceStep() {
        return stepBuilderFactory.get("generateInvoiceStep")
                .<Order, Order>chunk(10)
                .reader(orderReader())
                .processor(invoiceProcessor())
                .writer(invoiceWriter())
                .build();
    }

    @Bean
    public ItemReader<Order> orderReader() {
        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("id", Sort.Direction.ASC);
        
        return new RepositoryItemReaderBuilder<Order>()
                .name("orderReader")
                .repository(orderRepository)
                .methodName("findByStatus")
                .arguments(com.articurated.model.enums.OrderStatus.SHIPPED)
                .sorts(sorts)
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<Order, Order> invoiceProcessor() {
        return order -> {
            // Check if invoice already generated (you can add a flag in Order entity)
            // For now, we'll process all shipped orders
            return order;
        };
    }

    @Bean
    public ItemWriter<Order> invoiceWriter() {
        return orders -> {
            for (Order order : orders) {
                try {
                    pdfInvoiceService.generateInvoice(order);
                    // Simulate email sending
                    pdfInvoiceService.sendInvoiceEmail(order);
                } catch (Exception e) {
                    // Log error but continue processing other orders
                    System.err.println("Error generating invoice for order " + order.getOrderNumber() + ": " + e.getMessage());
                }
            }
        };
    }
}




