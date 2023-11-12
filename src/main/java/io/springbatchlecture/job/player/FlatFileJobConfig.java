package io.springbatchlecture.job.player;

import io.springbatchlecture.core.service.PlayerSalaryService;
import io.springbatchlecture.dto.PlayerDto;
import io.springbatchlecture.dto.PlayerSalaryDto;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemProcessorAdapter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
@AllArgsConstructor
public class FlatFileJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job flatFileJob(Step flatFileStep) {
        return jobBuilderFactory.get("flatFileJob")
                .incrementer(new RunIdIncrementer())
                .start(flatFileStep)
                .build();
    }

    @JobScope
    @Bean
    public Step flatFileStep(FlatFileItemReader<PlayerDto> playerFileItemReader,
                            ItemProcessorAdapter<PlayerDto, PlayerSalaryDto> playerSalaryItemProcessorAdapter,
                            FlatFileItemWriter<PlayerSalaryDto> playerFileItemWriter) {
        return stepBuilderFactory.get("flatFileStep")
                .<PlayerDto, PlayerSalaryDto>chunk(5)
                .reader(playerFileItemReader)
                /*
                .processor(new ItemProcessor<PlayerDto, PlayerSalaryDto>() {
                    @Override
                    public PlayerSalaryDto process(PlayerDto item) throws Exception {
                        return playerSalaryService.calcSalary(item);
                    }
                })
                */
                /*.processor(playerSalaryDtoItemProcessor)*/
                .processor(playerSalaryItemProcessorAdapter)
                /*
                .writer(new ItemWriter<>() {
                    @Override
                    public void write(List<? extends PlayerSalaryDto> items) throws Exception {
                        items.forEach(System.out::println);
                    }
                })
                */
                .writer(playerFileItemWriter)
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemWriter<PlayerSalaryDto> playerFileItemWriter() throws IOException {
        System.out.println("FlatFileJobConfig.playerFileItemWriter");

        BeanWrapperFieldExtractor<PlayerSalaryDto> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"ID", "firstName", "lastName", "salary"});
        fieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<PlayerSalaryDto> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter("\t");
        lineAggregator.setFieldExtractor(fieldExtractor);

        // 기존의 파일을 덮어쓴다.
        new File("player-salary-list.txt").createNewFile();
        FileSystemResource resource = new FileSystemResource("player-salary-list.txt");

        return new FlatFileItemWriterBuilder<PlayerSalaryDto>()
                .name("playerFileItemWriter")
                .resource(resource)
                .lineAggregator(lineAggregator)
                .build();
    }

    @StepScope
    @Bean
    public ItemProcessorAdapter<PlayerDto, PlayerSalaryDto> playerSalaryItemProcessorAdapter (PlayerSalaryService playerSalaryService) {
        System.out.println("FlatFileJobConfig.playerSalaryItemProcessorAdapter");
        ItemProcessorAdapter<PlayerDto, PlayerSalaryDto> adapter = new ItemProcessorAdapter<>();
        adapter.setTargetObject(playerSalaryService);
        adapter.setTargetMethod("calcSalary");
        return adapter;
    }
    @StepScope
    @Bean
    public ItemProcessor<PlayerDto, PlayerSalaryDto> playerSalaryDtoItemProcessor (PlayerSalaryService playerSalaryService) {
        return new ItemProcessor<PlayerDto, PlayerSalaryDto>() {
            @Override
            public PlayerSalaryDto process(PlayerDto item) throws Exception {
                return playerSalaryService.calcSalary(item);
            }
        };
    }

    @StepScope
    @Bean
    public FlatFileItemReader<PlayerDto> playerFileItemReader() {
        System.out.println("FlatFileJobConfig.playerFileItemReader");
        return new FlatFileItemReaderBuilder<PlayerDto>()
                .name("playerFileItemReader")
                .lineTokenizer(new DelimitedLineTokenizer())
                .linesToSkip(1)
                .fieldSetMapper(new PlayerFieldSetMapper())
                .resource(new FileSystemResource("player-list.txt"))
                .build();

    }
}
