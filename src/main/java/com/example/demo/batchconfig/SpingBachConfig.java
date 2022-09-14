package com.example.demo.batchconfig;

import com.example.demo.entity.BankTransaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class SpingBachConfig {

    @Autowired private JobBuilderFactory jobBuilderFactory;
    @Autowired private StepBuilderFactory stepBuilderFactory;
    @Autowired private ItemReader<BankTransaction> bankTransactionItemReader;
    @Autowired private ItemWriter<BankTransaction> bankTransactionItemWriter;

    @Bean
    public Job bankJob(){
        Step step1=stepBuilderFactory.get("step-load-data")
                .<BankTransaction,BankTransaction>chunk(100)
                .reader(bankTransactionItemReader)
                .processor(copositeItemProcessor())
                .writer(bankTransactionItemWriter)
                .build();

        return jobBuilderFactory.get("bank-data-loader-job")
                .start(step1)
                .build();
    }

    @Bean
    public ItemProcessor< BankTransaction, BankTransaction> copositeItemProcessor() {
        List<ItemProcessor<BankTransaction,BankTransaction>> itemProcessors=new ArrayList<>();
        itemProcessors.add(itemProcessor1());
        itemProcessors.add(itemProcessor2());
        CompositeItemProcessor<BankTransaction,BankTransaction> compositeItemProcessor=
                new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(itemProcessors);
        return compositeItemProcessor;
    }

    @Bean
    ItemProcessor itemProcessor1(){
        return new ItemProcessorOne();
    }

    @Bean
    ItemProcessor itemProcessor2(){
        return new ItemProcessorTwo();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    public FlatFileItemReader<BankTransaction> reader(@Value("${inputFile}") Resource inputFile)
    {
        FlatFileItemReader<BankTransaction> reader = new FlatFileItemReader<BankTransaction>();
        reader.setResource(inputFile);
        reader.setLinesToSkip(1);
        reader.setLineMapper(new DefaultLineMapper() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setNames(new String[] {"id","accountID","strTransactionDate","transactionType","transactionAmount"});
                        setStrict(false);
                        setDelimiter(",");
                    }
                });
                setFieldSetMapper(new BeanWrapperFieldSetMapper<BankTransaction>() {
                    {
                        setTargetType(BankTransaction.class);
                    }
                });
            }
        });
        return reader;
    }
}
