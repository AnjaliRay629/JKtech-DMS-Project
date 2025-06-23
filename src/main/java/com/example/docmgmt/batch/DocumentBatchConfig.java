package com.example.docmgmt.batch;

import com.example.docmgmt.model.Document;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
public class DocumentBatchConfig {

    private static final Logger log = LoggerFactory.getLogger(DocumentBatchConfig.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    public FlatFileItemReader<Document> reader() {
        return new FlatFileItemReaderBuilder<Document>()
                .name("documentReader")
                .resource(new ClassPathResource("documents.csv"))
                .delimited()
                .names("id", "title", "content", "author", "type")
                .targetType(Document.class)
                .build();
    }

    @Bean
    public DocumentItemProcessor processor() {
        return new DocumentItemProcessor();
    }

    @Bean
    public JpaItemWriter<Document> writer(EntityManagerFactory emf) {
        JpaItemWriter<Document> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                     FlatFileItemReader<Document> reader, DocumentItemProcessor processor,
                     JpaItemWriter<Document> writer) {
        return new StepBuilder("documentStep", jobRepository)
                .<Document, Document>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job importDocumentJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("importDocumentJob", jobRepository)
                .start(step)
                .build();
    }

    public class DocumentItemProcessor implements ItemProcessor<Document, Document> {

        @Override
        public Document process(Document document) {
            try {
                // Cache document in Redis with key "doc:<id>"
                redisTemplate.opsForValue().set("doc:" + document.getId(), document);
                log.debug("Cached document in Redis: doc:{}", document.getId());
            } catch (RedisConnectionFailureException e) {
                log.warn("Redis unavailable, skipping cache for document {}: {}", document.getId(), e.getMessage());
                // Continue processing without caching
            }
            return document; // Pass document to writer unchanged
        }
    }
}