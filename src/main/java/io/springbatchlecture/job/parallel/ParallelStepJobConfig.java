package io.springbatchlecture.job.parallel;


import io.springbatchlecture.dto.AmountDto;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

/*
* 단일 프로세스 멀티 쓰레드에서 Flow를 사용해 스텝을 동시에 실행한다.
* */
@Configuration
@AllArgsConstructor
public class ParallelStepJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job paralleJob(Flow splitFlow) {
        return jobBuilderFactory.get("paralleJob")
                .incrementer(new RunIdIncrementer())
                .start(splitFlow)
                .build()
                .build();
    }

    @Bean
    public Flow splitFlow(TaskExecutor taskExecutor,
                          Flow flowAmountFileStep,
                          Flow flowAnotherStep) {
        return new FlowBuilder<SimpleFlow>("splitFlow")
                .split(taskExecutor)
                .add(flowAmountFileStep, flowAnotherStep)
                .build();
    }

    @Bean
    public Flow flowAmountFileStep(Step amoutFileStep) {
        return new FlowBuilder<SimpleFlow>("flowAmountFileStep")
                .start(amoutFileStep)
                .end();
    }

    @Bean
    public Step amoutFileStep(FlatFileItemReader<AmountDto> amountFlatFileItemReader,
            ItemProcessor<AmountDto, AmountDto> amoutFileItemProcessor,
            FlatFileItemWriter<AmountDto> amountFileItemWriter) {
        return stepBuilderFactory.get("multiThreadStep")
                .<AmountDto, AmountDto>chunk(10)
                .reader(amountFlatFileItemReader)
                .processor(amoutFileItemProcessor)
                .writer(amountFileItemWriter)
                .build();
    }

    @Bean
    public Flow flowAnotherStep(Step anotherStep) {
        return new FlowBuilder<SimpleFlow>("flowAnotherStep")
                .start(anotherStep)
                .build();
    }

    @Bean
    public Step anotherStep() {
        return stepBuilderFactory.get("anotherStep")
                .tasklet((contribution, chunkContext) -> {
                    Thread.sleep(50);
                    System.out.println("Another Step Completed. Thread = " + Thread.currentThread().getName());
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
